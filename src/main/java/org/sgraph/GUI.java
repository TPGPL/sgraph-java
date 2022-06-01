package org.sgraph;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import net.synedra.validatorfx.TooltipWrapper;
import net.synedra.validatorfx.Validator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.sgraph.GraphGenerator.getDirection;

public class GUI extends Application {
    private static final int WINDOW_WIDTH = 700;
    private static final int WINDOW_HEIGHT = 900;
    private static final int CANVAS_RESOLUTION = 700;
    private static final double PADDING = 10.0;
    private static final int ITEM_HEIGHT = 30;
    private static final int BIG_ITEM_HEIGHT = 2 * ITEM_HEIGHT + (int) PADDING;
    private static final int ITEM_WIDTH = 105;
    private static final int DEFAULT_COLUMN_COUNT = 10;
    private static final int DEFAULT_ROW_COUNT = 10;
    private static final int DEFAULT_SUBGRAPH_COUNT = 1;
    private static final String DEFAULT_WEIGHT_RANGE = "0-1";
    private static final double LINE_WIDTH_PROPORTION = 2.0 / 3.0;
    private static final double LINE_LENGTH_PROPORTION = 4.0;
    private static final String DEFAULT_FILE_NAME = "graph.txt";

    private Graph graph;
    private PathFinder pf;
    private GraphicsContext gc;
    private FileChooser fileChooser;

    // text fields
    private TextField textFieldColumnCount;
    private TextField textFieldRowCount;
    private TextField textFieldSubgraphCount;
    private TextField textFieldWeightRange;

    // range field

    private Label labelEdgeRangeMin;
    private Label labelEdgeRangeMax;
    private Label labelNodeRangeMin;
    private Label labelNodeRangeMax;

    @Override
    public void start(Stage stage) {
        stage.setTitle("SGraph");
        stage.setResizable(false);

        Validator validator = new Validator();
        AtomicBoolean wasPathDrawn = new AtomicBoolean(false);

        // GUI creation
        // text labels

        Label labelColumnTextField = new Label("# of columns");
        labelColumnTextField.setPrefWidth(ITEM_WIDTH);
        labelColumnTextField.setPrefHeight(ITEM_HEIGHT);
        labelColumnTextField.setAlignment(Pos.CENTER);

        Label labelRowTextField = new Label("# of rows");
        labelRowTextField.setPrefWidth(ITEM_WIDTH);
        labelRowTextField.setPrefHeight(ITEM_HEIGHT);
        labelRowTextField.setAlignment(Pos.CENTER);

        Label labelSubgraphTextField = new Label("# of subgraphs");
        labelSubgraphTextField.setPrefWidth(ITEM_WIDTH);
        labelSubgraphTextField.setPrefHeight(ITEM_HEIGHT);
        labelSubgraphTextField.setAlignment(Pos.CENTER);

        Label labelRangeTextField = new Label("Weight range");
        labelRangeTextField.setPrefWidth(ITEM_WIDTH);
        labelRangeTextField.setPrefHeight(ITEM_HEIGHT);
        labelRangeTextField.setAlignment(Pos.CENTER);

        // text fields

        textFieldColumnCount = new TextField(Integer.toString(DEFAULT_COLUMN_COUNT));
        textFieldColumnCount.setPrefWidth(ITEM_WIDTH);
        textFieldColumnCount.setPrefHeight(ITEM_HEIGHT);
        textFieldColumnCount.setAlignment(Pos.CENTER);

        validator.createCheck()
                .dependsOn("column", textFieldColumnCount.textProperty())
                .withMethod(c -> {
                    try {
                        if (Integer.parseInt(c.get("column")) <= 0)
                            c.error("The number of columns must be positive.");
                    } catch (NumberFormatException ex) {
                        c.error("The column field must contain a natural number.");
                    }
                })
                .decorates(textFieldColumnCount)
                .immediate();

        textFieldRowCount = new TextField(Integer.toString(DEFAULT_ROW_COUNT));
        textFieldRowCount.setPrefWidth(ITEM_WIDTH);
        textFieldRowCount.setPrefHeight(ITEM_HEIGHT);
        textFieldRowCount.setAlignment(Pos.CENTER);

        validator.createCheck()
                .dependsOn("row", textFieldRowCount.textProperty())
                .withMethod(c -> {
                    try {
                        if (Integer.parseInt(c.get("row")) <= 0)
                            c.error("The number of rows must be positive.");
                    } catch (NumberFormatException ex) {
                        c.error("The row field must contain a natural number.");
                    }
                })
                .decorates(textFieldRowCount)
                .immediate();

        textFieldSubgraphCount = new TextField(Integer.toString(DEFAULT_SUBGRAPH_COUNT));
        textFieldSubgraphCount.setPrefWidth(ITEM_WIDTH);
        textFieldSubgraphCount.setPrefHeight(ITEM_HEIGHT);
        textFieldSubgraphCount.setAlignment(Pos.CENTER);

        validator.createCheck()
                .dependsOn("subgraph", textFieldSubgraphCount.textProperty())
                .withMethod(c -> {
                    try { // can't really check the < nodeCount condition
                        if (Integer.parseInt(c.get("subgraph")) <= 0)
                            c.error("The number of subgraphs must be positive.");
                    } catch (NumberFormatException ex) {
                        c.error("The subgraph field must contain a natural number.");
                    }
                })
                .decorates(textFieldSubgraphCount)
                .immediate();

        textFieldWeightRange = new TextField(DEFAULT_WEIGHT_RANGE);
        textFieldWeightRange.setPrefWidth(ITEM_WIDTH);
        textFieldWeightRange.setPrefHeight(ITEM_HEIGHT);
        textFieldWeightRange.setAlignment(Pos.CENTER);

        validator.createCheck()
                .dependsOn("range", textFieldWeightRange.textProperty())
                .withMethod(c -> {
                    try {
                        String rangeTextContent = c.get("range");
                        double min = Double.parseDouble(rangeTextContent.split("-")[0]);
                        double max = Double.parseDouble(rangeTextContent.split("-")[1]);
                        if (min < 0 || max <= min)
                            c.error("In weight range, MIN must be positive and lower than MAX.");
                    } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
                        c.error("The range field must follow a 'MIN-MAX' format.");
                    }
                })
                .decorates(textFieldWeightRange)
                .immediate();

        VBox columnBox = new VBox(PADDING, labelColumnTextField, textFieldColumnCount);
        VBox rowBox = new VBox(PADDING, labelRowTextField, textFieldRowCount);
        VBox subgraphBox = new VBox(PADDING, labelSubgraphTextField, textFieldSubgraphCount);
        VBox weightBox = new VBox(PADDING, labelRangeTextField, textFieldWeightRange);

        // buttons

        // buttons
        Button buttonGenerate = new Button("Generate");
        buttonGenerate.setOnAction(actionEvent -> {
            int col, row, sub;
            double min, max;
            String rangeTextContent = textFieldWeightRange.getText();

            try {
                col = Integer.parseInt(textFieldColumnCount.getText());
                row = Integer.parseInt(textFieldRowCount.getText());
                sub = Integer.parseInt(textFieldSubgraphCount.getText());
                min = Double.parseDouble(rangeTextContent.split("-")[0]);
                max = Double.parseDouble(rangeTextContent.split("-")[1]);

                if (sub > col * row)
                    throw new IllegalArgumentException("The number of subgraphs must be lower than the node count.");

            } catch (NumberFormatException e) {
                System.err.println("The text fields' content has incorrect format.");
                return;
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("The range text field doesn't follow the MIN-MAX format.");
                return;
            } catch (IllegalArgumentException e) {
                System.err.println(e.getMessage());
                return;
            }

            try {
                graph = GraphGenerator.generate(col, row, sub, min, max);
            } catch (Exception e) // if graph generation still SOMEHOW failed
            {
                System.err.println("Graph generation failed - error message: " + e.getMessage());
                return;
            }

            pf = null; // clearing PathFinder from previous usages;
            setNodeRangeLabels();
            setEdgeRangeLabels();

            draw(graph.getColumnCount(), graph.getRowCount());
        });
        buttonGenerate.setPrefWidth(ITEM_WIDTH);
        buttonGenerate.setPrefHeight(BIG_ITEM_HEIGHT);
        buttonGenerate.setAlignment(Pos.CENTER);

        TooltipWrapper<Button> generateButtonWrapper = new TooltipWrapper<>(
                buttonGenerate,
                validator.containsErrorsProperty(),
                Bindings.concat("Cannot generate a graph:\n", validator.createStringBinding()));

        Button buttonFileOpen = new Button("Open from file...");
        buttonFileOpen.setOnAction(actionEvent -> {
            fileChooser.setTitle("Load from...");
            File file = fileChooser.showOpenDialog(stage);

            if (file == null) {
                System.err.println("Failed to load the file.");
                return;
            }

            try {
                graph = GraphReader.readFromFile(file);
            } catch (Exception e) {
                System.err.println("Failed to load a graph from a file - error message: " + e.getMessage());
                return;
            }

            pf = null; // clearing up PathFinder from previous graph usages
            setNodeRangeLabels();
            setEdgeRangeLabels();

            draw(graph.getColumnCount(), graph.getRowCount());
        });
        buttonFileOpen.setPrefWidth(ITEM_WIDTH);
        buttonFileOpen.setPrefHeight(ITEM_HEIGHT);
        buttonFileOpen.setAlignment(Pos.CENTER);

        Button buttonFileSave = new Button("Save to file...");
        buttonFileSave.setOnAction(actionEvent -> {
            if (graph == null) {
                System.err.println("The graph has not been generated yet - cannot save anything to file.");
                return;
            }
            fileChooser.setTitle("Save to...");
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                return;
            }

            try {
                graph.readToFile(file);
            } catch (IOException e) {
                System.err.println("Failed to save a graph to file - error message: " + e.getMessage());
            }
        });
        buttonFileSave.setPrefWidth(ITEM_WIDTH);
        buttonFileSave.setPrefHeight(ITEM_HEIGHT);
        buttonFileSave.setAlignment(Pos.CENTER);

        VBox buttonBox = new VBox(PADDING, buttonFileOpen, buttonFileSave);

        HBox topBar = new HBox(PADDING, columnBox, rowBox, subgraphBox, weightBox, generateButtonWrapper, buttonBox);
        topBar.setPadding(new Insets(PADDING));

        Canvas canvas = new Canvas(CANVAS_RESOLUTION, CANVAS_RESOLUTION);
        gc = canvas.getGraphicsContext2D();

        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setInitialFileName(DEFAULT_FILE_NAME);

        FlowPane root = new FlowPane();

        canvas.setOnMouseClicked(event -> {
            double x, y, r;
            if (graph == null) {
                return;
            }
            x = event.getSceneX() - canvas.getLayoutX();
            y = event.getSceneY() - canvas.getLayoutY();

            int posX, posY, scale;

            scale = Math.max(graph.getColumnCount(), graph.getRowCount());
            r = (CANVAS_RESOLUTION - 2 * PADDING) / (scale * 4 - 2);

            posX = (int) ((x - PADDING + r) / (4 * r));
            posY = (int) ((y - PADDING + r) / (4 * r));

            if (posX < 0 || posY < 0 || posX > graph.getColumnCount() - 1 || posY > graph.getRowCount() - 1) {
                return;
            }

            if (event.getButton() == MouseButton.PRIMARY) {
                if (wasPathDrawn.get()) {
                    draw(graph.getColumnCount(), graph.getRowCount());
                    wasPathDrawn.set(false);
                }

                drawNodes(graph.getNode(posX + posY * graph.getColumnCount()), graph.getNodeCount());
                System.out.println("Chosen node: number " + pf.getStartingNode().getIndex());
            } else if (event.getButton() == MouseButton.SECONDARY) {
                if (pf == null) // no node chosen
                    return;

                drawPath(graph.getNode(posX + posY * graph.getColumnCount()));

                wasPathDrawn.set(true);
            }

        });

        // bottom bar

        labelEdgeRangeMin = new Label("MIN");
        labelEdgeRangeMin.setAlignment(Pos.BOTTOM_LEFT);
        labelEdgeRangeMin.setPrefHeight(ITEM_HEIGHT);
        labelEdgeRangeMin.setPrefWidth(WINDOW_WIDTH / 3.0);

        labelEdgeRangeMax = new Label("MAX");
        labelEdgeRangeMax.setAlignment(Pos.BOTTOM_RIGHT);
        labelEdgeRangeMax.setPrefHeight(ITEM_HEIGHT);
        labelEdgeRangeMax.setPrefWidth(WINDOW_WIDTH / 3.0);

        Label labelEdgeRangeTitle = new Label("Edge color scale");
        labelEdgeRangeTitle.setAlignment(Pos.BOTTOM_CENTER);
        labelEdgeRangeTitle.setPrefHeight(ITEM_HEIGHT);
        labelEdgeRangeTitle.setPrefWidth(WINDOW_WIDTH / 3.0);

        HBox edgeRangeContainer = new HBox(0, labelEdgeRangeMin, labelEdgeRangeTitle, labelEdgeRangeMax);

        labelNodeRangeMin = new Label("MIN");
        labelNodeRangeMin.setAlignment(Pos.TOP_LEFT);
        labelNodeRangeMin.setPrefHeight(ITEM_HEIGHT);
        labelNodeRangeMin.setPrefWidth(WINDOW_WIDTH / 3.0);

        labelNodeRangeMax = new Label("MAX");
        labelNodeRangeMax.setAlignment(Pos.TOP_RIGHT);
        labelNodeRangeMax.setPrefHeight(ITEM_HEIGHT);
        labelNodeRangeMax.setPrefWidth(WINDOW_WIDTH / 3.0);

        Label labelNodeRangeTitle = new Label("Node color scale");
        labelNodeRangeTitle.setAlignment(Pos.TOP_CENTER);
        labelNodeRangeTitle.setPrefHeight(ITEM_HEIGHT);
        labelNodeRangeTitle.setPrefWidth(WINDOW_WIDTH / 3.0);

        HBox nodeRangeContainer = new HBox(0, labelNodeRangeMin, labelNodeRangeTitle, labelNodeRangeMax);

        Image colorScale = createColorScale();
        ImageView scaleContainer = new ImageView(colorScale);

        VBox bottomBar = new VBox(PADDING / 2, edgeRangeContainer, scaleContainer, nodeRangeContainer);

        root.getChildren().addAll(topBar, canvas, bottomBar);

        stage.setScene(new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void draw(int columnCount, int rowCount) {

        gc.clearRect(0, 0, CANVAS_RESOLUTION, CANVAS_RESOLUTION);
        gc.setFill(Color.BLACK);

        // scale
        double ovalR = columnCount > rowCount ? (CANVAS_RESOLUTION - 2 * PADDING) / (2 * columnCount + (LINE_LENGTH_PROPORTION - 2.0) * (columnCount - 1)) : (CANVAS_RESOLUTION - 2 * PADDING) / (2 * rowCount + (LINE_LENGTH_PROPORTION - 2.0) * (rowCount - 1));

        double edgeLength = LINE_LENGTH_PROPORTION * ovalR; // edge length

        gc.setStroke(Color.BLACK); //Domyślny kolor
        gc.setLineWidth(LINE_WIDTH_PROPORTION * ovalR);

        int parsedNodeIndex, adhNodeIndex;

        for (int j = 0; j < rowCount; j++) {
            for (int i = 0; i < columnCount; i++) {
                parsedNodeIndex = j * columnCount + i;
                // vertical connection
                if ((adhNodeIndex = checkDown(parsedNodeIndex)) != -1) {
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getNode(parsedNodeIndex).getEdgeOnConnection(graph.getNode(adhNodeIndex))));
                    gc.beginPath();
                    gc.moveTo(PADDING + ovalR + i * edgeLength, PADDING + ovalR + j * edgeLength);
                    gc.lineTo(PADDING + ovalR + i * edgeLength + 0, PADDING + ovalR + j * edgeLength + edgeLength);
                    gc.stroke();
                    gc.closePath();
                }
                // horizontal connection
                if ((adhNodeIndex = checkRight(parsedNodeIndex)) != -1) {
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getNode(parsedNodeIndex).getEdgeOnConnection(graph.getNode(adhNodeIndex))));
                    gc.beginPath();
                    gc.moveTo(PADDING + ovalR + i * edgeLength, PADDING + ovalR + j * edgeLength);
                    gc.lineTo(PADDING + ovalR + i * edgeLength + edgeLength, PADDING + ovalR + j * edgeLength);
                    gc.stroke();
                    gc.closePath();
                }
            }
        }

        for (int j = 0; j < rowCount; j++) {
            for (int i = 0; i < columnCount; i++) {
                gc.fillOval(PADDING + i * edgeLength, PADDING + j * edgeLength, ovalR * 2, ovalR * 2); //Punkt
            }
        }
    }

    private int checkDown(int index) {
        ArrayList<Node> connectedNodes = graph.getNode(index).getConnectedNodes();
        for (Node n : connectedNodes) {
            if (index + graph.getColumnCount() < graph.getRowCount() * graph.getColumnCount() && index + graph.getColumnCount() == n.getIndex())
                return n.getIndex();
        }
        return -1;
    }

    private int checkRight(int index) {
        ArrayList<Node> connectedNodes = graph.getNode(index).getConnectedNodes();
        for (Node n : connectedNodes) {
            if (index + 1 == n.getIndex() && index / graph.getColumnCount() == n.getIndex() / graph.getColumnCount())
                return n.getIndex();
        }
        return -1;
    }

    private void drawNodes(Node startingNode, int nodeCount) {
        pf = new PathFinder(nodeCount, startingNode);
        pf.run();
        pf.calculateNodeValueRange();
        setNodeRangeLabels();

        int columnCount = graph.getColumnCount();
        int rowCount = graph.getRowCount();

        // scale
        double ovalR = columnCount > rowCount ? (CANVAS_RESOLUTION - 2 * PADDING) / (2 * columnCount + (LINE_LENGTH_PROPORTION - 2.0) * (columnCount - 1)) : (CANVAS_RESOLUTION - 2 * PADDING) / (2 * rowCount + (LINE_LENGTH_PROPORTION - 2.0) * (rowCount - 1));
        double edgeLength = LINE_LENGTH_PROPORTION * ovalR; // edge length

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(LINE_WIDTH_PROPORTION * ovalR);

        for (int j = 0; j < rowCount; j++) {
            for (int i = 0; i < columnCount; i++) {
                if (pf.getDistanceToNode(graph.getNode(j * graph.getColumnCount() + i)) != -1) {
                    gc.setFill(pf.getNodeValueRange().getHSBValue(pf.getDistanceToNode(graph.getNode(j * graph.getColumnCount() + i))));
                } else {
                    gc.setFill(Color.BLACK); // doesn't colour nodes which are not connected
                }

                gc.fillOval(PADDING + i * edgeLength, PADDING + j * edgeLength, ovalR * 2, ovalR * 2);
            }
        }
    }

    private Color getColorFromValue(double value) {
        if (value < 0 || value > 100) {
            return Color.BLACK;
        }
        double hue = Color.BLUE.getHue() + (Color.RED.getHue() - Color.BLUE.getHue()) * value / 100;
        return Color.hsb(hue, 1, 1, 1);
    }

    private Image createColorScale() {

        WritableImage scale = new WritableImage(WINDOW_WIDTH, ITEM_HEIGHT);
        PixelWriter pw = scale.getPixelWriter();

        for (int x = 0; x < WINDOW_WIDTH; x++) {
            double value = 100.0 * x / WINDOW_WIDTH;
            Color color = getColorFromValue(value);

            for (int y = 0; y < ITEM_HEIGHT; y++) {
                pw.setColor(x, y, color);
            }
        }

        return scale;
    }

    private void setEdgeRangeLabels() {
        labelEdgeRangeMin.setText(graph == null ? "MIN" : Double.toString(graph.getEdgeValueRange().getMin()));
        labelEdgeRangeMax.setText(graph == null ? "MAX" : Double.toString(graph.getEdgeValueRange().getMax()));
    }

    private void setNodeRangeLabels() {
        labelNodeRangeMin.setText(pf == null ? "MIN" : Double.toString(pf.getNodeValueRange().getMin()));
        labelNodeRangeMax.setText(pf == null ? "MAX" : Double.toString(pf.getNodeValueRange().getMax()));
    }

    private void drawPath(Node clickedNode) {
        gc.setFill(Color.BLACK);

        // scale
        double ovalR = graph.getColumnCount() > graph.getRowCount() ? (CANVAS_RESOLUTION - 2 * PADDING) / (2 * graph.getColumnCount() + (LINE_LENGTH_PROPORTION - 2.0) * (graph.getColumnCount() - 1)) : (CANVAS_RESOLUTION - 2 * PADDING) / (2 * graph.getRowCount() + (LINE_LENGTH_PROPORTION - 2.0) * (graph.getRowCount() - 1));

        double edgeLength = LINE_LENGTH_PROPORTION * ovalR; // edge length

        gc.setStroke(Color.BLACK); //Domyślny kolor
        gc.setLineWidth(LINE_WIDTH_PROPORTION * ovalR);

        LinkedList<Integer> path = pf.getIndexPathToNode(clickedNode);

        if (path == null) {
            System.err.printf("There is not path between nodes %d and %d.%n", pf.getStartingNode().getIndex(), clickedNode.getIndex());
            return;
        }

        GraphGenerator.Move move;
        int x = path.get(0) % graph.getColumnCount();
        int y = path.get(0) / graph.getColumnCount();

        gc.fillOval(PADDING + x * edgeLength, PADDING + y * edgeLength, ovalR * 2, ovalR * 2);
        gc.beginPath();

        for (int i = 1; i < path.size(); i++) {
            move = getDirection(path.get(i - 1), path.get(i), graph.getColumnCount(), graph.getRowCount());

            if (move == GraphGenerator.Move.DOWN) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength + edgeLength);
            } else if (move == GraphGenerator.Move.UP) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength - edgeLength);
            } else if (move == GraphGenerator.Move.RIGHT) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength + edgeLength, PADDING + ovalR + y * edgeLength);
            } else if (move == GraphGenerator.Move.LEFT) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength - edgeLength, PADDING + ovalR + y * edgeLength);
            } else {
                System.err.println("An unexpected error occurred while drawing a path.");
                return;
            }
            //Punkt
            x = path.get(i) % graph.getColumnCount();
            y = path.get(i) / graph.getColumnCount();
            gc.fillOval(PADDING + x * edgeLength, PADDING + y * edgeLength, ovalR * 2, ovalR * 2);
        }
        gc.stroke();
        gc.closePath();

        System.out.printf("Distance between nodes %d and %d: %g%n", pf.getStartingNode().getIndex(), clickedNode.getIndex(), pf.getDistanceToNode(clickedNode));
        System.out.printf("Path: %s%n", pf.getPathToNode(clickedNode));
    }
}
