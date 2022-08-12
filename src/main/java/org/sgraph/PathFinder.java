package org.sgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Klasa odpowiadająca za działanie algorytmu Dijkstry i wyznaczająca najkrótsze ścieżki do przekazanego wierzchołka.
 */
public class PathFinder {
    /**
     * Graf, w którym będzie działał algorytm Dijkstry.
     */
    private final Graph graph;
    /**
     * Indeks Wierzchołka od którego zaczyna się wyznaczanie najkrótszych ścieżek.
     */
    private final int startNodeIndex;
    /**
     * Tablica najkrótszych odległości do wierzchołka początkowego.
     * Początkowo inicjalizowana jako tablica Double.MAX_VALUE (oznacza brak połączenia/nieprzetworzony wierzchołek).
     */
    private final double[] distanceToNode;
    /**
     * Tablica indeksów poprzedników-wierzchołków algorytmu Dijkstry wykorzystywana do budowana najkrótszych ścieżek.
     * Dla wierzchołka początkowego i wierzchołków niepołączonych, wartość ustalona na -1.
     */
    private final int[] previousNode;
    /**
     * Tablica zawierająca stany przetworzenia wierzchołków przez algorytm Dijkstry.
     */
    private final boolean[] parsedNodes;
    /**
     * Kolejka priorytetowa indeksów wierzchołków do przetworzenia.
     */
    private final ArrayList<Integer> queue;
    /**
     * Zakres wartości najkrótszych odległości połączonych wierzchołków od wierzchołka początkowego.
     */
    private Range nodeValueRange;

    /**
     * Konstruktor klasy
     * Wykorzystuje algorytm BFS do wyznaczenia zawartości kolejki priorytetowej.
     *
     * @param graph          graf, w którym będzie działał algorytm Dijkstry
     * @param startNodeIndex indeks wierzchołka początkowego, od którego rozpoczyna się wyznaczanie najkrótszych ścieżek
     * @throws IllegalArgumentException jeżeli przekazana liczba wierzchołków jest niedodatnia
     * @see BreadthFirstSearch
     */
    public PathFinder(Graph graph, int startNodeIndex)
    {
        this.graph = graph;

        if (startNodeIndex < 0 || startNodeIndex >= graph.getNodeCount())
            throw new IllegalArgumentException(String.format("PathFinder: Invalid starting node index. Allowed range: %d - %d", 0, graph.getNodeCount() - 1));

        this.startNodeIndex = startNodeIndex;

        distanceToNode = new double[graph.getNodeCount()];
        previousNode = new int[graph.getNodeCount()];
        parsedNodes = new boolean[graph.getNodeCount()];
        queue = new ArrayList<>();

        Arrays.fill(distanceToNode, Double.MAX_VALUE);
        Arrays.fill(previousNode, -1); // TODO: is needed?

        // params for the starting node
        queue.add(startNodeIndex);
        distanceToNode[startNodeIndex] = 0;
        previousNode[startNodeIndex] = -1;

        // get connected nodes
        BreadthFirstSearch bfs = new BreadthFirstSearch(graph);
        bfs.run(startNodeIndex);
        queue.addAll(bfs.getConnectedNodeIndexes());
    }

    /**
     * Uruchamia algorytm Dijkstry, rozpoczynając od wierzchołka początkowego, i wyznacza najkrótsze ścieżki w grafie.
     */
    public void run()
    {
        while (!queue.isEmpty()) {
            int parsedNodeIndex = getNodeFromQueue();
            parsedNodes[parsedNodeIndex] = true;

            for (int nodeIndex : graph.getConnectedNodeIndexes(parsedNodeIndex)) {
                if (!parsedNodes[nodeIndex] && (distanceToNode[parsedNodeIndex] + graph.getEdgeOnNodeConnection(parsedNodeIndex, nodeIndex) < distanceToNode[nodeIndex])) {
                    distanceToNode[nodeIndex] = distanceToNode[parsedNodeIndex] + graph.getEdgeOnNodeConnection(parsedNodeIndex, nodeIndex);
                    previousNode[nodeIndex] = parsedNodeIndex;
                }
            }
        }
    }

    /**
     * Priorytetowo wyjmuje z kolejki wierzchołek, którego odległość do wierzchołka początkowego jest najmniejsza.
     *
     * @return indeks wierzchołka, którego odległość do wierzchołka początkowego jest najmniejsza
     */
    private int getNodeFromQueue()
    {
        int minNodeIndex = queue.get(0); // gets first element;

        for (int nodeIndex : queue) {
            if (distanceToNode[nodeIndex] < distanceToNode[nodeIndex])
                minNodeIndex = nodeIndex;
        }

        queue.remove(minNodeIndex);

        return minNodeIndex;
    }

    /**
     * Zwraca odległość  wierzchołka o określonym indeksie od wierzchołka początkowego.
     * Jeżeli wartość odległości jest domyślna (Double.MAX_VALUE), to zwraca -1.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return odległość sprawdzanego wierzchołka od wierzchołka początkowego
     */
    public double getDistanceToNode(int nodeIndex)
    {
        if (nodeIndex < 0 || nodeIndex >= graph.getNodeCount())
            throw new IllegalArgumentException(String.format("PathFinder: Invalid node index. Allowed range: %d - %d", 0, graph.getNodeCount() - 1));

        return distanceToNode[nodeIndex] == Double.MAX_VALUE ? -1 : distanceToNode[nodeIndex];
    }


    /**
     * Zwraca najkrótszą ścieżkę do wierzchołka o określonym indeksie w postaci napisu zawierającego ciąg indeksów wierzchołków.
     * Jeżeli droga nie istnieje, to zwraca null.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return napis zawierający najkrótszą ścieżkę do wierzchołka w postaci ciągu indeksów
     */
    public String getPathToNode(int nodeIndex)
    {
        if (nodeIndex < 0 || nodeIndex >= graph.getNodeCount())
            throw new IllegalArgumentException(String.format("PathFinder: Invalid node index. Allowed range: %d - %d", 0, graph.getNodeCount() - 1));

        if (distanceToNode[nodeIndex] == Double.MAX_VALUE) // no path
            return null;

        String path = "";
        LinkedList<Integer> indexes = getIndexPathToNode(nodeIndex);

        for (int index : indexes) {
            path = path.concat(Integer.toString(index));

            if (index != nodeIndex) path = path.concat(" -> ");
        }

        return path;
    }

    /**
     * Zwraca najkrótszą ścieżkę do wierzchołka o określonym indeksie w postaci listy liniowej indeksów wierzchołków.
     * Jeżeli droga nie istnieje, to zwraca null.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return lista liniowa zawierająca najkrótszą ścieżkę do wierzchołka w postaci indeksów wierzchołków
     */
    public LinkedList<Integer> getIndexPathToNode(int nodeIndex)
    {
        if (nodeIndex < 0 || nodeIndex >= graph.getNodeCount())
            throw new IllegalArgumentException(String.format("PathFinder: Invalid node index. Allowed range: %d - %d", 0, graph.getNodeCount() - 1));

        if (distanceToNode[nodeIndex] == Double.MAX_VALUE) // no path
            return null;

        LinkedList<Integer> indexes = new LinkedList<>();

        int parsedNodeIndex = nodeIndex;

        while (parsedNodeIndex != -1) {
            indexes.addFirst(parsedNodeIndex);
            parsedNodeIndex = previousNode[parsedNodeIndex];
        }

        return indexes;
    }

    /**
     * Oblicza zakres wartości najkrótszych odległości od wierzchołka początkowego.
     * Ignoruje połączenia o wartości domyślnej.
     */
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

    /**
     * Zwraca zakres wartości najkrótszych odległości od wierzchołka początkowego.
     *
     * @return zakres wartości najkrótszych odległości od wierzchołka początkowego
     */
    public Range getNodeValueRange()
    {
        return nodeValueRange;
    }

    /**
     * Zwraca indeks wierzchołka, od którego rozpoczyna się wyznaczanie najkrótszych ścieżek w grafie.
     *
     * @return indeks wierzchołka początkowego
     */
    public int getStartNodeIndex()
    {
        return startNodeIndex;
    }
}
