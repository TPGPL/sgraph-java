import java.io.*;
import java.util.*;

public class Graph {
    private final int columnCount;
    private final int rowCount;
    private int subgraphCount;
    private final ArrayList<Node> nodes;
    private Range edgeValueRange;

    public Graph(int columnCount, int rowCount) {
        if (columnCount <= 0)
            throw new IllegalArgumentException("Graph: The number of columns must be positive.");

        if (rowCount <= 0)
            throw new IllegalArgumentException("Graph: The number of rows must be positive.");


        this.columnCount = columnCount;
        this.rowCount = rowCount;

        nodes = new ArrayList<>();

        for (int i = 0; i < columnCount * rowCount; i++) {
            nodes.add(new Node(i));
        }
    }

    public Graph(String path){
        String [] line;
        ArrayList<Double> convertedLine;
        Scanner file_scanner = null;

        try {
            file_scanner = new Scanner(new File(path));
        }catch (FileNotFoundException e) {
            System.err.println("Graph: File not found");
            System.exit(1);
        }

        line=file_scanner.nextLine().replace(":"," ").split(" ");
        convertedLine=new ArrayList<>();
        //Function for converting a line
        for (String s : line) {
            try {
                convertedLine.add(Double.parseDouble(s));
            } catch (NumberFormatException ignored) {}
        }

        if(convertedLine.size()!=2)
            throw new InputMismatchException("Graph: Not correct graph dimensions");

        rowCount = convertedLine.get(0).intValue();
        columnCount = convertedLine.get(1).intValue();
        nodes=new ArrayList<>();

        for (int i = 0; i < rowCount*columnCount; i++)
            nodes.add(new Node(i));

        for(int i=0;i< nodes.size();i++){
            try {
                line = file_scanner.nextLine().replace(":", " ").split(" ");
            }catch ( NoSuchElementException e){
                System.err.println("Graph: File have less nodes that dimension suggest");
                System.exit(1);
            }
            convertedLine=new ArrayList<>();

            //Function for converting a line
            for (String s : line) {
                try {
                    convertedLine.add(Double.parseDouble(s));
                } catch (NumberFormatException ignored) {}
            }

            if(convertedLine.size()%2!=0)
                throw new InputMismatchException("Graph: Not correct graph nodes values in line " + (i+1));

            for(int j=0;j<convertedLine.size();j+=2){
                addConnection(nodes.get(i),nodes.get(convertedLine.get(j).intValue()),convertedLine.get(j+1));
            }
        }
        if(file_scanner.hasNextDouble())
            throw new InputMismatchException("Graph: File have more nodes that dimension suggest");
    }

    public Graph(int columnCount, int rowCount, int subgraphCount, double min, double max) {
        this(columnCount, rowCount);

        if (subgraphCount <= 0)
            throw new IllegalArgumentException("Graph: The number of subgraphs must be positive.");

        this.subgraphCount = subgraphCount;
        edgeValueRange = new Range(min, max);

        generate(); // in constructor to ensure that it is used only once per object
    }

    public int getColumnCount() {
        return columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public int getSubgraphCount() {
        return subgraphCount;
    }

    public Range getEdgeValueRange() {
        return edgeValueRange;
    }

    public Node getNode(int index) throws IllegalArgumentException {
        if (index < 0 || index >= columnCount * rowCount)
            throw new IllegalArgumentException(String.format("Graph: Cannot get a node of index %d in a %dx%d graph.", index, rowCount, columnCount));

        return nodes.get(index);
    }

    public void addConnection(Node node1, Node node2, double edge) throws IllegalArgumentException {
        if (!canNodesAdhere(node1, node2))
            throw new IllegalArgumentException(String.format("Graph: Nodes %d and %d cannot adhere in a %dx%d graph.", node1.getIndex(), node2.getIndex(), rowCount, columnCount));

        if (edge <= 0)
            throw new IllegalArgumentException("Graph: Edge value must be positive.");

        if (node1.hasConnection(node2)) // connection between node1 and node2 exists
        {
            double definedEdge = node1.getEdgeOnConnection(node2);

            if (definedEdge == edge) // the edge values are equal -> likely has been added the second time through IO
                return;
            else
                throw new IllegalArgumentException(String.format("Graph: Connection between nodes %d and %d has already been defined with an edge value of %g", node1.getIndex(), node2.getIndex(), definedEdge));
        }

        node1.addConnection(node2, edge);
        node2.addConnection(node1, edge);
    }

    public void removeConnection(Node node1, Node node2) {
        node1.removeConnection(node2);
        node2.removeConnection(node1);
    }

    private boolean canNodesAdhere(Node node1, Node node2) {
        int row1 = (node1.getIndex() - node1.getIndex() % columnCount) / columnCount + 1;
        int row2 = (node2.getIndex() - node2.getIndex() % columnCount) / columnCount + 1;
        int col1 = node1.getIndex() % columnCount + 1;
        int col2 = node2.getIndex() % columnCount + 1;

        return Math.abs(row1 - row2) == 1 || Math.abs(col1 - col2) == 1;
    }

    public void setEdgeValueRange(double min, double max) {
        edgeValueRange = new Range(min, max);
    }

    private void generate() {
        Random r = new Random();

        for (int i = 0; i < rowCount * columnCount; i++) {
            if (i % columnCount + 1 != columnCount) // if node is not in the last column
                addConnection(nodes.get(i), nodes.get(i + 1), r.nextDouble(edgeValueRange.getMin(), edgeValueRange.getMax()));

            if ((i - i % columnCount) / columnCount + 1 != rowCount) // if node is not in the last row
                addConnection(nodes.get(i), nodes.get(i + columnCount), r.nextDouble(edgeValueRange.getMin(), edgeValueRange.getMax()));
        }
        int limit=0;
        if (subgraphCount != 1)
            limit=subgraphCount;
            calculateSubraphCount();
            while(limit>subgraphCount) {
                divide();
                calculateSubraphCount();
            }// TODO
    }

    public void print() {
        System.out.println(rowCount + " " + columnCount);
        for (Node n : nodes) {
            System.out.println(n.toString());
        }
    }

    public void readToFile(String path) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(path));
        writer.write(String.format("%d %d\n", rowCount, columnCount));

        for (Node n : nodes) {
            writer.write(n.toString() + "\n");
        }

        writer.close();
    }

    public void divide() {
        Random random=new Random();
        ArrayList<Integer> way=new ArrayList<>();
        int control,w,next_w,move,next_move,slice=0;
        //Znalezienie początku
        do {
            w=random.nextInt(nodes.size());
        } while(nodes.get(w).getAdherentNumber()==4 || nodes.get(w).getAdherentNumber()==0);
        way.add(w);
        //Tworzenie ścieżki
        control=0; //Służy do sprawdzenia, czy kod nie wykonuję się za długo
        do {
            //wyciągam losowy sąsiedni wierzchołek
            next_w=nodes.get(w).getConnectedNodes().get(random.nextInt(nodes.get(w).getAdherentNumber())).getIndex();
            if(!way.contains(next_w)){
                way.add(next_w);
                w=next_w;
                control=0;
            }else
                control++;
        } while(nodes.get(w).getAdherentNumber()==4 && control<40);
        if(control==40) //Pętla nieskończona
            return;
        //Cięcie
        //try i catch wyłapuje czy w danym przypadku nie wystąpił początek albo koniec, gdzie może nie mieć co ciąć
        //Co jeśli są tylko dwa punkty
        if(way.size()==2){
            if(nodes.get(way.get(0)).getAdherentNumber()==1 && nodes.get(way.get(1)).getAdherentNumber()==1){ //Jeśli są połączone tylko ze sobą
                removeConnection(nodes.get(way.get(0)),nodes.get(way.get(1)));
            } else{ //Jeśli istnieją połączenia z innymi punktami
                while(nodes.get(way.get(0)).getAdherentNumber()>1){
                    if(nodes.get(way.get(0)).getConnectedNodes().get(0).getIndex()!=way.get(1)){
                        removeConnection(nodes.get(way.get(0)),nodes.get(way.get(0)).getConnectedNodes().get(0)); //Usuwa pierwszego z listy
                    }else{
                        removeConnection(nodes.get(way.get(0)),nodes.get(way.get(0)).getConnectedNodes().get(1)); //Chyba, że to ten drugi, wtedy bierze kolejnego
                    }
                }
                while(nodes.get(way.get(1)).getAdherentNumber()>1){
                    if(nodes.get(way.get(1)).getConnectedNodes().get(1).getIndex()!=way.get(0)){
                        removeConnection(nodes.get(way.get(1)),nodes.get(way.get(1)).getConnectedNodes().get(0)); //Usuwa pierwszego z listy
                    }else{
                        removeConnection(nodes.get(way.get(1)),nodes.get(way.get(1)).getConnectedNodes().get(1)); //Chyba, że to ten drugi, wtedy bierze kolejnego
                    }
                }
            }
        }else {
            //Droga dłuższa niż dwa
            //Pierwszy krok
            w = way.get(0);
            next_w = way.get(1);
            move = getDirection(w, next_w);
            if (move == 0 || move == 3) { //Tnie na lewo
                slice = w-1;
            } else if (move == 1 || move == 2) { //Tnie od dołu
                slice = w+columnCount;
            } else {
                System.err.println("Błąd przy tworzeniu ścieżki tnącej. Nie powinien w ogóle wystąpić!");
                System.exit(1);
            }
            removeConnection(nodes.get(w),nodes.get(slice));
            w = next_w;
            //Reszta ścieżki
            for (int i = 2; i < way.size(); i++) {
                next_w = way.get(i);
                next_move = getDirection(w, next_w);
                if ((next_move == 0 || next_move == 3) && next_move == move) { //Tnie na lewo
                    slice = w-1;
                } else if ((next_move == 1 || next_move == 2) && next_move == move) { //Tnie od dołu
                    slice = w+columnCount;
                } else if ((next_move == 0 || next_move == 3)) { //Tnie na lewo, ale wcześniej też od dołu
                    slice = w-1;
                    removeConnection(nodes.get(w),nodes.get(w+columnCount));
                } else if ((next_move == 1 || next_move == 2)) { //Tnie od dołu, ale wcześniej też po lewej
                    slice = 3;
                    removeConnection(nodes.get(w),nodes.get(w-1));
                } else {
                    System.err.println("Błąd przy tworzeniu ścieżki tnącej. Nie powinien w ogóle wystąpić!");
                    System.exit(1);
                }
                removeConnection(nodes.get(w),nodes.get(slice));
                w=next_w;
                move=next_move;
            }
            //Ostatni element
            removeConnection(nodes.get(next_w),nodes.get(slice));
        }
    }

    private int getDirection(int position, int n_position){
        //Góra
        if(position-columnCount >-1 && position-columnCount==n_position)
            return 0;
        //Lewo
        else if(position-1==n_position && position/columnCount==n_position/columnCount)
            return 1;
        //Prawo
        else if(position+1==n_position && position/columnCount==n_position/columnCount)
            return 2;
        //Dół
        else if(position+columnCount <nodes.size() && position+columnCount==n_position)
            return 3;
        else
            return  -1;
    }

    public boolean areNodesConnected(Node node1, Node node2) {
        BreadthFirstSearch bfs = new BreadthFirstSearch(rowCount * columnCount);
        bfs.run(node1);

        return bfs.wasNodeVisited(node2);
    }

    public void calculateSubraphCount() {
        int n = 0;
        BreadthFirstSearch bfs = new BreadthFirstSearch(rowCount * columnCount);

        while (bfs.hasNotVisitedNode()) {
            n++;
            bfs.run(nodes.get(bfs.getNotVisitedNode()));
        }

        subgraphCount = n;
    }
}
