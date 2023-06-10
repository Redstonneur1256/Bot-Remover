package fr.redstonneur1256.bot;

import arc.Core;
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
    public static boolean logging = Core.settings.getBool("bot-protector-logging", false);

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

        reload(true, true);
        Timer.schedule(() -> Threads.daemon(() -> reload(false, false)), 3600, 3600);

        Events.run(EventType.ServerLoadEvent.class, () -> {
            if (Vars.mods.getMod("unifiedmetrics") != null) {
                UnifiedMetricsHook.hook();
            }
        });
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("bl-stat", "Displays stats", args -> Log.info("Blocked @ connections", blocked.get()));
        handler.register("bl-log", "Enable logging of blocked connections", args -> {
            logging = !logging;
            Core.settings.put("bot-protector-logging", logging);
            Log.info("Logging is now @", logging ? "enabled" : "disabled");
        });
        handler.register("bl-reload", "Reloads the IP blacklist", args -> Threads.daemon(() -> reload(true, true)));
    }

    private void reload(boolean log, boolean debug) {
        Set<IPAddressString> addresses = new HashSet<>();

        for (BlockListProvider provider : providers) {
            Set<IPAddressString> found = provider.provide();
            addresses.addAll(found);

            Log.log(debug ? Log.LogLevel.info : Log.LogLevel.debug, "[Bot-Protector] Found @ addresses for blocklist @", found.size(), provider.getName());
        }

        Log.log(log ? Log.LogLevel.info : Log.LogLevel.debug, "[Bot-Protector] (re)loaded @ addresses", addresses.size());
        BotProtector.addresses = addresses;
    }

}
