package fr.redstonneur1256.bot.provider;

import arc.util.serialization.Jval;
import fr.redstonneur1256.bot.util.HttpCache;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AwsBlockListProvider implements BlockListProvider {

    @Override
    public String getName() {
        return "AWS";
    }

    @Override
    public Set<String> provide(HttpCache cache) throws IOException {
        Set<String> addresses = new HashSet<>();

        Jval json = cache.getJson("https://ip-ranges.amazonaws.com/ip-ranges.json");
        parseAddresses(addresses, json.get("prefixes"), "ip_prefix");
        parseAddresses(addresses, json.get("ipv6_prefixes"), "ipv6_prefix");
        return addresses;
    }

    private void parseAddresses(Set<String> addresses, Jval prefixes, String key) {
        for (Jval prefix : prefixes.asArray()) {
            addresses.add(prefix.getString(key));
        }
    }

}
