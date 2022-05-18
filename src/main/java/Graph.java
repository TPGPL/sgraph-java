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

        if (subgraphCount != 1)
            divide(); // TODO
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
        return;
    } // TODO

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
