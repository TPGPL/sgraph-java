package org.sgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Klasa odpowiadająca za przechowywanie informacji o grafie-siatce i zarządzanie jego elementami.
 */
public class Graph {
    /**
     * Liczba kolumn w siatce.
     */
    private final int columnCount;
    /**
     * Liczba wierszy w siatce.
     */
    private final int rowCount;
    /**
     * Liczba spójnych grafów w siatce.
     */
    private int subgraphCount;
    /**
     * Tablica przechowująca wierzchołki grafu.
     */
    private final ArrayList<Node> nodes;
    /**
     * Zakres w jakim znajdują się wagi na krawędziach w grafie.
     */
    private Range edgeValueRange;

    /**
     * Konstruktor klasy.
     *
     * @param columnCount liczba kolumn w siatce
     * @param rowCount    liczba wierszy w siatce
     * @throws IllegalArgumentException jeżeli liczba kolumn lub wierszy jest niedodatnia
     */
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

    /**
     * Zwraca liczbę kolumn w siatce.
     *
     * @return liczba kolumn w siatce
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Zwraca liczbę wierszy w siatce.
     *
     * @return liczba wierszy w siatce.
     */
    public int getRowCount() {
        return rowCount;
    }

    /**
     * Zwraca liczbę wierzchołków w grafie.
     *
     * @return liczba wierzchołków w grafie
     */
    public int getNodeCount() {
        return rowCount * columnCount;
    }

    /**
     * Zwraca liczbę spójnych grafów w siatce.
     *
     * @return liczba spójnych grafów w siatce
     */
    public int getSubgraphCount() {
        return subgraphCount;
    }

    /**
     * Zwraca zakres wartości wag na krawędziach w grafie.
     *
     * @return zakres wartości wag na krawędziach
     */
    public Range getEdgeValueRange() {
        return edgeValueRange;
    }

    /**
     * Zwraca wierzchołek o podanym indeksie.
     *
     * @param index indeks wierzchołka
     * @return wierzchołek o podanym indeksie
     * @throws IllegalArgumentException jeżeli podano indeks spoza zakresu wierzchołków
     */
    public Node getNode(int index) throws IllegalArgumentException {
        if (index < 0 || index >= getNodeCount())
            throw new IllegalArgumentException(String.format("Graph: Cannot get a node of index %d in a %dx%d graph.", index, rowCount, columnCount));

        return nodes.get(index);
    }

    /**
     * Dodaje połączenie o określonej wadze między dwoma wierzchołkami w grafie.
     *
     * @param node1 pierwszy wierzchołek połączenia
     * @param node2 drugi wierzchołek połączenia
     * @param edge  wartość wagi na krawędzi połączenia
     * @throws IllegalArgumentException jeżeli wierzchołki nie mogą ze sobą sąsiadować w takim grafie, wartość wagi jest niedodatnia, istnieje już połączenie między tymi wierzchołkami o innej wadze
     */
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

    /**
     * Usuwa połączenie między dwoma wierzchołkami.
     *
     * @param node1 pierwszy wierzchołek połączenia
     * @param node2 drugi wierzchołek połączenia
     */
    public void removeConnection(Node node1, Node node2) {
        node1.removeConnection(node2);
        node2.removeConnection(node1);
    }

    /**
     * Sprawdza, czy dwa wierzchołki mogą ze sobą sąsiadować w takim grafie.
     *
     * @param node1 pierwszy sprawdzany wierzchołek
     * @param node2 drugi sprawdzany wierzchołek
     * @return true, jeżeli wierzchołki mogą ze sobą sąsiadować; w przeciwnym razie zwraca false
     */
    private boolean canNodesAdhere(Node node1, Node node2) {
        int row1 = (node1.getIndex() - node1.getIndex() % columnCount) / columnCount + 1;
        int row2 = (node2.getIndex() - node2.getIndex() % columnCount) / columnCount + 1;
        int col1 = node1.getIndex() % columnCount + 1;
        int col2 = node2.getIndex() % columnCount + 1;

        return Math.abs(row1 - row2) == 1 || Math.abs(col1 - col2) == 1;
    }

    /**
     * Zapisuje informacje o grafie do pliku tekstowego o określonym formacie.
     * Pierwsza linia pliku zawiera wymiary grafu, a kolejne zawierają listy sąsiedstwa wszystkich wierzchołków grafu.
     *
     * @param file plik, do którego będą zapisywane informacje
     * @throws IOException jeżeli wystąpił błąd wejścia/wyjścia podczas pisania do pliku
     */
    public void readToFile(File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(String.format("%d %d\n", rowCount, columnCount));

        for (Node n : nodes) {
            writer.write(n.toString() + "\n");
        }

        writer.close();
    }

    /**
     * Oblicza liczbę spójnych grafów w siatce przy użyciu algorytmu przeszukiwania wszerz.
     *
     * @see BreadthFirstSearch
     */
    public void calculateSubraphCount() {
        int n = 0;
        BreadthFirstSearch bfs = new BreadthFirstSearch(getNodeCount());

        while (bfs.hasNotVisitedNode()) {
            n++;
            bfs.run(nodes.get(bfs.getNotVisitedNode()));
        }

        subgraphCount = n;
    }

    /**
     * Oblicza zakres wartości wag na krawędziach w grafie.
     * Jeżeli w grafie nie ma żadnych krawędzi, zakres zostaje ustawiony na [0;0].
     */
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
