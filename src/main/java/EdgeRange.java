public class EdgeRange extends Range {
    public EdgeRange(double min, double max)
    {
        super(min, max);

        if (max <= min)
            throw new IllegalArgumentException("Range: MIN must be lower than MAX.");
    }
}
