package fr.redstonneur1256.bot;

import arc.Events;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Threads;
import arc.util.Timer;
import fr.redstonneur1256.bot.hooks.unifiedmetrics.UnifiedMetricsHook;
import fr.redstonneur1256.bot.provider.BlockListProvider;
import fr.redstonneur1256.bot.provider.GoogleCloudBlockListProvider;
import fr.redstonneur1256.bot.provider.LocalAzureBlockListProvider;
import fr.redstonneur1256.bot.provider.RawBlockListProvider;
import inet.ipaddr.IPAddressString;
import mindustry.Vars;
import mindustry.game.EventType;
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
                    new GoogleCloudBlockListProvider("https://www.gstatic.com/ipranges/goog.json"),
                    new GoogleCloudBlockListProvider("https://www.gstatic.com/ipranges/cloud.json"),
                    new LocalAzureBlockListProvider()
            );
        } catch (MalformedURLException exception) {
            throw new RuntimeException(exception);
        }

        reload();
        Timer.schedule(() -> Threads.daemon(this::reload), 3600, 3600);

        Events.run(EventType.ServerLoadEvent.class, () -> {
            if (Vars.mods.getMod("unifiedmetrics") != null) {
                UnifiedMetricsHook.hook();
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("conns", "Displays stats", args -> Log.info("Blocked @ connections", blocked.get()));
        handler.register("bl-reload", "Reloads the IP blacklist", args -> Threads.daemon(this::reload));
    }

    private void reload() {
        Set<IPAddressString> addresses = new HashSet<>();

        for (BlockListProvider provider : providers) {
            Set<IPAddressString> found = provider.provide();
            addresses.addAll(found);

            Log.debug("[Bot-Protector] Found @ addresses for blocklist @", found.size(), provider.getName());
        }

        Log.info("[Bot-Protector] (re)loaded @ addresses", addresses.size());
        BotProtector.addresses = addresses;
    }

}
