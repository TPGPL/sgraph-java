public class Range {
    private final double min;
    private final double max;

    public Range(double min, double max) {
        if (min < 0 || max <= min)
            throw new IllegalArgumentException("Range: MIN must be positive and lower than MAX.");

        this.min = min;
        this.max = max;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

}
