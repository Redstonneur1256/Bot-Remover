package fr.redstonneur1256.bot.provider;

import inet.ipaddr.IPAddressString;

import java.util.Set;

public interface BlockListProvider {

    Set<IPAddressString> provide();

}
