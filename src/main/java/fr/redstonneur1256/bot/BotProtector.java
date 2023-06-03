package fr.redstonneur1256.bot;

import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Threads;
import arc.util.Timer;
import fr.redstonneur1256.bot.provider.BlockListProvider;
import fr.redstonneur1256.bot.provider.LocalAzureBlockListProvider;
import fr.redstonneur1256.bot.provider.RawBlockListProvider;
import inet.ipaddr.IPAddressString;
import mindustry.mod.Plugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BotProtector extends Plugin {

    public static final AtomicInteger blocked = new AtomicInteger();
    public static Set<IPAddressString> addresses = new HashSet<>();

    private Seq<BlockListProvider> providers;

    @Override
    public void init() {
        try {
            providers = Seq.with(
                    new RawBlockListProvider(new URL("https://raw.githubusercontent.com/X4BNet/lists_vpn/main/output/datacenter/ipv4.txt")),
                    new LocalAzureBlockListProvider()
            );
        } catch (MalformedURLException exception) {
            throw new RuntimeException(exception);
        }

        reload();
        Timer.schedule(() -> Threads.daemon(this::reload), 3600, 3600);
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("conns", "Displays stats", args -> Log.info("Blocked @ connections", blocked.get()));
    }

    private void reload() {
        Set<IPAddressString> addresses = new HashSet<>();

        for (BlockListProvider provider : providers) {
            addresses.addAll(provider.provide());
        }

        Log.info("[Bot-Protector] (re)loaded @ addresses", addresses.size());
        BotProtector.addresses = addresses;
    }

}
