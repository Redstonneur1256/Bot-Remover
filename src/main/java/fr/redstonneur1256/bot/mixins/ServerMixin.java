package fr.redstonneur1256.bot.mixins;

import arc.net.*;
import arc.util.Log;
import fr.redstonneur1256.bot.BotProtector;
import inet.ipaddr.IPAddressString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Inject(method = "acceptOperation", at = @At("HEAD"), cancellable = true)
    private void injectAccept(SocketChannel channel, CallbackInfo ci) {
        try {
            SocketAddress remote = channel.getRemoteAddress();

            if (remote instanceof InetSocketAddress) {
                if (blockedAddress((InetSocketAddress) remote, "TCP")) {
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
    private boolean cancelled;

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Larc/net/Server;addConnection(Larc/net/Connection;)V"))
    private void redirectAddConnection(Server instance, Connection connection) {
        cancelled = connection.getRemoteAddressUDP() != null && blockedAddress(connection.getRemoteAddressUDP(), "UDP");

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

    private boolean blockedAddress(InetSocketAddress remote, String method) {
        IPAddressString address = new IPAddressString(remote.getAddress().getHostAddress());

        if (BotProtector.logging) {
            Log.warn("[Bot-Protector] [@] Blocked connection @", method, remote);
        }

        for (IPAddressString blacklisted : BotProtector.addresses) {
            if (blacklisted.contains(address)) {
                BotProtector.blocked.incrementAndGet();
                return true;
            }
        }

        return false;
    }

}
