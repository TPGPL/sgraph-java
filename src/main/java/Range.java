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

    @Override
    public String toString() {
        return min + " - " + max;
    }
}
