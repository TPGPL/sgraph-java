package org.sgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Graph {
    private final int columnCount;
    private final int rowCount;
    private int subgraphCount;
    private final ArrayList<Node> nodes;
    private Range edgeValueRange;

    public Graph(int columnCount, int rowCount) {
        if (columnCount <= 0)
            throw new IllegalArgumentException("Graph: The number of columns must be positive.");

        if (rowCount <= 0)
            throw new IllegalArgumentException("Graph: The number of rows must be positive.");


        this.columnCount = columnCount;
        this.rowCount = rowCount;

        nodes = new ArrayList<>();

        for (int i = 0; i < getNodeCount(); i++) {
            nodes.add(new Node(i));
        }
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getNodeCount() {
        return rowCount * columnCount;
    }

    public int getSubgraphCount() {
        return subgraphCount;
    }

    public Range getEdgeValueRange() {
        return edgeValueRange;
    }

    public Node getNode(int index) throws IllegalArgumentException {
        if (index < 0 || index >= getNodeCount())
            throw new IllegalArgumentException(String.format("Graph: Cannot get a node of index %d in a %dx%d graph.", index, rowCount, columnCount));

        return nodes.get(index);
    }

    public void addConnection(Node node1, Node node2, double edge) throws IllegalArgumentException {
        if (!canNodesAdhere(node1, node2))
            throw new IllegalArgumentException(String.format("Graph: Nodes %d and %d cannot adhere in a %dx%d graph.", node1.getIndex(), node2.getIndex(), rowCount, columnCount));

        if (edge <= 0)
            throw new IllegalArgumentException("Graph: The edge value must be positive.");

        if (node1.hasConnection(node2)) // connection between node1 and node2 exists
        {
            double definedEdge = node1.getEdgeOnConnection(node2);

            if (definedEdge == edge) // the edge values are equal -> likely has been added the second time through IO
                return;
            else // trying to add a connection with a different edge value
                throw new IllegalArgumentException(String.format("Graph: Connection between nodes %d and %d has already been defined with an edge value of %g", node1.getIndex(), node2.getIndex(), definedEdge));
        }

        node1.addConnection(node2, edge);
        node2.addConnection(node1, edge);
    }

    public void removeConnection(Node node1, Node node2) {
        node1.removeConnection(node2);
        node2.removeConnection(node1);
    }

    private boolean canNodesAdhere(Node node1, Node node2) {
        int row1 = (node1.getIndex() - node1.getIndex() % columnCount) / columnCount + 1;
        int row2 = (node2.getIndex() - node2.getIndex() % columnCount) / columnCount + 1;
        int col1 = node1.getIndex() % columnCount + 1;
        int col2 = node2.getIndex() % columnCount + 1;

        return Math.abs(row1 - row2) == 1 || Math.abs(col1 - col2) == 1;
    }

    public void readToFile(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(String.format("%d %d\n", rowCount, columnCount));

        for (Node n : nodes) {
            writer.write(n.toString() + "\n");
        }

        writer.close();
    }

    public void calculateSubraphCount() {
        int n = 0;
        BreadthFirstSearch bfs = new BreadthFirstSearch(getNodeCount());

        while (bfs.hasNotVisitedNode()) {
            n++;
            bfs.run(nodes.get(bfs.getNotVisitedNode()));
        }

        subgraphCount = n;
    }

    public void calculateEdgeValueRange() {
        double min = Double.MAX_VALUE;
        double max = -1;
        double edge;

        for (Node n : nodes) {
            if (n.getIndex() % columnCount + 1 != columnCount) {
                if (n.hasConnection(getNode(n.getIndex() + 1))) {
                    edge = n.getEdgeOnConnection(getNode(n.getIndex() + 1));

                    if (edge > max)
                        max = edge;

                    if (edge < min)
                        min = edge;
                }
            }

            if ((n.getIndex() - n.getIndex() % columnCount) / columnCount + 1 != rowCount) {
                if (n.hasConnection(getNode(n.getIndex() + columnCount))) {
                    edge = n.getEdgeOnConnection(getNode(n.getIndex() + columnCount));

                    if (edge > max)
                        max = edge;

                    if (edge < min)
                        min = edge;
                }
            }
        }

        edgeValueRange = (max == -1) ? new Range(0, 0) : new Range(min, max);
    }
}
