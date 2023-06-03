package fr.redstonneur1256.bot;

import arc.files.Fi;
import arc.util.Http;
import arc.util.Log;
import arc.util.Threads;
import arc.util.Timer;
import arc.util.serialization.Jval;
import inet.ipaddr.IPAddressString;
import mindustry.Vars;
import mindustry.mod.Plugin;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

public class BotProtector extends Plugin {

    public static Set<IPAddressString> addresses = new HashSet<>();

    @Override
    public void init() {
        reloadGitHubMeta();
        Timer.schedule(() -> Threads.daemon(this::reloadGitHubMeta), 3601, 3601);
    }

    private void reloadGitHubMeta() {
        Log.debug("[Bot-Protector] (re)loading GitHub actions IPs");

        Fi file = Vars.tmpDirectory.child("actions-meta-cache.json");
        file.parent().mkdirs();

        if (!file.exists() || Instant.ofEpochMilli(file.lastModified()).plus(1, ChronoUnit.HOURS).isBefore(Instant.now())) {
            Log.debug("[Bot-Protector] Downloading GitHub meta...", file);

            Http.get("https://api.github.com/meta").block(response -> {
                file.write(response.getResultAsStream(), false);

                Log.debug("[Bot-Protector] Downloaded cache to @", file);
            });
        }

        Set<IPAddressString> addresses = new HashSet<>();

        try (var reader = file.reader()) {
            for (Jval jval : Jval.read(reader).get("actions").asArray()) {
                addresses.add(new IPAddressString(jval.asString()));
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        Log.debug("[Bot-Protector] Found @ masks", addresses.size());

        BotProtector.addresses = addresses;
    }

}
