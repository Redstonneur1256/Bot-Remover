package fr.redstonneur1256.bot.provider;

import arc.util.Log;
import arc.util.Strings;
import inet.ipaddr.IPAddressString;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class RawBlockListProvider implements BlockListProvider {

    private final URL url;

    public RawBlockListProvider(URL url) {
        this.url = url;
    }

    @Override
    public String getName() {
        return url.toString();
    }

    @Override
    public Set<IPAddressString> provide() {
        try {
            URLConnection connection = url.openConnection();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                return reader.lines()
                        .filter(line -> !line.isEmpty())
                        .map(IPAddressString::new)
                        .collect(Collectors.toSet());
            }
        } catch (IOException exception) {
            Log.err("[Bot-Protector] Unable to load blocklist from @:\n@", url, Strings.getStackTrace(exception));

            return new HashSet<>();
        }
    }

}
