package org.sgraph;

/**
 * Klasa przechowująca informacje o ważonym połączeniu z określonym wierzchołkiem w grafie.
 */
public class Connection {
    /**
     * Wierzchołek, z którym istnieje połączenie.
     */
    private final Node node;
    /**
     * Wartość wagi na krawędzi połączenia z wierzchołkiem.
     */
    private final double weight;

    /**
     * Konstruktor klasy
     *
     * @param node   wierzchołek, z którym istnieje połączenie
     * @param weight wartość wagi na krawędzi połączenia
     * @throws IllegalArgumentException jeżeli wartość wagi na krawędzi połączenia jest niedodatnia
     */
    public Connection(Node node, double weight)
    {
        if (weight <= 0)
            throw new IllegalArgumentException("Connection: Edge value must be positive.");

        this.node = node;
        this.weight = weight;
    }

    /**
     * Zwraca wartość wagi na krawędzi połączenia.
     *
     * @return wartość wagi na krawędzi połączenia
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Zwraca wierzchołek, z którym istnieje połączenie.
     *
     * @return wierzchołek, z którym istnieje połączenie
     */
    public Node getNode()
    {
        return node;
    }

    /**
     * Sprawdza, czy przekazany obiekt jest równy obiektowi klasy.
     * Sprawdzenie odbywa się poprzez porównanie typów obiektów, a jeżeli są równe, to poprzez indeksy wierzchołków, z którymi istnieje połączenie.
     *
     * @param obj porównywany obiekt
     * @return true, jeżeli obiekty są równe; w przeciwnym razie - false
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Connection co)
            return co.node.getIndex() == this.node.getIndex(); // comparing only through node index to simplify implementation

        return false;
    }

    /**
     * Zwraca napis postaci "[indeks wierzchołka]:[waga]" reprezentujący aktualny stan obiektu.
     *
     * @return napis reprezentujący połączenie
     */
    @Override
    public String toString()
    {
        return node.getIndex() + ":" + weight;
    }
}
