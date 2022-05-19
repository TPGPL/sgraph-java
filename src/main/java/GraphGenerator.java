import java.util.ArrayList;
import java.util.Random;

public class GraphGenerator {
    private static final Random r = new Random();

    public static Graph generate(int columnCount, int rowCount, int subgraphCount, double min, double max) {
        Graph g = new Graph(columnCount, rowCount, subgraphCount, min, max);

        for (int i = 0; i < g.getNodeCount(); i++) {
            if (i % columnCount + 1 != columnCount) // if node is not in the last column
                g.addConnection(g.getNode(i), g.getNode(i + 1), r.nextDouble(g.getEdgeValueRange().getMin(), g.getEdgeValueRange().getMax()));

            if ((i - i % columnCount) / columnCount + 1 != rowCount) // if node is not in the last row
                g.addConnection(g.getNode(i), g.getNode(i + columnCount), r.nextDouble(g.getEdgeValueRange().getMin(), g.getEdgeValueRange().getMax()));
        }

        if (subgraphCount != 1) {
            int limit = g.getSubgraphCount();
            g.calculateSubraphCount();

            while (limit > g.getSubgraphCount()) {
                divide(g);
                g.calculateSubraphCount();
            }
        }
        return g;
    }

    private static void divide(Graph g) {
        ArrayList<Integer> way = new ArrayList<>();
        int control, w, next_w, move, next_move, slice = 0;
        //Znalezienie początku
        do {
            w = r.nextInt(g.getNodeCount());
        } while (g.getNode(w).getAdherentNumber() == 4 || g.getNode(w).getAdherentNumber() == 0);
        way.add(w);
        //Tworzenie ścieżki
        control = 0; //Służy do sprawdzenia, czy kod nie wykonuję się za długo
        do {
            //wyciągam losowy sąsiedni wierzchołek
            next_w = g.getNode(w).getConnectedNodes().get(r.nextInt(g.getNode(w).getAdherentNumber())).getIndex();
            if (!way.contains(next_w)) {
                way.add(next_w);
                w = next_w;
                control = 0;
            } else
                control++;
        } while (g.getNode(w).getAdherentNumber() == 4 && control < 40);

        if (control == 40) //Pętla nieskończona
            return;
        //Cięcie
        //try i catch wyłapuje czy w danym przypadku nie wystąpił początek albo koniec, gdzie może nie mieć co ciąć
        //Co jeśli są tylko dwa punkty
        if (way.size() == 2) {
            if (g.getNode(way.get(0)).getAdherentNumber() == 1 && g.getNode(way.get(1)).getAdherentNumber() == 1) { //Jeśli są połączone tylko ze sobą
                g.removeConnection(g.getNode(way.get(0)), g.getNode(way.get(1)));
            } else { //Jeśli istnieją połączenia z innymi punktami
                while (g.getNode(way.get(0)).getAdherentNumber() > 1) {
                    if (g.getNode(way.get(0)).getConnectedNodes().get(0).getIndex() != way.get(1)) {
                        g.removeConnection(g.getNode(way.get(0)), g.getNode(way.get(0)).getConnectedNodes().get(0)); //Usuwa pierwszego z listy
                    } else {
                        g.removeConnection(g.getNode(way.get(0)), g.getNode(way.get(0)).getConnectedNodes().get(1)); //Chyba, że to ten drugi, wtedy bierze kolejnego
                    }
                }
                while (g.getNode(way.get(1)).getAdherentNumber() > 1) {
                    if (g.getNode(way.get(1)).getConnectedNodes().get(1).getIndex() != way.get(0)) {
                        g.removeConnection(g.getNode(way.get(1)), g.getNode(way.get(1)).getConnectedNodes().get(0)); //Usuwa pierwszego z listy
                    } else {
                        g.removeConnection(g.getNode(way.get(1)), g.getNode(way.get(1)).getConnectedNodes().get(1)); //Chyba, że to ten drugi, wtedy bierze kolejnego
                    }
                }
            }
        } else {
            //Droga dłuższa niż dwa
            //Pierwszy krok
            w = way.get(0);
            next_w = way.get(1);
            move = g.getDirection(w, next_w);
            if (move == 0 || move == 3) { //Tnie na lewo
                slice = w - 1;
            } else if (move == 1 || move == 2) { //Tnie od dołu
                slice = w + g.getColumnCount();
            } else {
                System.err.println("Błąd przy tworzeniu ścieżki tnącej. Nie powinien w ogóle wystąpić!");
                System.exit(1);
            }
            g.removeConnection(g.getNode(w), g.getNode(slice));
            w = next_w;
            //Reszta ścieżki
            for (int i = 2; i < way.size(); i++) {
                next_w = way.get(i);
                next_move = g.getDirection(w, next_w);
                if ((next_move == 0 || next_move == 3) && next_move == move) { //Tnie na lewo
                    slice = w - 1;
                } else if ((next_move == 1 || next_move == 2) && next_move == move) { //Tnie od dołu
                    slice = w + g.getColumnCount();
                } else if ((next_move == 0 || next_move == 3)) { //Tnie na lewo, ale wcześniej też od dołu
                    slice = w - 1;
                    g.removeConnection(g.getNode(w), g.getNode(w + g.getColumnCount()));
                } else if ((next_move == 1 || next_move == 2)) { //Tnie od dołu, ale wcześniej też po lewej
                    slice = 3;
                    g.removeConnection(g.getNode(w), g.getNode(w - 1));
                } else {
                    System.err.println("Błąd przy tworzeniu ścieżki tnącej. Nie powinien w ogóle wystąpić!");
                    System.exit(1);
                }
                g.removeConnection(g.getNode(w), g.getNode(slice));
                w = next_w;
                move = next_move;
            }
            //Ostatni element
            g.removeConnection(g.getNode(next_w), g.getNode(slice));
        }
    }
}
