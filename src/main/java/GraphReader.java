import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class GraphReader {
    public static Graph readFromFile(String path) {
        String[] lineSplit;
        ArrayList<Double> convertedLine;
        Scanner file_scanner = null;

        try {
            file_scanner = new Scanner(new File(path));
        } catch (FileNotFoundException e) {
            System.err.println("GraphReader: File was not found.");
            System.exit(1);
        }

        lineSplit = file_scanner.nextLine().replace(":", " ").split(" ");
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
                lineSplit = file_scanner.nextLine().replace(":", " ").split(" ");
            } catch (NoSuchElementException e) {
                System.err.println("GraphReader: File has less lines than dimensions suggest.");
                System.exit(1);
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
                g.addConnection(g.getNode(i), g.getNode(convertedLine.get(j).intValue()), convertedLine.get(j + 1));
            }
        }
        if (file_scanner.hasNextDouble())
            throw new InputMismatchException("GraphReader: File contains more connection lists than dimensions suggest.");

        return g;
    }
}