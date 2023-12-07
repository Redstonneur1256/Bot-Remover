package fr.redstonneur1256.bot;

import arc.Core;
import arc.Events;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.Threads;
import arc.util.Time;
import arc.util.Timer;
import fr.redstonneur1256.bot.hooks.unifiedmetrics.UnifiedMetricsHook;
import fr.redstonneur1256.bot.provider.AwsBlockListProvider;
import fr.redstonneur1256.bot.provider.AzureBlockListProvider;
import fr.redstonneur1256.bot.provider.BlockListProvider;
import fr.redstonneur1256.bot.provider.CsvBlockListProvider;
import fr.redstonneur1256.bot.provider.GoogleCloudBlockListProvider;
import fr.redstonneur1256.bot.provider.RawBlockListProvider;
import fr.redstonneur1256.bot.util.AddressTree;
import fr.redstonneur1256.bot.util.HttpCache;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Plugin;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BotProtector extends Plugin {

    public static BotProtector instance;

    public final AtomicInteger blocked = new AtomicInteger();
    public boolean active = Core.settings.getBool("bot-protector-active", true);
    public boolean logging = Core.settings.getBool("bot-protector-logging", false);
    public AddressTree ipv4tree;
    public AddressTree ipv6tree;
    public Seq<BlockListProvider> providers;

    @Override
    public void init() {
        instance = this;

        providers = Seq.with(
                new RawBlockListProvider("https://raw.githubusercontent.com/X4BNet/lists_vpn/main/output/datacenter/ipv4.txt"),
                new CsvBlockListProvider("https://digitalocean.com/geo/google.csv", 0),
                new CsvBlockListProvider("https://geoip.linode.com/", 0),
                new AwsBlockListProvider(),
                new GoogleCloudBlockListProvider("https://www.gstatic.com/ipranges/goog.json"),
                new GoogleCloudBlockListProvider("https://www.gstatic.com/ipranges/cloud.json"),
                new AzureBlockListProvider()
        );

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
        handler.register("bl-info", "Displays stats", args -> {
            Log.info("Bot-Protector v@", Vars.mods.getMod(BotProtector.class).meta.version);
            Log.info("Active: @", active);
            Log.info("Blocked connections (since start): @", blocked.get());
            Log.info("IPv4: @ nodes | IPv6: @ nodes", ipv4tree.getNodeCount(), ipv6tree.getNodeCount());
        });
        handler.register("bl-log", "Enable logging of blocked connections", args -> {
            logging = !logging;
            Core.settings.put("bot-protector-logging", logging);
            Log.info("Logging is now @", logging ? "enabled" : "disabled");
        });
        handler.register("bl-reload", "Reloads the IP blacklist", args -> Threads.daemon(() -> reload(true, true)));
        handler.register("bl-toggle", "Toggles the bot protection", args -> {
            active = !active;
            Core.settings.put("bot-protector-active", active);
            Log.info("Bot-Protector has been @", active ? "enabled" : "disabled");
        });
    }

    private void reload(boolean log, boolean debug) {
        Log.LogLevel level = debug ? Log.LogLevel.info : Log.LogLevel.debug;
        Set<String> addresses = new HashSet<>();

        HttpCache cache = new HttpCache();

        for (BlockListProvider provider : providers) {
            try {
                Set<String> found = provider.provide(cache);
                addresses.addAll(found);

                Log.log(level, "[Bot-Protector] Found @ addresses for blocklist @", found.size(), provider.getName());
            } catch (Exception exception) {
                Log.err("[Bot-Protector] Unable to (re)load addresses from blocklist @", provider.getName());
                Log.err(exception);
            }
        }

        Log.log(level, "[Bot-Protector] Rebuilding IP tree...");

        Time.mark();

        AddressTree ipv4tree = new AddressTree();
        AddressTree ipv6tree = new AddressTree();

        for (String address : addresses) {
            try {
                String[] parts = address.split("/");
                InetAddress addr = InetAddress.getByName(parts[0]);
                int subnet = Integer.parseInt(parts[1]);

                AddressTree tree = addr instanceof Inet4Address ? ipv4tree : ipv6tree;

                byte[] key = addr.getAddress();
                byte[] mask = getAddressMask(key.length, subnet);

                tree.set(key, mask);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        Log.log(level, "[Bot-Protector] Built ip tree in @ ms", Time.elapsed());

        this.ipv4tree = ipv4tree;
        this.ipv6tree = ipv6tree;

        Log.log(log ? Log.LogLevel.info : Log.LogLevel.debug, "[Bot-Protector] (re)loaded @ addresses", addresses.size());
    }

    private byte[] getAddressMask(int length, int subnet) {
        byte[] mask = new byte[length];
        for (int bit = 0; bit < subnet; bit++) {
            mask[bit / 8] |= (byte) (0x80 >> (bit % 8));
        }
        return mask;
    }

}
