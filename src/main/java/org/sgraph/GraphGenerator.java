package org.sgraph;

import java.util.ArrayList;
import java.util.Random;

/**
 * Klasa zawierająca statyczne metody pozwalające na wygenerowanie grafu na podstawie parametrów wejściowych.
 */
public class GraphGenerator {
    /**
     * Generator liczb pseudolosowych.
     */
    private static final Random r = new Random();

    /**
     * Typ wyliczeniowy reprezentujący możliwe przejścia między wierzchołkami w grafie.
     */
    public enum Move {
        /**
         * Ruch w górę.
         */
        UP,
        /**
         * Ruch w lewo.
         */
        LEFT,
        /**
         * Ruch w prawo.
         */
        RIGHT,
        /**
         * Ruch w dół.
         */
        DOWN,
        /**
         * Brak ruchu.
         */
        NO_MOVE
    }

    /**
     * Generuje graf-siatkę na podstawie podanych parametrów wejściowych
     *
     * @param columnCount   liczba kolumn w siatce
     * @param rowCount      liczba wierszy w siatce
     * @param subgraphCount liczba spójnych grafów w siatce
     * @param min           lewa granica zakresu wartości wag na krawędziach
     * @param max           prawa granica zakresu wartości wag na krawędziach
     * @return graf wygenerowany na podstawie danych wejściowych
     * @throws IllegalArgumentException jeżeli liczba spójnych grafów jest niedodatnia lub większa od liczby wierzchołków, MIN jest ujemne lub mniejsze od MAX
     */
    public static Graph generateGraph(int columnCount, int rowCount, int subgraphCount, double min, double max) {
        Graph graph = new Graph(columnCount, rowCount);
        Random rand = new Random();
        Range edgeRange = new Range(min, max);

        if (subgraphCount <= 0 || subgraphCount > graph.getNodeCount())
            throw new IllegalArgumentException("GraphGenerator: The number of subgraphs must be positive and lower than the total number of nodes.");

        if (min == max)
            throw new IllegalArgumentException("GraphGenerator: Invalid edge value range. MIN must not be equal to MAX.");

        for (int i = 0; i < graph.getNodeCount(); i++) {
            if (i % columnCount + 1 != columnCount) // if node is not in the last column
                graph.addConnection(i, i + 1, rand.nextDouble(edgeRange.getMin(), edgeRange.getMax()));

            if ((i - i % columnCount) / columnCount + 1 != rowCount) // if node is not in the last row
                graph.addConnection(i, i + columnCount, rand.nextDouble(edgeRange.getMin(), edgeRange.getMax()));
        }

        graph.calculateSubraphCount();

        if (subgraphCount != 1) {
            while (subgraphCount > graph.getSubgraphCount()) {
                divide(graph);
                graph.calculateSubraphCount();
            }
        }

        graph.calculateEdgeValueRange();

        return graph;
    }

    /**
     * Wydziela w siatce osobny spójny graf w sposób losowy.
     *
     * @param g graf do podzielenia na mniejsze fragmenty
     */
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
                g.removeConnection(way.get(0), way.get(1));
            } else { // if they are connected to other nodes
                while (g.getNode(way.get(0)).getAdherentNumber() > 1) {
                    if (g.getNode(way.get(0)).getConnectedNodes().get(0).getIndex() != way.get(1)) {
                        g.removeConnection(way.get(0), g.getNode(way.get(0)).getConnectedNodes().get(0).getIndex());
                    } else {
                        g.removeConnection(way.get(0), g.getNode(way.get(0)).getConnectedNodes().get(1).getIndex());
                    }
                }

                while (g.getNode(way.get(1)).getAdherentNumber() > 1) {
                    if (g.getNode(way.get(1)).getConnectedNodes().get(1).getIndex() != way.get(0)) {
                        g.removeConnection(way.get(1), g.getNode(way.get(1)).getConnectedNodes().get(0).getIndex());
                    } else {
                        g.removeConnection(way.get(1), g.getNode(way.get(1)).getConnectedNodes().get(1).getIndex());
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

            g.removeConnection(w, slice);
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
                    g.removeConnection(w, w + g.getColumnCount());
                } else if ((next_move == Move.LEFT || next_move == Move.RIGHT)) { // slices to the left, then to the bottom
                    slice = 3;
                    g.removeConnection(w, w - 1);
                } else {
                    System.err.println("GraphGenerator: An unexpected error occured while slicing the graph into subgraphs.");
                    return;
                }
                g.removeConnection(w, slice);
                w = next_w;
                move = next_move;
            }

            // final step
            g.removeConnection(next_w, slice);
        }
    }

    /**
     * Zwraca kierunek przejścia z jednego wierzchołka do drugiego.
     *
     * @param position    indeks wierzchołka przed przejściem
     * @param n_position  indeks wierzchołka po przejściu
     * @param columnCount liczba kolumn w siatce
     * @param rowCount    liczba wierszy w siatce
     * @return kierunek przejścia w postaci elementu typu wyliczającego Move
     */
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
