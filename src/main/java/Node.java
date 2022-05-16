import java.util.ArrayList;

public class Node {
    private final int index;
    private final ArrayList<Connection> connections;

    public Node(int index)
    {
        this.index = index;
        connections = new ArrayList<>();
    }

    public int getIndex()
    {
        return index;
    }

    public void addConnection(Node node, double edge)
    {
        connections.add(new Connection(node, edge));
    }

    public void removeConnection(Node node)
    {
        connections.remove(new Connection(node, 0));
    }

    public boolean hasConnection(Node node)
    {
        return connections.contains(new Connection(node, 0));
    }

    public int getAdherentNumber()
    {
        return connections.size();
    }

    public double getEdgeOnConnection(Node node)
    {
        int index = connections.indexOf(new Connection(node, 0));

        if (index == -1)
            return 0;

        return connections.get(index).getWeight();
    }

    @Override
    public String toString()
    {
        String text = "\t\t";

        for (Connection c : connections)
            text = text.concat(c.toString() + " ");

        return text;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node n)
            return n.getIndex() == this.getIndex();

        return false;
    }
}
