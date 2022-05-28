package org.sgraph;

import javafx.scene.paint.Color;

public class Range {
    private final double min;
    private final double max;

    public Range(double min, double max) {
        if (min < 0)
            throw new IllegalArgumentException("Range: MIN must be non-negative.");

        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public Color getHSBValue(double value)
    {
        if (value < min || value > max)
            throw new IllegalArgumentException("Range: Value must be in range bounds.");

        if (min == max) // prevents 0-0 range as well
            return Color.hsb(0, 1, 1, 1);

        return Color.hsb((1 - (value - min) / (max - min)) * 240, 1, 1, 1);
    }

    @Override
    public String toString() {
        return min + " - " + max;
    }
}
