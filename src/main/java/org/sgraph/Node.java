package org.sgraph;

import java.util.ArrayList;

/**
 * Klasa odpowiadająca za przechowywanie informacji o wierzchołku i zarządzanie nim.
 */
public class Node {
    /**
     * Indeks wierzchołka.
     */
    private final int index;
    /**
     * Tablica zawierająca połączenia wierzchołka
     */
    private final ArrayList<Connection> connections;

    /**
     * Konstruktor klasy
     *
     * @param index indeks wierzchołka
     */
    public Node(int index)
    {
        this.index = index;
        connections = new ArrayList<>();
    }

    /**
     * Zwraca indeks wierzchołka.
     *
     * @return indeks wierzchołka
     */
    public int getIndex()
    {
        return index;
    }

    /**
     * Dodaje połączenie z określonym wierzchołkiem o określonej wartości wagi na krawędzi połączenia.
     *
     * @param node wierzchołek, z którym powstanie połączenie
     * @param edge wartość wagi na krawędzi połączenia
     */
    public void addConnection(Node node, double edge)
    {
        connections.add(new Connection(node, edge));
    }

    /**
     * Usuwa połączenie z określonym wierzchołkiem.
     *
     * @param node wierzchołek, z którym zostanie usunięte połączenie
     */
    public void removeConnection(Node node)
    {
        connections.remove(new Connection(node, 1));
    }

    /**
     * Sprawdza, czy istnieje połączenie z przekazanym wierzchołkiem.
     *
     * @param node sprawdzany wierzchołek
     * @return true, jeżeli istnieje połączenie z przekazanym wierzchołkiem; w przeciwnym razie, zwraca false
     */
    public boolean hasConnection(Node node)
    {
        return connections.contains(new Connection(node, 1));
    }

    /**
     * Zwraca liczbę istniejących połączeń z wierzchołkiem.
     *
     * @return liczba istniejących połączeń
     */
    public int getAdherentNumber()
    {
        return connections.size();
    }

    /**
     * Zwraca wartość wagi na krawędzi połączenia z przekazanym wierzchołkiem.
     * Jeżeli nie istnieje połączenie z wierzchołkiem, zwraca 0.
     *
     * @param node sprawdzany wierzchołek
     * @return wartość wagi na krawędzi połączenia z wierzchołkiem
     */
    public double getEdgeOnConnection(Node node)
    {
        int index = connections.indexOf(new Connection(node, 1));


        return (index == -1) ? 0 : connections.get(index).getWeight();
    }

    /**
     * Zwraca tablicę wierzchołków, z którymi istnieje połączenie.
     *
     * @return tablica wierzchołków, z którymi istnieje połączenie
     */
    public ArrayList<Node> getConnectedNodes()
    {
        ArrayList<Node> connectedNodes = new ArrayList<>();

        for (Connection c : connections)
            connectedNodes.add(c.getNode());

        return connectedNodes;
    }

    /**
     * Zwraca tablicę indeksów wierzchołków, z którymi istnieje połączenie.
     *
     * @return tablica indeksów wierzchołków, z którymi istnieje połączenie
     */
    public ArrayList<Integer> getConnectedNodeIndexes()
    {
        ArrayList<Integer> nodeIndexes = new ArrayList<>();

        for (Connection c : connections)
            nodeIndexes.add(c.getNode().getIndex());

        return nodeIndexes;
    }

    /**
     * Zwraca ciąg napisów reprezentujących połączenia oddzielonych spacją reprezentujący aktualny stan obiektu.
     *
     * @return napis reprezentujący wierzchołek
     */
    @Override
    public String toString()
    {
        String text = "\t\t";

        for (Connection c : connections)
            text = text.concat(c.toString() + " ");

        return text;
    }

    /**
     * Sprawdza, czy przekazany obiekt jest równy obiektowi klasy.
     * Sprawdzenie odbywa się poprzez porównanie typów obiektów, a jeżeli są równe, to poprzez indeksy wierzchołków.
     *
     * @param obj porównywany obiekt
     * @return true, jeżeli obiekty są równe; w przeciwnym razie - false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Node n)
            return n.getIndex() == this.getIndex();

        return false;
    }
}
