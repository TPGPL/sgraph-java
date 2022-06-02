package org.sgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Klasa odpowiadająca za działanie algorytmu Dijkstry i wyznaczająca najkrótsze ścieżki do przekazanego wierzchołka.
 */
public class PathFinder {
    /**
     * Wierzchołek od którego zaczyna się wyznaczanie najkrótszych ścieżek.
     */
    private final Node startingNode;
    /**
     * Tablica najkrótszych odległości do wierzchołka początkowego.
     * Początkowo inicjalizowana jako tablica Double.MAX_VALUE (oznacza brak połączenia/nieprzetworzony wierzchołek).
     */
    private final double[] distanceToNode;
    /**
     * Tablica poprzedników-wierzchołków algorytmu Dijkstry wykorzystywana do budowana najkrótszych ścieżek.
     * Dla wierzchołka początkowego i wierzchołków niepołączonych, wartość ustalona na null.
     */
    private final Node[] previousNode;
    /**
     * Tablica zawierająca stany przetworzenia wierzchołków przez algorytm Dijkstry.
     */
    private final boolean[] parsedNodes;
    /**
     * Kolejka priorytetowa wierzchołków do przetworzenia.
     */
    private final ArrayList<Node> queue;
    /**
     * Zakres wartości najkrótszych odległości połączonych wierzchołków od wierzchołka początkowego.
     */
    private Range nodeValueRange;

    /**
     * Konstruktor klasy
     * Wykorzystuje algorytm BFS do wyznaczenia zawartości kolejki priorytetowej.
     *
     * @param nodeCount    liczba wierzchołków w grafie
     * @param startingNode wierzchołek początkowy od którego rozpoczyna się wyznaczanie najkrótszych ścieżek
     * @throws IllegalArgumentException jeżeli przekazana liczba wierzchołków jest niedodatnia
     * @see BreadthFirstSearch
     */
    public PathFinder(int nodeCount, Node startingNode)
    {
        if (nodeCount <= 0) throw new IllegalArgumentException("PathFinder: The node count must be positive.");

        this.startingNode = startingNode;
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

    /**
     * Uruchamia algorytm Dijkstry, rozpoczynając od wierzchołka początkowego, i wyznacza najkrótsze ścieżki w grafie.
     */
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

    /**
     * Priorytetowo wyjmuje z kolejki wierzchołek, którego odległość do wierzchołka początkowego jest najmniejsza.
     *
     * @return wierzchołek, którego odległość do wierzchołka początkowego jest najmniejsza
     */
    private Node getNodeFromQueue()
    {
        Node minNode = queue.get(0); // gets first element;

        for (Node n : queue) {
            if (distanceToNode[n.getIndex()] < distanceToNode[minNode.getIndex()]) minNode = n;
        }

        queue.remove(minNode);

        return minNode;
    }

    /**
     * Zwraca odległość przekazanego wierzchołka od wierzchołka początkowego.
     * Jeżeli wartość odległości jest domyślna (Double.MAX_VALUE), to zwraca -1.
     *
     * @param n sprawdzany wierzchołek
     * @return odległość sprawdzanego wierzchołka od wierzchołka początkowego
     */
    public double getDistanceToNode(Node n)
    {
        return distanceToNode[n.getIndex()] == Double.MAX_VALUE ? -1 : distanceToNode[n.getIndex()];
    }


    /**
     * Zwraca najkrótszą ścieżkę do przekazanego wierzchołka w postaci napisu zawierającego ciąg indeksów wierzchołków.
     * Jeżeli droga nie istnieje, to zwraca null.
     *
     * @param n sprawdzany wierzchołek
     * @return napis zawierający najkrótszą ścieżkę do wierzchołka w postaci ciągu indeksów
     */
    public String getPathToNode(Node n)
    {
        if (distanceToNode[n.getIndex()] == Double.MAX_VALUE) // no path
            return null;

        String path = "";
        LinkedList<Integer> indexes = getIndexPathToNode(n);

        for (int index : indexes) {
            path = path.concat(Integer.toString(index));

            if (index != n.getIndex()) path = path.concat(" -> ");
        }

        return path;
    }

    /**
     * Zwraca najkrótszą ścieżkę do przekazanego wierzchołka w postaci listy liniowej indeksów wierzchołków.
     * Jeżeli droga nie istnieje, to zwraca null.
     *
     * @param n sprawdzany wierzchołek
     * @return lista liniowa zawierająca najkrótszą ścieżkę do wierzchołka w postaci indeksów wierzchołków
     */
    public LinkedList<Integer> getIndexPathToNode(Node n)
    {
        if (distanceToNode[n.getIndex()] == Double.MAX_VALUE) // no path
            return null;

        LinkedList<Integer> indexes = new LinkedList<>();

        Node parsedNode = n;

        while (parsedNode != null) {
            indexes.addFirst(parsedNode.getIndex());
            parsedNode = previousNode[parsedNode.getIndex()];
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
     * Zwraca wierzchołek, od którego rozpoczyna się wyznaczanie najkrótszych ścieżek w grafie.
     *
     * @return wierzchołek początkowy
     */
    public Node getStartingNode()
    {
        return startingNode;
    }
}
