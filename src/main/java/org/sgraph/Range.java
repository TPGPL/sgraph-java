package org.sgraph;

import javafx.scene.paint.Color;

/**
 * Klasa przechowująca informacje o zakresie wartości.
 */
public class Range {
    /**
     * Lewa granica zakresu wartości.
     */
    private final double min;
    /**
     * Prawa granica zakresu wartości.
     */
    private final double max;

    /**
     * Konstruktor klasy
     *
     * @param min lewa granica zakresu wartości
     * @param max prawa granica zakresu wartości
     * @throws IllegalArgumentException jeżeli lewa granica zakresu jest ujemna
     */
    public Range(double min, double max) {
        if (min < 0)
            throw new IllegalArgumentException("Range: MIN must be non-negative.");

        this.min = min;
        this.max = max;
    }

    /**
     * Zwraca lewą granicę zakresu wartości.
     *
     * @return lewa granica zakresu wartości
     */
    public double getMin() {
        return min;
    }

    /**
     * Zwraca prawą granicę zakresu wartości.
     *
     * @return prawa granica zakresu wartości
     */
    public double getMax() {
        return max;
    }

    /**
     * Zwraca reprezentację wartości liczbowej w zakresie jako kolor ze skali od niebieskiego do czerwonego.
     * Jeżeli granice zakresu wartości są równe, zwraca kolor czarny.
     *
     * @param value wartośc liczbowa, dla której zwracany jest kolor ze skali
     * @return kolor z zakresu od niebieskiego do czerwonego
     * @throws IllegalArgumentException jeżeli wartość liczbowa nie należy do zakresu wartości
     */
    public Color getHSBValue(double value)
    {
        if (value < min || value > max)
            throw new IllegalArgumentException("Range: Value must be in range bounds.");

        if (min == max) // prevents 0-0 range as well
            return Color.hsb(0, 1, 1, 1);

        return Color.hsb((1 - (value - min) / (max - min)) * 240, 1, 1, 1);
    }

    /**
     * Zwraca napis postaci "[MIN] - [MAX]" reprezentujący aktualny stan obiektu.
     *
     * @return napis reprezentujący zakres wartości
     */
    @Override
    public String toString() {
        return min + " - " + max;
    }
}
