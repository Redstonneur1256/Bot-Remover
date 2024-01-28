package fr.redstonneur1256.bot.hooks.unifiedmetrics;

import dev.cubxity.plugins.metrics.api.metric.collector.Collector;
import dev.cubxity.plugins.metrics.api.metric.data.CounterMetric;
import dev.cubxity.plugins.metrics.api.metric.data.Metric;
import fr.redstonneur1256.bot.BotProtector;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UnifiedMetricBlockedCollector implements Collector {

    @Override
    public @NotNull List<Metric> collect() {
        return Arrays.asList(
                new CounterMetric("bot_protector_blocked_connections", Collections.emptyMap(), BotProtector.instance.blocked.get()),
                new CounterMetric("bot_protector_blocked_packets", Collections.emptyMap(), BotProtector.instance.blockedPackets.get()),
                new CounterMetric("bot_protector_blocked_bytes", Collections.emptyMap(), BotProtector.instance.blockedBytes.get())
        );
    }

}
