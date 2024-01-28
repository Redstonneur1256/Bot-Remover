package fr.redstonneur1256.bot;

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
import fr.redstonneur1256.bot.util.HttpCache;
import fr.redstonneur1256.bot.util.RadixTree;
import fr.redstonneur1256.bot.util.Tracker;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.mod.Plugin;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class BotProtector extends Plugin {

    public static BotProtector instance;

    private Config config;

    public Mode mode = Mode.DISABLED;
    public Tracker connectionTracker;
    public Tracker packetTracker;
    private Timer.Task chillTask;

    public Seq<BlockListProvider> providers;
    public RadixTree ipv4tree;
    public RadixTree ipv6tree;

    public final AtomicLong blockConnections = new AtomicLong();
    public final AtomicLong blockedPackets = new AtomicLong();
    public final AtomicLong blockedBytes = new AtomicLong();

    @Override
    public void init() {
        instance = this;

        config = new Config();

        connectionTracker = config.connectionTracker;
        packetTracker = config.packetTracker;

        providers = Seq.with(new RawBlockListProvider("https://raw.githubusercontent.com/X4BNet/lists_vpn/main/output/datacenter/ipv4.txt"), new CsvBlockListProvider("https://digitalocean.com/geo/google.csv", 0), new CsvBlockListProvider("https://geoip.linode.com/", 0), new AwsBlockListProvider(), new GoogleCloudBlockListProvider("https://www.gstatic.com/ipranges/goog.json"), new GoogleCloudBlockListProvider("https://www.gstatic.com/ipranges/cloud.json"), new AzureBlockListProvider());

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
        // why did I name it "bl" ???
        handler.register("bl-info", "Displays stats", args -> {
            Log.info("Bot-Protector v@", Vars.mods.getMod(BotProtector.class).meta.version);
            Log.info("Mode: @", mode);
            Log.info("Blocked connections (since start): @", blockConnections.get());
            Log.info("IPv4: @ nodes | IPv6: @ nodes", ipv4tree.getNodeCount(), ipv6tree.getNodeCount());
        });
        handler.register("bl-reload", "Reloads the IP blacklist", args -> Threads.daemon(() -> reload(true, true)));
        handler.register("bl-toggle", "Toggles the bot protection mode", args -> {
            mode = Mode.values()[(mode.ordinal() + 1) % Mode.values().length];
            Log.info("Bot-Protector mode changed to " + mode);
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

        RadixTree ipv4tree = new RadixTree();
        RadixTree ipv6tree = new RadixTree();

        for (String address : addresses) {
            try {
                String[] parts = address.split("/");
                InetAddress addr = InetAddress.getByName(parts[0]);
                int subnet = Integer.parseInt(parts[1]);

                RadixTree tree = addr instanceof Inet4Address ? ipv4tree : ipv6tree;

                tree.set(addr.getAddress(), subnet);
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        Log.log(level, "[Bot-Protector] Built ip tree in @ ms", Time.elapsed());

        this.ipv4tree = ipv4tree;
        this.ipv6tree = ipv6tree;

        Log.log(log ? Log.LogLevel.info : Log.LogLevel.debug, "[Bot-Protector] (re)loaded @ addresses", addresses.size());
    }

    public boolean isBlocked(InetSocketAddress remote) {
        switch (mode) {
            case DISABLED:
                return false;
            case BLOCK_ALL:
                return true;
            case BLOCK_VPN:
                byte[] address = remote.getAddress().getAddress();
                RadixTree tree = remote.getAddress() instanceof Inet4Address ? ipv4tree : ipv6tree;
                return tree.find(address);
        }
        return false;
    }

    public void incrementMode() {
        if (mode == Mode.BLOCK_ALL) {
            return;
        }

        if (mode == Mode.DISABLED) {
            Log.warn("[Bot-Protector] Entering under attack mode!");
        }

        mode = Mode.values()[mode.ordinal() + 1];
        scheduleDecrement();
    }

    public void decrementMode() {
        if (mode == Mode.DISABLED) {
            return;
        }
        mode = Mode.values()[mode.ordinal() - 1];

        if (mode != Mode.DISABLED) {
            scheduleDecrement();
        }
    }

    private void scheduleDecrement() {
        if (chillTask != null) {
            chillTask.cancel();
        }
        chillTask = Timer.schedule(this::decrementMode, config.decrementDelay.num());
    }

}
