package fr.redstonneur1256.bot.mixins;

import arc.net.Server;
import arc.util.Log;
import fr.redstonneur1256.bot.BotProtector;
import inet.ipaddr.IPAddressString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

@Mixin(Server.class)
public class ServerMixin {

    @Inject(method = "acceptOperation", at = @At("HEAD"), cancellable = true)
    private void injectAccept(SocketChannel channel, CallbackInfo ci) {
        try {
            if (channel.getRemoteAddress() instanceof InetSocketAddress inetAddress) {
                IPAddressString address = new IPAddressString(inetAddress.getAddress().getHostAddress());

                for (IPAddressString blacklisted : BotProtector.addresses) {
                    if (blacklisted.contains(address)) {
                        ci.cancel();
                        channel.close();
                        return;
                    }
                }

                return;
            }

            Log.warn("[Bot-Protector] Unknown type of address @", channel.getRemoteAddress().getClass().getName());
        } catch (IOException exception) {
            Log.err("[Bot-Protector]", exception);
        }
    }

}
