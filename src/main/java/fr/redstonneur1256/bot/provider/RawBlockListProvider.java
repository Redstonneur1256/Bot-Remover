package fr.redstonneur1256.bot.provider;

import fr.redstonneur1256.bot.util.HttpCache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public class RawBlockListProvider implements BlockListProvider {

    private final String url;

    public RawBlockListProvider(String url) {
        this.url = url;
    }

    @Override
    public String getName() {
        return url;
    }

    @Override
    public Set<String> provide(HttpCache cache) throws IOException {
        return cache.get(url, stream -> new BufferedReader(new InputStreamReader(stream))
                .lines()
                .filter(line -> !line.isEmpty())
                .collect(Collectors.toSet()));
    }

}
