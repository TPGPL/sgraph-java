package org.sgraph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Klasa zawierająca statyczne metody pozwalające na wczytanie grafu z pliku wejściowego.
 */
public class GraphReader {

    /**
     * Wczytuje graf z pliku wejściowego o określonym formacie.
     *
     * @param file plik wejściowy
     * @return graf wczytany z pliku wejściowego
     * @throws IOException            jeżeli wystąpił błąd z czytaniem pliku lub nie udało się go otworzyć
     * @throws InputMismatchException jeżeli wymiary grafu lub liczba danych w jednej linii jest niepoprawna
     * @throws NoSuchElementException jeżeli w pliku jest mniej linii niż wynika z wczytanych wymiarów grafu
     */
    public static Graph readFromFile(File file) throws IOException {
        String[] lineSplit;
        ArrayList<Double> convertedLine;
        Scanner file_scanner;

        try {
            file_scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            System.err.println("GraphReader: " + e.getMessage());
            throw new FileNotFoundException("GraphReader: " + e.getMessage());
        }

        lineSplit = file_scanner.nextLine().split("\\s+"); // get dimensions
        convertedLine = new ArrayList<>();

        for (String s : lineSplit) {
            try {
                convertedLine.add(Double.parseDouble(s));
            } catch (NumberFormatException ignored) {
            }
        }

        if (convertedLine.size() != 2)
            throw new InputMismatchException("GraphReader: Incorrect graph dimensions format.");

        Graph g = new Graph(convertedLine.get(1).intValue(), convertedLine.get(0).intValue());

        for (int i = 0; i < g.getNodeCount(); i++) {
            try {
                lineSplit = file_scanner.nextLine().replace(":", " ").split("\\s+");
            } catch (NoSuchElementException e) {
                throw new NoSuchElementException("GraphReader: File has less lines than dimensions suggest.");
            }

            convertedLine.clear();

            for (String s : lineSplit) {
                try {
                    convertedLine.add(Double.parseDouble(s));
                } catch (NumberFormatException ignored) {
                }
            }

            if (convertedLine.size() % 2 != 0)
                throw new InputMismatchException("GraphReader: Incorrect node connection values in line " + (i + 1));

            for (int j = 0; j < convertedLine.size(); j += 2) {
                g.addConnection(i, convertedLine.get(j).intValue(), convertedLine.get(j + 1));
            }
        }
        if (file_scanner.hasNextDouble())
            throw new InputMismatchException("GraphReader: File contains more connection lists than dimensions suggest.");

        g.calculateSubraphCount();
        g.calculateEdgeValueRange();

        return g;
    }
}
