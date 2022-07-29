package org.sgraph;

/**
 * Klasa zawierająca stałe wymagane dla GUI programu.
 */
public class Properties {
    /**
     * Szerokość okna aplikacji.
     */
    public static final int WINDOW_WIDTH = 700;
    /**
     * Wysokość okna aplikacji.
     */
    public static final int WINDOW_HEIGHT = 900;
    /**
     * Wymiar kwadratowej przestrzeni na rysowanie grafu.
     */
    public static final int CANVAS_RESOLUTION = 700;
    /**
     * Odległość między elementami interfejsu graficznego.
     */
    public static final double PADDING = 10.0;
    /**
     * Wysokość elementów interfejsu graficznego (przyciski, pola tekstowe).
     */
    public static final int ITEM_HEIGHT = 30;
    /**
     * Domyslna liczba kolumn w siatce.
     */
    public static final int DEFAULT_COLUMN_COUNT = 10;
    /**
     * Domyślna liczba wierszy w siatce.
     */
    public static final int DEFAULT_ROW_COUNT = 10;
    /**
     * Domyślna liczba spójnych grafów w siatce.
     */
    public static final int DEFAULT_SUBGRAPH_COUNT = 1;
    /**
     * Domyślny zakres wartości wag na krawędziach.
     */
    public static final String DEFAULT_WEIGHT_RANGE = "0-1";
    /**
     * Stosunek szerokości krawędzi do promienia wierzchołka.
     */
    public static final double LINE_WIDTH_PROPORTION = 2.0 / 3.0;
    /**
     * Stosunek długości krawędzi do promienia wierzchołka.
     */
    public static final double LINE_LENGTH_PROPORTION = 4.0;
    /**
     * Domyślna nazwa pliku wyjściowego.
     */
    public static final String DEFAULT_FILE_NAME = "graph.txt";
}
