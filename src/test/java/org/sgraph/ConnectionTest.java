package org.sgraph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTest {

    @Test
    void getWeight() {
        Connection instance = new Connection(new Node(1), 1.0);
        double expectedValue = 1.0;
        double actualValue = instance.getWeight();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void getNode() {
        Connection instance = new Connection(new Node(1), 1.0);
        Node expectedValue = new Node(1);
        Node actualValue = instance.getNode();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void testToString() {
        Connection instance = new Connection(new Node(1), 1.0);
        String expectedValue = "1:1.0";
        String actualValue = instance.toString();

        assertEquals(expectedValue,actualValue);
    }
}