package fr.redstonneur1256.bot.provider;

import fr.redstonneur1256.bot.util.HttpCache;

import java.util.Set;

public interface BlockListProvider {

    String getName();

    Set<String> provide(HttpCache cache) throws Exception;

}
