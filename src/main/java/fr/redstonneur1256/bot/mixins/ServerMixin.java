package fr.redstonneur1256.bot.mixins;

import arc.net.Connection;
import arc.net.DcReason;
import arc.net.NetListener;
import arc.net.Server;
import arc.net.UdpConnection;
import arc.util.Log;
import fr.redstonneur1256.bot.BotProtector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;

@Mixin(Server.class)
public abstract class ServerMixin {

    @Shadow
    private NetListener dispatchListener;

    @Shadow
    protected abstract void addConnection(Connection connection);

    @Shadow
    private Connection[] connections;

    @Inject(method = "acceptOperation", at = @At("HEAD"), cancellable = true)
    private void injectAccept(SocketChannel channel, CallbackInfo ci) {
        try {
            SocketAddress remote = channel.getRemoteAddress();
            if (remote instanceof InetSocketAddress) {
                if (handleAddress((InetSocketAddress) remote, "TCP")) {
                    ci.cancel();
                    channel.close();
                }
                return;
            }

            Log.warn("[Bot-Protector] Unknown type of address @", remote.getClass().getName());
        } catch (IOException exception) {
            Log.err("[Bot-Protector]", exception);
        }
    }

    /**
     * Used to determine if the last connection to has done UDP registration was cancelled or not, required because of
     * two different Mixins injection points since we cannot use CaptureLocals due to UdpConnection being private
     */
    private @Unique boolean cancelled;

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Larc/net/Server;addConnection(Larc/net/Connection;)V"))
    private void redirectAddConnection(Server instance, Connection connection) {
        cancelled = connection.getRemoteAddressUDP() != null && handleAddress(connection.getRemoteAddressUDP(), "UDP");

        if (cancelled) {
            // Do not trigger the listener, it's not triggered for connected so shouldn't be triggered for closed
            connection.removeListener(dispatchListener);

            connection.close(DcReason.closed);
        } else {
            addConnection(connection);
        }
    }

    @Inject(method = "update",
            at = @At(
                    value = "INVOKE",
                    shift = At.Shift.AFTER,
                    target = "Larc/net/Server;addConnection(Larc/net/Connection;)V"
            ),
            cancellable = true
    )
    private void injectUpdateRegisterUDP(int timeout, CallbackInfo ci) {
        if (cancelled) {
            // the connection was cancelled, prevent sending RegisterUDP and notifying connected
            ci.cancel();
        }
    }


    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Larc/net/UdpConnection;readFromAddress()Ljava/net/InetSocketAddress;"))
    private InetSocketAddress redirectReadFromAddress(UdpConnection instance) throws IOException {
        InetSocketAddress address = instance.readFromAddress();

        for (Connection connection : connections) {
            if (address.equals(connection.udpRemoteAddress)) {
                return address; // actual connection, let it live
            }
        }

        BotProtector protector = BotProtector.instance;

        if (protector.isBlocked(address)) {
            protector.blockedPackets.incrementAndGet();
            protector.blockedBytes.addAndGet(instance.readBuffer.position());
            return null; // if returning null then it's just ignored by arc
        }

        if (protector.packetTracker.increment()) {
            protector.incrementMode();
        }

        return address;
    }

    @Unique
    private boolean handleAddress(InetSocketAddress remote, String method) {
        BotProtector protector = BotProtector.instance;

        if (!protector.isBlocked(remote)) {
            return false;
        }

        if (protector.connectionTracker.increment()) {
            protector.incrementMode();
        }

        protector.blockConnections.incrementAndGet();

        return true;
    }

}
