package org.sgraph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NodeTest {

    @Test
    void getIndex() {
        Node instance = new Node(12);
        int expectedValue = 12;
        int actualValue = instance.getIndex();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void addConnection() {
        Node instance = new Node(0);
        instance.addConnection(new Node(2), 1.0);
        boolean expectedValue = true;
        boolean actualValue = instance.hasConnection(new Node(2));

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void removeConnection() {
        Node instance = new Node(0);
        instance.addConnection(new Node(2), 1.0);
        instance.removeConnection(new Node(2));
        boolean expectedValue = false;
        boolean actualValue = instance.hasConnection(new Node(2));

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void hasConnection() {
        Node instance = new Node(0);
        instance.addConnection(new Node(2), 1.0);

        boolean expectedValue = true;
        boolean actualValue = instance.hasConnection(new Node(2));

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void getAdherentNumber() {
        Node instance = new Node(1);
        instance.addConnection(new Node(2), 1.0);
        instance.addConnection(new Node(3), 3.0);
        instance.addConnection(new Node(4), 2.0);
        int expectedValue = 3;
        int actualValue = instance.getAdherentNumber();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void getEdgeOnConnection() {
        Node instance = new Node(1);
        instance.addConnection(new Node(3), 3.0);
        double expectedValue = 3.0;
        double actualValue = instance.getEdgeOnConnection(new Node(3));

        assertEquals(expectedValue, actualValue);
    }
}