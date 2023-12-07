package fr.redstonneur1256.bot.provider;

import arc.util.serialization.Jval;
import fr.redstonneur1256.bot.util.HttpCache;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class AzureBlockListProvider implements BlockListProvider {

    @Override
    public String getName() {
        return "Microsoft Azure";
    }

    @Override
    public Set<String> provide(HttpCache cache) throws IOException {
        Document document = cache.getDocument("https://www.microsoft.com/en-us/download/details.aspx?id=56519");
        Elements downloadElements = document.getElementsByAttribute("download");
        if (downloadElements.isEmpty() || !downloadElements.get(0).hasAttr("href")) {
            throw new IllegalStateException("Could not locate download button");
        }
        String href = downloadElements.get(0).attributes().get("href");

        Set<String> addresses = new HashSet<>();

        Jval json = cache.getJson(href);
        for (Jval value : json.get("values").asArray()) {
            for (Jval addressPrefixes : value.get("properties").get("addressPrefixes").asArray()) {
                addresses.add(addressPrefixes.asString());
            }
        }

        return addresses;
    }

}
