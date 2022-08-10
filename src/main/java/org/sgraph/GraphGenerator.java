package org.sgraph;

import java.util.Random;

/**
 * Klasa zawierająca statyczne metody pozwalające na wygenerowanie grafu na podstawie parametrów wejściowych.
 */
public class GraphGenerator {
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
                graph.split();
                graph.calculateSubraphCount();
            }
        }

        graph.calculateEdgeValueRange();

        return graph;
    }
}
