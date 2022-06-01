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

public class GUI extends Application {
    private enum Move {
        UP, LEFT, RIGHT, DOWN, NO_MOVE
    }
    private static final int WINDOW_WIDTH = 700/2;
    private static final int WINDOW_HEIGHT = 900/2;
    private static final int CANVAS_RESOLUTION = 700/2;
    private static final double PADDING = 10.0/2;
    private static final int ITEM_HEIGHT = 30/2;
    private static final int BIG_ITEM_HEIGHT = (2 * ITEM_HEIGHT + (int) PADDING)/2;
    private static final int ITEM_WIDTH = 105/2;
    private static final int DEFAULT_COLUMN_COUNT = 10;
    private static final int DEFAULT_ROW_COUNT = 10;
    private static final int DEFAULT_SUBGRAPH_COUNT = 1;
    private static final String DEFAULT_WEIGHT_RANGE = "0-1";
    private static final double LINE_WIDTH_PROPORTION = 2.0 / 3.0;
    private static final double LINE_LENGTH_PROPORTION = 4.0;
    private static final String DEFAULT_FILE_NAME = "graph.txt";

    private Graph graph;

    private PathFinder pathFinder;
    private GraphicsContext gc;
    private FileChooser fileChooser;

    // text fields
    private TextField textFieldColumnCount;
    private TextField textFieldRowCount;
    private TextField textFieldSubgraphCount;
    private TextField textFieldWeightRange;

    // buttons
    private Button buttonGenerate;
    private Button buttonFileOpen;
    private Button buttonFileSave;

    @Override
    public void start(Stage stage) {
        stage.setTitle("SGraph");
        stage.setResizable(false);

        Validator validator = new Validator();

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
                .dependsOn("column",textFieldColumnCount.textProperty())
                .withMethod(c-> {
                    try {
                        if (Integer.parseInt(c.get("column")) <= 0)
                            c.error("The number of columns must be positive.");
                    } catch (NumberFormatException ex)
                    {
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
                .dependsOn("row",textFieldRowCount.textProperty())
                .withMethod(c-> {
                    try {
                        if (Integer.parseInt(c.get("row")) <= 0)
                            c.error("The number of rows must be positive.");
                    } catch (NumberFormatException ex)
                    {
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
                .dependsOn("subgraph",textFieldSubgraphCount.textProperty())
                .withMethod(c-> {
                    try { // can't really check the < nodeCount condition
                        if (Integer.parseInt(c.get("subgraph")) <= 0)
                            c.error("The number of subgraphs must be positive.");
                    } catch (NumberFormatException ex)
                    {
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
                .dependsOn("range",textFieldWeightRange.textProperty())
                .withMethod(c-> {
                    try {
                        String rangeTextContent = c.get("range");
                        double min = Double.parseDouble(rangeTextContent.split("-")[0]);
                        double max = Double.parseDouble(rangeTextContent.split("-")[1]);
                        if (min < 0 || max <= min)
                            c.error("In weight range, MIN must be positive and lower than MAX.");
                    } catch (NumberFormatException|ArrayIndexOutOfBoundsException ex)
                    {
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

        buttonGenerate = new Button("Generate");
        buttonGenerate.setOnAction(actionEvent -> {
            disableAllButtons();
            int col, row, sub;
            double min, max;
            String columnTextContent = textFieldColumnCount.getText();
            String rowTextContent = textFieldRowCount.getText();
            String subgraphTextContent = textFieldSubgraphCount.getText();
            String rangeTextContent = textFieldWeightRange.getText();
            try {
                col = Integer.parseInt(columnTextContent);
                row = Integer.parseInt(rowTextContent);
                sub = Integer.parseInt(subgraphTextContent);
                min = Double.parseDouble(rangeTextContent.split("-")[0]);
                max = Double.parseDouble(rangeTextContent.split("-")[1]);

            } catch (NumberFormatException e) {
                System.err.println("Text fields have not corrected format");
                enableAllButtons();
                return;
            } catch (ArrayIndexOutOfBoundsException ee) {
                System.err.println("Range do not have -, or not correted format");
                enableAllButtons();
                return;
            }
            //TODO try catch, jeśli niepoprawne dane do grafu
            graph = GraphGenerator.generate(col, row, sub, min, max);
            pathFinder=null;
            draw(graph.getColumnCount(), graph.getRowCount());
            enableAllButtons();
        });
        buttonGenerate.setPrefWidth(ITEM_WIDTH);
        buttonGenerate.setPrefHeight(BIG_ITEM_HEIGHT);
        buttonGenerate.setAlignment(Pos.CENTER);

        TooltipWrapper<Button> generateButtonWrapper = new TooltipWrapper<>(
                buttonGenerate,
                validator.containsErrorsProperty(),
                Bindings.concat("Cannot generate a graph:\n", validator.createStringBinding()));

        buttonFileOpen = new Button("Open from file...");
        buttonFileOpen.setOnAction(actionEvent -> {
            disableAllButtons();
            fileChooser.setTitle("Load from...");
            File file = fileChooser.showOpenDialog(stage);

            if (file == null) {
                enableAllButtons();
                return;
            }
            //TODO try catch, jeśli niepoprawny format pliku
            graph = GraphReader.readFromFile(file);
            pathFinder=null;
            draw(graph.getColumnCount(), graph.getRowCount());
            enableAllButtons();
        });
        buttonFileOpen.setPrefWidth(ITEM_WIDTH);
        buttonFileOpen.setPrefHeight(ITEM_HEIGHT);
        buttonFileOpen.setAlignment(Pos.CENTER);

        buttonFileSave = new Button("Save to file...");
        buttonFileSave.setOnAction(actionEvent -> {
            disableAllButtons();
            fileChooser.setTitle("Save to...");
            File file = fileChooser.showSaveDialog(stage);
            if (file == null) {
                enableAllButtons();
                return;
            }
            //TODO try catch
            try {
                graph.readToFile(file);
            } catch (IOException e) {
                e.printStackTrace();
                enableAllButtons();
                return;
            }
            pathFinder=null;
            enableAllButtons();
        });
        buttonFileSave.setPrefWidth(ITEM_WIDTH);
        buttonFileSave.setPrefHeight(ITEM_HEIGHT);
        buttonFileSave.setAlignment(Pos.CENTER);

        VBox buttonBox = new VBox(PADDING, buttonFileOpen, buttonFileSave);

        HBox topBar = new HBox(PADDING, columnBox, rowBox, subgraphBox, weightBox, buttonGenerate, buttonBox);
        topBar.setPadding(new Insets(PADDING));

        Canvas canvas = new Canvas(CANVAS_RESOLUTION, CANVAS_RESOLUTION);
        gc = canvas.getGraphicsContext2D();

        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setInitialFileName(DEFAULT_FILE_NAME);

        FlowPane root = new FlowPane();

        canvas.setOnMouseClicked(event -> { // TODO: make a switch
            disableAllButtons();
            double x, y, r;
            if (graph == null) {
                enableAllButtons();
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
                enableAllButtons();
                return;
            }
            //Sprawdzanie przycisków myszki
            if(event.getButton()== MouseButton.PRIMARY) {
                drawNodes(graph.getNode(posX + posY * graph.getColumnCount()), graph.getNodeCount());
            } else if (event.getButton()== MouseButton.SECONDARY) {
                drawPath(graph.getNode(posX + posY * graph.getColumnCount()), graph.getNodeCount());
            }
            enableAllButtons();
        });


        root.getChildren().addAll(topBar, canvas);

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
        if(pathFinder==null || startingNode!=pathFinder.getStartingNode()) {
            if(pathFinder!=null)
                draw(graph.getColumnCount(), graph.getRowCount());
            pathFinder = new PathFinder(nodeCount, startingNode);
            pathFinder.run();
            pathFinder.calculateNodeValueRange();
        }else
            return; //Ignoruj
        int columnCount = graph.getColumnCount();
        int rowCount = graph.getRowCount();

        // scale
        double ovalR = columnCount > rowCount ? (CANVAS_RESOLUTION - 2 * PADDING) / (2 * columnCount + (LINE_LENGTH_PROPORTION - 2.0) * (columnCount - 1)) : (CANVAS_RESOLUTION - 2 * PADDING) / (2 * rowCount + (LINE_LENGTH_PROPORTION - 2.0) * (rowCount - 1));
        double edgeLength = LINE_LENGTH_PROPORTION * ovalR; // edge length

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(LINE_WIDTH_PROPORTION * ovalR);

        for (int j = 0; j < rowCount; j++) {
            for (int i = 0; i < columnCount; i++) {
                if (pathFinder.getDistanceToNode(graph.getNode(j * graph.getColumnCount() + i)) != -1) {
                    gc.setFill(pathFinder.getNodeValueRange().getHSBValue(pathFinder.getDistanceToNode(graph.getNode(j * graph.getColumnCount() + i))));
                } else {
                    gc.setFill(Color.BLACK); // doesn't colour nodes which are not connected
                }

                gc.fillOval(PADDING + i * edgeLength, PADDING + j * edgeLength, ovalR * 2, ovalR * 2);
            }
        }
    }

    private void drawPath(Node clickedNode, int nodeCount) {
        //Jeśli nie wybrano pierwszego wierzchołka
        if(pathFinder==null) {
            drawNodes(clickedNode,nodeCount);
            return;
        }
        gc.setFill(Color.BLACK);

        // scale
        double ovalR = graph.getColumnCount() > graph.getRowCount() ? (CANVAS_RESOLUTION - 2 * PADDING) / (2 * graph.getColumnCount() + (LINE_LENGTH_PROPORTION - 2.0) * (graph.getColumnCount() - 1)) : (CANVAS_RESOLUTION - 2 * PADDING) / (2 * graph.getRowCount() + (LINE_LENGTH_PROPORTION - 2.0) * (graph.getRowCount() - 1));

        double edgeLength = LINE_LENGTH_PROPORTION * ovalR; // edge length

        gc.setStroke(Color.BLACK); //Domyślny kolor
        gc.setLineWidth(LINE_WIDTH_PROPORTION * ovalR);

        LinkedList<Integer> path=pathFinder.getPathToNode(clickedNode);
        Move move;
        int x,y;
        x=path.get(0)%graph.getColumnCount();
        y=path.get(0)/graph.getColumnCount();
        gc.fillOval(PADDING + x * edgeLength, PADDING + y * edgeLength, ovalR * 2, ovalR * 2);
        gc.beginPath();
        for(int i=1;i< path.size();i++){
            move=getDirection(path.get(i-1),path.get(i), graph.getColumnCount(),graph.getRowCount());
            //Pionowo
            if(move==Move.DOWN) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength + edgeLength);
            } else if (move==Move.UP) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength - edgeLength);
            } else if (move==Move.RIGHT) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength + edgeLength, PADDING + ovalR + y * edgeLength);
            } else if (move==Move.LEFT) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength - edgeLength, PADDING + ovalR + y * edgeLength);
            }else{
                System.err.println("GraphPath: An unexpected error occurred while walking through path");
                return;
            }
            //Punkt
            x=path.get(i)%graph.getColumnCount();
            y=path.get(i)/graph.getColumnCount();
            gc.fillOval(PADDING + x * edgeLength, PADDING + y * edgeLength, ovalR * 2, ovalR * 2);
        }
        gc.stroke();
        gc.closePath();
    }

    private static Move getDirection(int position, int n_position, int columnCount, int rowCount) {
        if (position - columnCount > -1 && position - columnCount == n_position)
            return Move.UP;
        else if (position - 1 == n_position && position / columnCount == n_position / columnCount)
            return Move.LEFT;
        else if (position + 1 == n_position && position / columnCount == n_position / columnCount)
            return Move.RIGHT;
        else if (position + columnCount < rowCount * columnCount && position + columnCount == n_position)
            return Move.DOWN;
        else
            return Move.NO_MOVE;
    }
    private void disableAllButtons() {
        buttonFileOpen.setDisable(true);
        buttonFileSave.setDisable(true);
    }

    private void enableAllButtons() {
        buttonFileOpen.setDisable(false);
        buttonFileSave.setDisable(false);
    }
}

// TODO:
//  - graph generation should be in a separate thread
//  - Save button should be disabled until a proper graph in generated
