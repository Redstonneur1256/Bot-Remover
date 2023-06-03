package fr.redstonneur1256.bot.hooks.unifiedmetrics;

import dev.cubxity.plugins.metrics.api.metric.collector.Collector;
import dev.cubxity.plugins.metrics.api.metric.data.CounterMetric;
import dev.cubxity.plugins.metrics.api.metric.data.Metric;
import fr.redstonneur1256.bot.BotProtector;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class UnifiedMetricBlockedCollector implements Collector {

    @NotNull
    @Override
    public List<Metric> collect() {
        return Collections.singletonList(new CounterMetric("bot_protector_blocked_connections", Collections.emptyMap(), BotProtector.blocked.get()));
    }

}
