package fr.redstonneur1256.bot.provider;

import fr.redstonneur1256.bot.util.HttpCache;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;
import java.util.stream.Collectors;

public class CsvBlockListProvider implements BlockListProvider {

    private final String url;
    private final int column;

    public CsvBlockListProvider(String url, int column) {
        this.url = url;
        this.column = column;
    }

    @Override
    public String getName() {
        return url;
    }

    @Override
    public Set<String> provide(HttpCache cache) throws IOException {
        return cache.get(url, stream -> new BufferedReader(new InputStreamReader(stream))
                .lines()
                .filter(line -> !line.isEmpty() && line.charAt(0) != '#')
                .map(line -> line.split(","))
                .filter(parts -> parts.length >= column)
                .map(parts -> parts[column])
                .collect(Collectors.toSet()));
    }

}
