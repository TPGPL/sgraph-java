package org.sgraph;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Klasa odpowiadająca za działanie algorytmu przeszukiwania wszerz (BFS) dla grafu.
 */
public class BreadthFirstSearch {
    /**
     * Graf przeszukiwany algorytmem BFS.
     */
    private final Graph graph;
    /**
     * Tablica przechowująca informacje o tym, czy wierzchołek o danym indeksie został odwiedzony przez BFS.
     */
    private final boolean[] visitedNodes;
    /**
     * Tablica przechowująca indeksy wierzchołków połączonych z wierzchołkiem, od którego BFS rozpoczął działanie.
     */
    private final ArrayList<Integer> connectedNodeIndexes;
    /**
     * Kolejka FIFO przechowująca wierzchołki, które wymagają odwiedzenia.
     */
    private final LinkedList<Integer> queue;

    /**
     * Konstruktor klasy
     *
     * @param graph graf, w którym będzie działać BFS
     * @throws IllegalArgumentException jeżeli liczba wierzchołków jest niedodatnia
     */
    public BreadthFirstSearch(Graph graph) {
        this.graph = graph;
        visitedNodes = new boolean[graph.getNodeCount()];
        queue = new LinkedList<>();
        connectedNodeIndexes = new ArrayList<>();
    }

    /**
     * Rozpoczyna działanie algorytmu BFS i przechodzi po wszystkich połączonych wierzchołkach wszerz.
     * Dołącza indeksy odwiedzonych wierzchołków do tablicy connectedNodeIndexes i oznacza je jako odwiedzone.
     *
     * @param startNodeIndex indeks wierzchołka, od którego rozpoczyna się działanie algorytmu
     */
    public void run(int startNodeIndex)
    {
        if (startNodeIndex < 0 || startNodeIndex >= graph.getNodeCount())
            throw new IllegalArgumentException(String.format("BreadthFirstSearch: Invalid starting node index. Allowed range: %d - %d", 0, graph.getNodeCount() - 1));

        int parsedNodeIndex;

        queue.add(startNodeIndex);
        visitedNodes[startNodeIndex] = true;

        while (!queue.isEmpty()) {
            parsedNodeIndex = queue.removeFirst();

            for (int nodeIndex : graph.getConnectedNodeIndexes(parsedNodeIndex))
            {
                if (!visitedNodes[nodeIndex])
                {
                    queue.add(nodeIndex);
                    connectedNodeIndexes.add(nodeIndex);
                    visitedNodes[nodeIndex] = true;
                }
            }
        }
    }

    /**
     * Sprawdza, czy w grafie jest jakiś nieodwiedzony wierzchołek.
     *
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
     *
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
     * Zwraca tablicę indeksów wierzchołków połączonych z wierzchołkiem, od którego algorytm BFS rozpoczął działanie.
     *
     * @return tablica indeksów wierzchołków połączonych z wierzchołkiem początkowym
     */
    public ArrayList<Integer> getConnectedNodeIndexes() {
        return connectedNodeIndexes;
    }
}
