public class Connection {
    private final Node node;
    private final double weight;

    public Connection(Node node, double weight)
    {
        if (weight <= 0)
            throw new IllegalArgumentException("Connection: Edge value must be positive.");

        this.node = node;
        this.weight = weight;
    }

    public double getWeight() {
        return weight;
    }

    public Node getNode()
    {
        return node;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Connection co)
            return co.node.getIndex() == this.node.getIndex(); // comparing only through node index to simplify implementation

        return false;
    }

    @Override
    public String toString()
    {
        return node.getIndex() + ":" + weight;
    }
}
