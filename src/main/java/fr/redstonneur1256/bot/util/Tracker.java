package fr.redstonneur1256.bot.util;

import arc.util.Timer;

import java.util.concurrent.atomic.AtomicInteger;

public class Tracker {

    private RollingAverage average;
    private AtomicInteger count;
    private int threshold;

    public Tracker(int samples, int threshold) {
        this.average = new RollingAverage(samples);
        this.count = new AtomicInteger();
        this.threshold = threshold;

        // there must be a better way than rotate the values every second
        Timer.schedule(() -> average.append(count.getAndSet(0)), 1, 1);
    }

    public boolean increment() {
        count.incrementAndGet();
        return average.getTotal() > threshold;
    }

    public void resize(int samples) {
        average.resize(samples);
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

}
