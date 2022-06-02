package org.sgraph;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Klasa odpowiadająca za działanie algorytmu przeszukiwania wszerz (BFS) dla grafu.
 */
public class BreadthFirstSearch {
    /**
     * Tablica przechowująca informacje o tym, czy wierzchołek o danym indeksie został odwiedzony przez BFS.
     */
    private final boolean[] visitedNodes;
    /**
     * Tablica przechowująca wierzchołki połączone z wierzchołkiem, od którego BFS rozpoczął działanie.
     */
    private final ArrayList<Node> connectedNodes;
    /**
     * Kolejka FIFO przechowująca wierzchołki, które wymagają odwiedzenia.
     */
    private final LinkedList<Node> queue;

    /**
     * Konstruktor klasy
     * @param nodeNumber liczba wierzchołków w grafie
     * @throws IllegalArgumentException jeżeli liczba wierzchołków jest niedodatnia
     */
    public BreadthFirstSearch(int nodeNumber) {
        if (nodeNumber <= 0)
            throw new IllegalArgumentException("BreadthFirstSearch: The number of nodes in graph must be positive.");

        visitedNodes = new boolean[nodeNumber];
        queue = new LinkedList<>();
        connectedNodes = new ArrayList<>();
    }

    /**
     * Rozpoczyna działanie algorytmu BFS i przechodzi po wszystkich połączonych wierzchołkach wszerz.
     * Dołącza odwiedzone wierzchołki do tablicy connectedNodes i oznacza je jako odwiedzone.
     * @param startNode wierzchołek od którego rozpoczyna się działanie algorytmu
     */
    public void run(Node startNode)
    {
        Node parsedNode;

        queue.add(startNode);
        visitedNodes[startNode.getIndex()] = true;

        while (!queue.isEmpty()) {
            parsedNode = queue.removeFirst();

            for (Node n : parsedNode.getConnectedNodes()) {
                if (!visitedNodes[n.getIndex()]) {
                    queue.add(n);
                    connectedNodes.add(n);
                    visitedNodes[n.getIndex()] = true;
                }
            }
        }
    }

    /**
     * Sprawdza, czy w grafie jest jakiś nieodwiedzony wierzchołek.
     * @return true, jeżeli jakiś wierzchołek nie został odwiedzony, w przeciwnym razie false
     */
    public boolean hasNotVisitedNode()
    {
        for (boolean visitedNode : visitedNodes) {
            if (!visitedNode)
                return true;
        }

        return false;
    }

    /**
     * Zwraca indeks nieodwiedzonego wierzchołka o najmniejszym indeksie.
     * Jeżeli nie ma takiego wierzchołka, zwraca -1.
     * @return indeks nieodwiedzonego wierzchołka
     */
    public int getNotVisitedNode()
    {
        for (int i = 0; i < visitedNodes.length; i++)
            if (!visitedNodes[i])
                return i;

        return -1;
    }

    /**
     * Zwraca tablicę wierzchołków połączonych z wierzchołkiem, od którego algorytm BFS rozpoczął działanie.
     * @return tablica wierzchołków połączonych z wierzchołkiem początkowym
     */
    public ArrayList<Node> getConnectedNodes() {
        return connectedNodes;
    }
}
