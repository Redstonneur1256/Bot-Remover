package fr.redstonneur1256.bot.hooks.unifiedmetrics;

import arc.util.Log;
import dev.cubxity.plugins.metrics.api.UnifiedMetricsProvider;
import dev.cubxity.plugins.metrics.api.metric.collector.Collector;
import dev.cubxity.plugins.metrics.api.metric.collector.CollectorCollection;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class UnifiedMetricsHook implements CollectorCollection {

    public static void hook() {
        Log.info("[Bot-Protector] UnifiedMetrics hook enabled.");

        UnifiedMetricsProvider.get().getMetricsManager().registerCollection(new UnifiedMetricsHook());
    }

    @NotNull
    @Override
    public List<Collector> getCollectors() {
        return Collections.singletonList(new UnifiedMetricBlockedCollector());
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public void dispose() {
    }

    @Override
    public void initialize() {
    }

}
