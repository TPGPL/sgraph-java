package org.sgraph;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void getColumnCount() {
        Graph instance = new Graph(3, 4);
        int expectedValue = 3;
        int actualValue = instance.getColumnCount();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void getRowCount() {
        Graph instance = new Graph(3, 4);
        int expectedValue = 4;
        int actualValue = instance.getRowCount();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void getNodeCount() {
        Graph instance = new Graph(3, 4);
        int expectedValue = 12;
        int actualValue = instance.getNodeCount();

        assertEquals(expectedValue, actualValue);
    }
}