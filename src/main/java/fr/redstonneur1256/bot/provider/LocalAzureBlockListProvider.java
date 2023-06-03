package fr.redstonneur1256.bot.provider;

import arc.util.Log;
import arc.util.serialization.Jval;
import fr.redstonneur1256.bot.BotProtector;
import inet.ipaddr.IPAddressString;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class LocalAzureBlockListProvider implements BlockListProvider {

    @Override
    public Set<IPAddressString> provide() {
        try (InputStream stream = BotProtector.class.getResourceAsStream("/azure.json")) {
            if (stream == null) {
                throw new IOException("Not found");
            }

            Set<IPAddressString> addresses = new HashSet<>();

            for (Jval value : Jval.read(new InputStreamReader(stream)).get("values").asArray()) {
                for (Jval addressPrefixes : value.get("properties").get("addressPrefixes").asArray()) {
                    addresses.add(new IPAddressString(addressPrefixes.asString()));
                }
            }

            return addresses;
        } catch (IOException exception) {
            Log.err("[Bot-Protector] Unable to load azure blocklist", exception);

            return new HashSet<>();
        }
    }

}
