package org.sgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class PathFinder {
    private final double[] distanceToNode;
    private final Node[] previousNode;
    private final boolean[] parsedNodes;
    private final ArrayList<Node> queue;
    private Range nodeValueRange;

    public PathFinder(int nodeCount, Node startingNode)
    {
        if (nodeCount <= 0) throw new IllegalArgumentException("PathFinder: The node count must be positive.");

        distanceToNode = new double[nodeCount];
        previousNode = new Node[nodeCount];
        parsedNodes = new boolean[nodeCount];
        queue = new ArrayList<>();

        Arrays.fill(distanceToNode, Double.MAX_VALUE);

        // params for the starting node
        queue.add(startingNode);
        distanceToNode[startingNode.getIndex()] = 0;
        previousNode[startingNode.getIndex()] = null;

        // get connected nodes
        BreadthFirstSearch bfs = new BreadthFirstSearch(nodeCount);
        bfs.run(startingNode);
        queue.addAll(bfs.getConnectedNodes());
    }

    public void run()
    {
        while (!queue.isEmpty()) {
            Node currParsedNode = getNodeFromQueue();
            parsedNodes[currParsedNode.getIndex()] = true;

            for (Node n : currParsedNode.getConnectedNodes()) {
                if (!parsedNodes[n.getIndex()] && (distanceToNode[currParsedNode.getIndex()] + currParsedNode.getEdgeOnConnection(n) < distanceToNode[n.getIndex()])) {
                    distanceToNode[n.getIndex()] = distanceToNode[currParsedNode.getIndex()] + currParsedNode.getEdgeOnConnection(n);
                    previousNode[n.getIndex()] = currParsedNode;
                }
            }
        }
    }

    private Node getNodeFromQueue()
    {
        Node minNode = queue.get(0); // gets first element;

        for (Node n : queue) {
            if (distanceToNode[n.getIndex()] < distanceToNode[minNode.getIndex()]) minNode = n;
        }

        queue.remove(minNode);

        return minNode;
    }

    public double getDistanceToNode(Node n)
    {
        return distanceToNode[n.getIndex()] == Double.MAX_VALUE ? -1 : distanceToNode[n.getIndex()];
    }

    public String getPathToNode(Node n)
    {
        if (distanceToNode[n.getIndex()] == Double.MAX_VALUE) // no path
            return null;

        String path = "";
        LinkedList<Integer> indexes = new LinkedList<>();
        Node parsedNode = n;

        while (parsedNode != null) {
            indexes.addFirst(parsedNode.getIndex());
            parsedNode = previousNode[parsedNode.getIndex()];
        }

        for (int index : indexes) {
            path = path.concat(Integer.toString(index));

            if (index != n.getIndex()) path = path.concat(" -> ");
        }

        return path;
    }

    public void calculateNodeValueRange()
    {
        double minValue = Double.MAX_VALUE;
        double maxValue = -1;

        for (double d : distanceToNode) {
            if (d == Double.MAX_VALUE)
                continue;

            if (d < minValue)
                minValue = d;

            if (d > maxValue)
                maxValue = d;
        }

        nodeValueRange = new Range(minValue, maxValue);
    }

    public Range getNodeValueRange()
    {
        return nodeValueRange;
    }
}
