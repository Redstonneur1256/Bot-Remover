package fr.redstonneur1256.bot.provider;

import arc.util.Http;
import arc.util.Log;
import arc.util.Strings;
import arc.util.serialization.Jval;
import inet.ipaddr.IPAddressString;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class GoogleCloudBlockListProvider implements BlockListProvider {

    private final String url;

    public GoogleCloudBlockListProvider(String url) {
        this.url = url;
    }

    @Override
    public String getName() {
        return url;
    }

    @Override
    public Set<IPAddressString> provide() {
        try {
            Set<IPAddressString> addresses = new HashSet<>();

            Http.get(url).header("User-Agent", "BotProtector").block(response -> {
                Jval json = Jval.read(new InputStreamReader(response.getResultAsStream()));

                for (Jval prefix : json.get("prefixes").asArray()) {
                    Jval ipv4 = prefix.get("ipv4Prefix");
                    Jval ipv6 = prefix.get("ipv6Prefix");
                    if (ipv4 != null && ipv4.isString()) addresses.add(new IPAddressString(ipv4.asString()));
                    if (ipv6 != null && ipv6.isString()) addresses.add(new IPAddressString(ipv6.asString()));
                }
            });

            return addresses;
        } catch (Throwable exception) {
            Log.err("[Bot-Protector] Unable to load blocklist from @:\n@", url, Strings.getStackTrace(exception));

            return new HashSet<>();
        }
    }

}
