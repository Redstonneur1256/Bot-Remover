package fr.redstonneur1256.bot.provider;

import arc.util.serialization.Jval;
import fr.redstonneur1256.bot.util.HttpCache;

import java.io.IOException;
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
    public Set<String> provide(HttpCache cache) throws IOException {
        Set<String> addresses = new HashSet<>();
        Jval json = cache.getJson(url);

        for (Jval prefix : json.get("prefixes").asArray()) {
            Jval ipv4 = prefix.get("ipv4Prefix");
            Jval ipv6 = prefix.get("ipv6Prefix");
            if (ipv4 != null && ipv4.isString()) addresses.add(ipv4.asString());
            if (ipv6 != null && ipv6.isString()) addresses.add(ipv6.asString());
        }

        return addresses;
    }

}
