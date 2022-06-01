package org.sgraph;

import java.util.ArrayList;
import java.util.Random;

public class GraphGenerator {
    private static final Random r = new Random();

    public enum Move {
        UP, LEFT, RIGHT, DOWN, NO_MOVE
    }

    public static Graph generate(int columnCount, int rowCount, int subgraphCount, double min, double max) {
        Graph g = new Graph(columnCount, rowCount);

        if (subgraphCount <= 0 || subgraphCount > g.getNodeCount())
            throw new IllegalArgumentException("GraphGenerator: The number of subgraphs must be positive and lower than the total number of nodes.");

        if (min < 0 || max <= min)
            throw new IllegalArgumentException("GraphGenerator: Invalid edge value range. MIN must be non-negative and lower than MAX.");

        for (int i = 0; i < g.getNodeCount(); i++) {
            if (i % columnCount + 1 != columnCount) // if node is not in the last column
                g.addConnection(g.getNode(i), g.getNode(i + 1), r.nextDouble(min, max));

            if ((i - i % columnCount) / columnCount + 1 != rowCount) // if node is not in the last row
                g.addConnection(g.getNode(i), g.getNode(i + columnCount), r.nextDouble(min, max));
        }

        g.calculateSubraphCount();

        if (subgraphCount != 1) {
            while (subgraphCount > g.getSubgraphCount()) {
                divide(g);
                g.calculateSubraphCount();
            }
        }

        g.calculateEdgeValueRange();

        return g;
    }

    private static void divide(Graph g) {
        ArrayList<Integer> way = new ArrayList<>();
        int w, next_w, slice;
        Move move, next_move;

        // find the starting node
        do {
            w = r.nextInt(g.getNodeCount());
        } while (g.getNode(w).getAdherentNumber() == 4 || g.getNode(w).getAdherentNumber() == 0);

        way.add(w);

        // start creating a slicing path

        do {
            next_w = g.getNode(w).getConnectedNodes().get(r.nextInt(g.getNode(w).getAdherentNumber())).getIndex(); // draws a random adherent node

            if (way.contains(next_w))
                return; // the path crosssed -> dividing starts from the beginning

            way.add(next_w);
            w = next_w;

        } while (g.getNode(w).getAdherentNumber() == 4);

        // removing connections on path
        if (way.size() == 2) { // if there are only two nodes in path
            if (g.getNode(way.get(0)).getAdherentNumber() == 1 && g.getNode(way.get(1)).getAdherentNumber() == 1) { // if they are only connected to each other
                g.removeConnection(g.getNode(way.get(0)), g.getNode(way.get(1)));
            } else { // if they are connected to other nodes
                while (g.getNode(way.get(0)).getAdherentNumber() > 1) {
                    if (g.getNode(way.get(0)).getConnectedNodes().get(0).getIndex() != way.get(1)) {
                        g.removeConnection(g.getNode(way.get(0)), g.getNode(way.get(0)).getConnectedNodes().get(0));
                    } else {
                        g.removeConnection(g.getNode(way.get(0)), g.getNode(way.get(0)).getConnectedNodes().get(1));
                    }
                }

                while (g.getNode(way.get(1)).getAdherentNumber() > 1) {
                    if (g.getNode(way.get(1)).getConnectedNodes().get(1).getIndex() != way.get(0)) {
                        g.removeConnection(g.getNode(way.get(1)), g.getNode(way.get(1)).getConnectedNodes().get(0));
                    } else {
                        g.removeConnection(g.getNode(way.get(1)), g.getNode(way.get(1)).getConnectedNodes().get(1));
                    }
                }
            }
        } else { // path longer than two nodes
            // first step
            w = way.get(0);
            next_w = way.get(1);
            move = getDirection(w, next_w, g.getColumnCount(), g.getRowCount());

            if (move == Move.UP || move == Move.DOWN) { // slices to the left
                slice = w - 1;
            } else if (move == Move.LEFT || move == Move.RIGHT) { // slices to the bottom
                slice = w + g.getColumnCount();
            } else {
                System.err.println("GraphGenerator: An unexpected error occured while slicing the graph into subgraphs.");
                return;
            }

            g.removeConnection(g.getNode(w), g.getNode(slice));
            w = next_w;

            // the rest of the path
            for (int i = 2; i < way.size(); i++) {
                next_w = way.get(i);
                next_move = getDirection(w, next_w, g.getColumnCount(), g.getRowCount());

                if ((next_move == Move.UP || next_move == Move.DOWN) && next_move == move) { // slices to the left
                    slice = w - 1;
                } else if ((next_move == Move.LEFT || next_move == Move.RIGHT) && next_move == move) { // slices to the bottom
                    slice = w + g.getColumnCount();
                } else if ((next_move == Move.UP || next_move == Move.DOWN)) { // slices to the bottom, then to the left
                    slice = w - 1;
                    g.removeConnection(g.getNode(w), g.getNode(w + g.getColumnCount()));
                } else if ((next_move == Move.LEFT || next_move == Move.RIGHT)) { // slices to the left, then to the bottom
                    slice = 3;
                    g.removeConnection(g.getNode(w), g.getNode(w - 1));
                } else {
                    System.err.println("GraphGenerator: An unexpected error occured while slicing the graph into subgraphs.");
                    return;
                }
                g.removeConnection(g.getNode(w), g.getNode(slice));
                w = next_w;
                move = next_move;
            }

            // final step
            g.removeConnection(g.getNode(next_w), g.getNode(slice));
        }
    }

    public static Move getDirection(int position, int n_position, int columnCount, int rowCount) {
        if (position - columnCount > -1 && position - columnCount == n_position)
            return Move.UP;
        else if (position - 1 == n_position && position / columnCount == n_position / columnCount)
            return Move.LEFT;
        else if (position + 1 == n_position && position / columnCount == n_position / columnCount)
            return Move.RIGHT;
        else if (position + columnCount < rowCount * columnCount && position + columnCount == n_position)
            return Move.DOWN;
        else
            return Move.NO_MOVE;
    }
}
