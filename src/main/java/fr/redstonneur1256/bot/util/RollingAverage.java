package fr.redstonneur1256.bot.util;

import java.util.Arrays;

// pretty sure there is a built-in arc class to do that, but I cannot find it
public class RollingAverage {

    private int[] values;
    private int index;
    private int sum;

    public RollingAverage(int samples) {
        this.values = new int[samples];
        this.index = 0;
        this.sum = 0;
    }

    public void resize(int samples) {
        values = Arrays.copyOf(values, samples);
        index = 0;
        sum = 0;

        for (int value : values) {
            sum += value;
        }
    }

    public void append(int value) {
        sum -= values[index];
        sum += value;
        values[index] = value;
        index = (index + 1) % values.length;
    }

    public int getAverage() {
        return sum / values.length;
    }

    public int getTotal() {
        return sum;
    }

}
