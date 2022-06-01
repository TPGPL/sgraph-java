package org.sgraph;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.sgraph.Range;

import static org.junit.jupiter.api.Assertions.*;

class RangeTest {

    @Test
    void getMin() {
        Range instance = new Range(0, 1);
        double expectedValue = 0;
        double actualValue = instance.getMin();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void getMax() {
        Range instance = new Range(0, 1);
        double expectedValue = 1;
        double actualValue = instance.getMax();

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void getHSBValue() {
        Range instance = new Range(0, 1);
        Color expectedValue = Color.hsb(240, 1, 1, 1);
        Color actualValue = instance.getHSBValue(0);

        assertEquals(expectedValue, actualValue);

        instance = new Range(0, 0);
        expectedValue = Color.hsb(0, 1, 1, 1);
        actualValue = instance.getHSBValue(0);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    void testToString() {
        Range instance = new Range(0, 1);
        String expectedValue = 0.0 + " - " + 1.0;
        String actualValue = instance.toString();

        assertEquals(expectedValue, actualValue);
    }
}