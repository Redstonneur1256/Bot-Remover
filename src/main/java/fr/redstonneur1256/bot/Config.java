package fr.redstonneur1256.bot;

import fr.redstonneur1256.bot.util.Tracker;
import mindustry.net.Administration;

public class Config {

    public Administration.Config connectionLimit;
    public Administration.Config connectionLimitInterval;

    public Administration.Config packetLimit;
    public Administration.Config packetLimitInterval;

    public Administration.Config decrementDelay;

    public Tracker connectionTracker;
    public Tracker packetTracker;

    public Config() {
        connectionLimit = new Administration.Config("bl-con-limit",
                "amount of connections required in 'bl-con-time' seconds to activate the plugin",
                20,
                () -> connectionTracker.setThreshold(connectionLimit.num()));

        connectionLimitInterval = new Administration.Config("bl-con-time",
                "see 'bl-con-limit'",
                4,
                () -> connectionTracker.resize(connectionLimitInterval.num()));


        packetLimit = new Administration.Config("bl-packet-limit",
                "amount of UDP packets required in 'bl-packet-time' seconds to activate the plugin, not triggered by players",
                20, // only UDP packets sent by non-players are ping packets and 20/second as the default should be safe
                () -> packetTracker.setThreshold(packetLimit.num()));

        packetLimitInterval = new Administration.Config(
                "bl-packet-time",
                "see 'bl-packet-limit'",
                1,
                () -> packetTracker.resize(packetLimitInterval.num()));


        decrementDelay = new Administration.Config("bl-delay", "Time in seconds before the level is lowered", 60);

        connectionTracker = new Tracker(connectionLimitInterval.num(), connectionLimit.num());
        packetTracker = new Tracker(packetLimitInterval.num(), packetLimit.num());

    }

}
