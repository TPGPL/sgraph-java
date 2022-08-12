package org.sgraph;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import static org.sgraph.Move.getDirection;
import static org.sgraph.Move.MoveDirection;

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
     * @param nodeIndex indeks wierzchołka
     * @return wierzchołek o podanym indeksie
     * @throws IllegalArgumentException jeżeli podano indeks spoza zakresu wierzchołków
     */
    public Node getNode(int nodeIndex) {
        if (nodeIndex < 0 || nodeIndex >= getNodeCount())
            throw new IllegalArgumentException(String.format("Graph: Cannot get a node of index %d in a %dx%d graph.", nodeIndex, rowCount, columnCount));

        return nodes.get(nodeIndex);
    }

    /**
     * Dodaje połączenie o określonej wadze między dwoma wierzchołkami w grafie.
     *
     * @param firstNodeIndex  indeks pierwszego wierzchołka połączenia
     * @param secondNodeIndex indeks drugiego wierzchołka połączenia
     * @param edge            wartość wagi na krawędzi połączenia
     * @throws IllegalArgumentException jeżeli wierzchołki nie mogą ze sobą sąsiadować w takim grafie, wartość wagi jest niedodatnia, istnieje już połączenie między tymi wierzchołkami o innej wadze
     */
    public void addConnection(int firstNodeIndex, int secondNodeIndex, double edge) throws IllegalArgumentException {
        if (isIndexNotInBounds(firstNodeIndex) || isIndexNotInBounds(secondNodeIndex))
            throw new IllegalArgumentException("Graph: Attempted to remove a connection between nodes of invalid indexes.");

        Node node1 = getNode(firstNodeIndex);
        Node node2 = getNode(secondNodeIndex);

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
     * Usuwa połączenie między dwoma wierzchołkami o podanych indeksach.
     *
     * @param firstNodeIndex  indeks pierwszego wierzchołka połączenia
     * @param secondNodeIndex indeks drugiego wierzchołka połączenia
     */
    public void removeConnection(int firstNodeIndex, int secondNodeIndex) {
        if (isIndexNotInBounds(firstNodeIndex) || isIndexNotInBounds(secondNodeIndex))
            throw new IllegalArgumentException("Graph: Attempted to remove a connection between nodes of invalid indexes.");

        Node node1 = getNode(firstNodeIndex);
        Node node2 = getNode(secondNodeIndex);

        node1.removeConnection(node2);
        node2.removeConnection(node1);
    }

    /**
     * Sprawdza, czy dwa wierzchołki mogą ze sobą sąsiadować w takim grafie.
     *
     * @param firstNode  pierwszy sprawdzany wierzchołek
     * @param secondNode drugi sprawdzany wierzchołek
     * @return true, jeżeli wierzchołki mogą ze sobą sąsiadować; w przeciwnym razie zwraca false
     */
    private boolean canNodesAdhere(Node firstNode, Node secondNode) {
        int row1 = getNodeRowNumber(firstNode.getIndex());
        int row2 = getNodeRowNumber(secondNode.getIndex());

        int col1 = getNodeColumnNumber(firstNode.getIndex());
        int col2 = getNodeColumnNumber(secondNode.getIndex());

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
        double max = 0;
        double edge;

        for (Node n : nodes) {
            if (getNodeColumnNumber(n.getIndex()) != columnCount) {
                edge = n.getEdgeOnConnection(getNode(n.getIndex() + 1));

                if (edge > max)
                    max = edge;

                if (edge != 0 && edge < min)
                    min = edge;

            }

            if (getNodeRowNumber(n.getIndex()) != rowCount) {
                edge = n.getEdgeOnConnection(getNode(n.getIndex() + columnCount));

                if (edge > max)
                    max = edge;

                if (edge != 0 && edge < min)
                    min = edge;
            }
        }

        edgeValueRange = (max == 0) ? new Range(0, 0) : new Range(min, max);
    }

    /**
     * Sprawdza, czy wierzchołek o podanym indeksie nie znajduje się w grafie.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return wartość logiczną, czy wierzchołek o podanym indeksie nie znajduje się w grafie
     */
    private boolean isIndexNotInBounds(int nodeIndex)
    {
        return nodeIndex < 0 || nodeIndex >= getNodeCount();
    }

    /**
     * Zwraca numer wiersza, w której znajduje się wierzchołek o określonym indeksie.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return numer wiersza, w której znajduje się wierzchołek
     */
    private int getNodeRowNumber(int nodeIndex)
    {
        return (nodeIndex - nodeIndex % columnCount) / columnCount + 1;
    }

    /**
     * Zwraca numer kolumny, w której znajduje się wierzchołek o określonym indeksie.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return numer kolumny, w której znajduje się wierzchołek
     */
    private int getNodeColumnNumber(int nodeIndex)
    {
        return nodeIndex % columnCount + 1;
    }

    /**
     * Wydziela w siatce osobny spójny graf w sposób losowy.
     */
    public void split() {
        ArrayList<Integer> way = new ArrayList<>();
        int w, next_w, slice;
        MoveDirection move, next_move;

        Random rand = new Random();

        // find the starting node
        do {
            w = rand.nextInt(getNodeCount());
        } while (getNode(w).getAdherentNumber() == 4 || getNode(w).getAdherentNumber() == 0);

        way.add(w);

        // start creating a slicing path

        do {
            next_w = getNode(w).getConnectedNodes().get(rand.nextInt(getNode(w).getAdherentNumber())).getIndex(); // draws a random adherent node

            if (way.contains(next_w))
                return; // the path crosssed -> dividing starts from the beginning

            way.add(next_w);
            w = next_w;

        } while (getNode(w).getAdherentNumber() == 4);

        // removing connections on path
        if (way.size() == 2) { // if there are only two nodes in path
            if (getNode(way.get(0)).getAdherentNumber() == 1 && getNode(way.get(1)).getAdherentNumber() == 1) { // if they are only connected to each other
                removeConnection(way.get(0), way.get(1));
            } else { // if they are connected to other nodes
                while (getNode(way.get(0)).getAdherentNumber() > 1) {
                    if (getNode(way.get(0)).getConnectedNodes().get(0).getIndex() != way.get(1)) {
                        removeConnection(way.get(0), getNode(way.get(0)).getConnectedNodes().get(0).getIndex());
                    } else {
                        removeConnection(way.get(0), getNode(way.get(0)).getConnectedNodes().get(1).getIndex());
                    }
                }

                while (getNode(way.get(1)).getAdherentNumber() > 1) {
                    if (getNode(way.get(1)).getConnectedNodes().get(1).getIndex() != way.get(0)) {
                        removeConnection(way.get(1), getNode(way.get(1)).getConnectedNodes().get(0).getIndex());
                    } else {
                        removeConnection(way.get(1), getNode(way.get(1)).getConnectedNodes().get(1).getIndex());
                    }
                }
            }
        } else { // path longer than two nodes
            // first step
            w = way.get(0);
            next_w = way.get(1);
            move = getDirection(w, next_w, getColumnCount(), getRowCount());

            if (move == MoveDirection.UP || move == MoveDirection.DOWN) { // slices to the left
                slice = w - 1;
            } else if (move == MoveDirection.LEFT || move == MoveDirection.RIGHT) { // slices to the bottom
                slice = w + getColumnCount();
            } else {
                System.err.println("Graph: An unexpected error occured while slicing the graph into subgraphs.");
                return;
            }

            removeConnection(w, slice);
            w = next_w;

            // the rest of the path
            for (int i = 2; i < way.size(); i++) {
                next_w = way.get(i);
                next_move = getDirection(w, next_w, getColumnCount(), getRowCount());

                if ((next_move == MoveDirection.UP || next_move == MoveDirection.DOWN) && next_move == move) { // slices to the left
                    slice = w - 1;
                } else if ((next_move == MoveDirection.LEFT || next_move == MoveDirection.RIGHT) && next_move == move) { // slices to the bottom
                    slice = w + getColumnCount();
                } else if ((next_move == MoveDirection.UP || next_move == MoveDirection.DOWN)) { // slices to the bottom, then to the left
                    slice = w - 1;
                    removeConnection(w, w + getColumnCount());
                } else if ((next_move == MoveDirection.LEFT || next_move == MoveDirection.RIGHT)) { // slices to the left, then to the bottom
                    slice = 3;
                    removeConnection(w, w - 1);
                } else {
                    System.err.println("Graph: An unexpected error occured while slicing the graph into subgraphs.");
                    return;
                }
                removeConnection(w, slice);
                w = next_w;
                move = next_move;
            }

            // final step
            removeConnection(next_w, slice);
        }
    }
}
