package fr.redstonneur1256.bot.util;

import arc.files.Fi;
import arc.util.io.Streams;
import arc.util.serialization.Jval;
import mindustry.Vars;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public class HttpCache {

    public HttpCache() {
    }

    public InputStream get(String url) throws IOException {
        String hash = HashUtil.getHash("SHA-256", url.getBytes());
        Fi cachedFile = Vars.tmpDirectory.child("http").child(hash);

        // TODO: proper caching instead of fixed 1 day cache
        if (!cachedFile.exists() || cachedFile.lastModified() + TimeUnit.DAYS.toMillis(1) < System.currentTimeMillis()) {
            cachedFile.parent().mkdirs();

            try (OutputStream output = cachedFile.write();
                 InputStream input = new URL(url).openStream()) {
                Streams.copy(input, output);
            }
        }

        return cachedFile.read(8192);
    }

    public <T, E extends Throwable> T get(String url, UnsafeFunction<InputStream, T, E> mapper) throws IOException, E {
        try (InputStream stream = get(url)) {
            return mapper.apply(stream);
        }
    }

    public Document getDocument(String url) throws IOException {
        return get(url, stream -> Jsoup.parse(stream, "UTF-8", url));
    }

    public Jval getJson(String url) throws IOException {
        return get(url, stream -> Jval.read(new InputStreamReader(stream)));
    }

    public interface UnsafeFunction<A, R, E extends Throwable> {
        R apply(A a) throws E;
    }

}
