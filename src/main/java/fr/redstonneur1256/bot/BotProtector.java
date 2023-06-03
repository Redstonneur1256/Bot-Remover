package fr.redstonneur1256.bot;

import arc.util.CommandHandler;
import arc.util.Log;
import arc.util.serialization.Jval;
import inet.ipaddr.IPAddressString;
import mindustry.mod.Plugin;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class BotProtector extends Plugin {

    public static final AtomicInteger blocked = new AtomicInteger();
    public static final Set<IPAddressString> addresses = new HashSet<>();

    @Override
    public void init() {
        try (InputStream stream = BotProtector.class.getResourceAsStream("/azure.json")) {
            if (stream != null) {
                for (Jval value : Jval.read(new InputStreamReader(stream)).get("values").asArray()) {
                    for (Jval addressPrefixes : value.get("properties").get("addressPrefixes").asArray()) {
                        addresses.add(new IPAddressString(addressPrefixes.asString()));
                    }
                }
            }
        } catch (IOException exception) {
            Log.err("Failed to load internal azure addresses", exception);
        }
        Log.info("[Bot-Protector] Loaded @ azure address masks.", addresses.size());
    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.register("conns", "Displays stats", args -> Log.info("Blocked @ connections", blocked.get()));
    }

}
