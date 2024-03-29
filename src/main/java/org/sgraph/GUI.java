package org.sgraph;

import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.sgraph.Move.*;
import static org.sgraph.Properties.*;

/**
 * Klasa odpowiadająca za interfejs graficzny aplikacji i przetwarzanie zdarzeń w programie.
 */
public class GUI extends Application {
    /**
     * Obiekt przechowujący aktualnie wygenerowany graf.
     */
    private Graph graph;
    /**
     * Obiekt odpowiadający za wyszukiwanie ścieżek do aktualnie wybranego wierzchołka początkowego.
     */
    private PathFinder pf;
    /**
     * Obiekt odpowiadający za rysowanie po canvasie.
     */
    private GraphicsContext gc;
    /**
     * Obiekt odpowiadający za okno wyboru i zapisu plików.
     */
    private FileChooser fileChooser;

    /**
     * Pole tekstowe przeznaczone na liczbę kolumn w siatce.
     */
    // text fields
    private TextField textFieldColumnCount;
    /**
     * Pole tekstowe przeznaczone na liczbę wierszy w siatce.
     */
    private TextField textFieldRowCount;
    /**
     * Pole tekstowe przeznaczone na liczbę spójnych grafów w siatce.
     */
    private TextField textFieldSubgraphCount;
    /**
     * Pole tekstowe przeznaczone na zakres wartości wag na krawędziach.
     */
    private TextField textFieldWeightRange;

    // range field

    /**
     * Etykieta lewej granicy zakresu wartości wag na krawędziach.
     */
    private Label labelEdgeRangeMin;
    /**
     * Etykieta prawej granicy zakresu wartości wag na krawędziach.
     */
    private Label labelEdgeRangeMax;
    /**
     * Etykieta lewej granicy zakresu wartości odległości od wierzchołka początkowego.
     */
    private Label labelNodeRangeMin;
    /**
     * Etykieta prawej granicy zakresu wartości odległości od wierzchołka początkowego.
     */
    private Label labelNodeRangeMax;

    /**
     * Uruchamia okno interfejsu graficznego aplikacji i obsługuje zdarzenia w programie.
     *
     * @param stage scena, na której jest wyświetlany interfejs graficzny
     */
    @Override
    public void start(Stage stage) {
        stage.setTitle("SGraph");
        stage.setResizable(false);

        Validator validator = new Validator();
        AtomicBoolean wasPathDrawn = new AtomicBoolean(false);

        // GUI creation
        // text labels

        Label labelColumnTextField = new Label("# of columns");
        Label labelRowTextField = new Label("# of rows");
        Label labelSubgraphTextField = new Label("# of subgraphs");
        Label labelRangeTextField = new Label("Weight range");

        // text fields

        textFieldColumnCount = new TextField(Integer.toString(DEFAULT_COLUMN_COUNT));

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
        buttonGenerate.getStyleClass().add("big-button");

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
                graph = GraphGenerator.generateGraph(col, row, sub, min, max);
            } catch (Exception e) // if graph generation still SOMEHOW failed
            {
                System.err.println("Graph generation failed - error message: " + e.getMessage());
                return;
            }

            if (!graph.isConnected())
                System.out.println("Graph is not connected - detected fragments: " + graph.getSubgraphCount());

            pf = null; // clearing PathFinder from previous usages;
            setNodeRangeLabels();
            setEdgeRangeLabels();

            draw(graph.getColumnCount(), graph.getRowCount());
        });

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

            if (graph.getSubgraphCount() != 1)
                System.out.println("Graph is not connected - detected fragments: " + graph.getSubgraphCount());

            pf = null; // clearing up PathFinder from previous graph usages
            setNodeRangeLabels();
            setEdgeRangeLabels();

            draw(graph.getColumnCount(), graph.getRowCount());
        });

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

                drawNodes(posX + posY * graph.getColumnCount());
                System.out.println("Chosen node: number " + pf.getStartNodeIndex());
            } else if (event.getButton() == MouseButton.SECONDARY) {
                if (pf == null) // no node chosen
                    return;

                drawPath(posX + posY * graph.getColumnCount());

                wasPathDrawn.set(true);
            }

        });

        // bottom bar

        labelEdgeRangeMin = new Label("MIN");
        labelEdgeRangeMin.getStyleClass().addAll("wide-label", "edge-min");

        labelEdgeRangeMax = new Label("MAX");
        labelEdgeRangeMax.getStyleClass().addAll("wide-label", "edge-max");

        Label labelEdgeRangeTitle = new Label("Edge color scale");
        labelEdgeRangeTitle.getStyleClass().addAll("wide-label", "edge-text");

        HBox edgeRangeContainer = new HBox(0, labelEdgeRangeMin, labelEdgeRangeTitle, labelEdgeRangeMax);

        labelNodeRangeMin = new Label("MIN");
        labelNodeRangeMin.getStyleClass().addAll("wide-label", "node-min");

        labelNodeRangeMax = new Label("MAX");
        labelNodeRangeMax.getStyleClass().addAll("wide-label", "node-max");

        Label labelNodeRangeTitle = new Label("Node color scale");
        labelNodeRangeTitle.getStyleClass().addAll("wide-label", "node-text");

        HBox nodeRangeContainer = new HBox(0, labelNodeRangeMin, labelNodeRangeTitle, labelNodeRangeMax);

        Image colorScale = createColorScale();
        ImageView scaleContainer = new ImageView(colorScale);

        VBox bottomBar = new VBox(PADDING / 2, edgeRangeContainer, scaleContainer, nodeRangeContainer);

        root.getChildren().addAll(topBar, canvas, bottomBar);

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/buttons.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/labels.css")).toExternalForm());
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("styles/text-fields.css")).toExternalForm());

        stage.setScene(scene);
        stage.show();
    }

    /**
     * Uruchamia okno interfejsu graficznego.
     *
     * @param args argumenty wywołania programu
     */
    public static void main(String[] args) {
        launch();
    }

    /**
     * Rysuje graf o określonych wymiarach i zabarwia krawędzie względem zakresu wartości.
     *
     * @param columnCount liczba kolumn w siatce
     * @param rowCount    liczba wierszy w siatce
     */
    public void draw(int columnCount, int rowCount) {

        gc.clearRect(0, 0, CANVAS_RESOLUTION, CANVAS_RESOLUTION);
        gc.setFill(Color.BLACK);

        // scale
        double ovalR = columnCount > rowCount ? (CANVAS_RESOLUTION - 2 * PADDING) / (2 * columnCount + (LINE_LENGTH_PROPORTION - 2.0) * (columnCount - 1)) : (CANVAS_RESOLUTION - 2 * PADDING) / (2 * rowCount + (LINE_LENGTH_PROPORTION - 2.0) * (rowCount - 1));

        double edgeLength = LINE_LENGTH_PROPORTION * ovalR; // edge length

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(LINE_WIDTH_PROPORTION * ovalR);

        int parsedNodeIndex, adhNodeIndex;

        for (int j = 0; j < rowCount; j++) {
            for (int i = 0; i < columnCount; i++) {
                parsedNodeIndex = j * columnCount + i;
                // vertical connection
                if ((adhNodeIndex = checkDown(parsedNodeIndex)) != -1) {
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getEdgeOnNodeConnection(parsedNodeIndex, adhNodeIndex)));
                    gc.beginPath();
                    gc.moveTo(PADDING + ovalR + i * edgeLength, PADDING + ovalR + j * edgeLength);
                    gc.lineTo(PADDING + ovalR + i * edgeLength + 0, PADDING + ovalR + j * edgeLength + edgeLength);
                    gc.stroke();
                    gc.closePath();
                }
                // horizontal connection
                if ((adhNodeIndex = checkRight(parsedNodeIndex)) != -1) {
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getEdgeOnNodeConnection(parsedNodeIndex, adhNodeIndex)));
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

    /**
     * Zwraca indeks wierzcholka znajdującego się pod sprawdzanym wierzchołkiem i sąsiadującego z nim.
     * Jeżeli nie ma takiego wierzchołka, to zwraca -1.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return indeks połączonego wierzchołka pod sprawdzanym wierzchołkiem
     */
    private int checkDown(int nodeIndex) {
        ArrayList<Integer> connectedNodeIndexes = graph.getConnectedNodeIndexes(nodeIndex);

        for (int parsedNodeIndex : connectedNodeIndexes) {
            if (nodeIndex + graph.getColumnCount() < graph.getRowCount() * graph.getColumnCount() && nodeIndex + graph.getColumnCount() == parsedNodeIndex)
                return parsedNodeIndex;
        }

        return -1;
    }

    /**
     * Zwraca indeks wierzcholka znajdującego się na prawo od sprawdzanego wierzchołka i sąsiadującego z nim.
     * Jeżeli nie ma takiego wierzchołka, to zwraca -1.
     *
     * @param nodeIndex indeks sprawdzanego wierzchołka
     * @return indeks połączonego wierzchołka na prawo od sprawdzanego wierzchołka
     */
    private int checkRight(int nodeIndex) {
        ArrayList<Integer> connectedNodeIndexes = graph.getConnectedNodeIndexes(nodeIndex);

        for (int parsedNodeIndex : connectedNodeIndexes) {
            if (nodeIndex + 1 == parsedNodeIndex && nodeIndex / graph.getColumnCount() == parsedNodeIndex / graph.getColumnCount())
                return parsedNodeIndex;
        }

        return -1;
    }

    /**
     * Wyznacza najkrótsze ścieżki do wybranego wierzchołka w grafie i zabarwia wierzchołki względem zakresu wartości odległości od wierzchołka początkowego.
     *
     * @param startNodeIndex indeks wierzchołka początkowego
     */
    private void drawNodes(int startNodeIndex) {
        pf = new PathFinder(graph, startNodeIndex);
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
                if (pf.getDistanceToNode(j * graph.getColumnCount() + i) != -1) {
                    gc.setFill(pf.getNodeValueRange().getHSBValue(pf.getDistanceToNode(j * graph.getColumnCount() + i)));
                } else {
                    gc.setFill(Color.BLACK); // doesn't colour nodes which are not connected
                }

                gc.fillOval(PADDING + i * edgeLength, PADDING + j * edgeLength, ovalR * 2, ovalR * 2);
            }
        }
    }

    /**
     * Tworzy obraz skali kolorów od niebieskiego do czerwonego.
     *
     * @return obraz skali kolorów
     */
    private Image createColorScale() {

        WritableImage scale = new WritableImage(WINDOW_WIDTH, ITEM_HEIGHT);
        PixelWriter pw = scale.getPixelWriter();
        Range r = new Range(0, 100);

        for (int x = 0; x < WINDOW_WIDTH; x++) {
            double value = 100.0 * x / WINDOW_WIDTH;
            Color color = r.getHSBValue(value);

            for (int y = 0; y < ITEM_HEIGHT; y++) {
                pw.setColor(x, y, color);
            }
        }

        return scale;
    }

    /**
     * Ustawia wartości etykiet zakresu wartości wag na krawędziach.
     * Jeżeli graf nie został jeszcze wygenerowany, ustawia ich wartości odpowiednio na "MIN" i "MAX".
     */
    private void setEdgeRangeLabels() {
        labelEdgeRangeMin.setText(graph == null ? "MIN" : Double.toString(graph.getEdgeValueRange().getMin()));
        labelEdgeRangeMax.setText(graph == null ? "MAX" : Double.toString(graph.getEdgeValueRange().getMax()));
    }

    /**
     * Ustawia wartości etykiet zakresu odległości od wierzchołka początkowego.
     * Jeżeli żaden wierzchołek nie został jeszcze wybrany, ustawia ich wartości odpowiednio na "MIN" i "MAX".
     */
    private void setNodeRangeLabels() {
        labelNodeRangeMin.setText(pf == null ? "MIN" : Double.toString(pf.getNodeValueRange().getMin()));
        labelNodeRangeMax.setText(pf == null ? "MAX" : Double.toString(pf.getNodeValueRange().getMax()));
    }

    /**
     * Rysuje drogę od wierzchołka początkowego do wybranego wierzchołka. Wypisuje jej wartość oraz ciąg indeksów do okna konsoli.
     * Jeżeli droga między wierzchołkami nie istnieje, wypisuje odpowiedni komunikat i kończy działanie.
     *
     * @param clickedNodeIndex indeks wierzchołka do którego zostanie narysowana droga
     */
    private void drawPath(int clickedNodeIndex) {
        gc.setFill(Color.DARKSLATEGRAY);

        // scale
        double ovalR = graph.getColumnCount() > graph.getRowCount() ? (CANVAS_RESOLUTION - 2 * PADDING) / (2 * graph.getColumnCount() + (LINE_LENGTH_PROPORTION - 2.0) * (graph.getColumnCount() - 1)) : (CANVAS_RESOLUTION - 2 * PADDING) / (2 * graph.getRowCount() + (LINE_LENGTH_PROPORTION - 2.0) * (graph.getRowCount() - 1));

        double edgeLength = LINE_LENGTH_PROPORTION * ovalR; // edge length

        gc.setStroke(Color.DARKSLATEGRAY);
        gc.setLineWidth(LINE_WIDTH_PROPORTION * ovalR);

        LinkedList<Integer> path = pf.getIndexPathToNode(clickedNodeIndex);

        if (path == null) {
            System.err.printf("There is not path between nodes %d and %d.%n", pf.getStartNodeIndex(), clickedNodeIndex);
            return;
        }

        MoveDirection move;
        int x = path.get(0) % graph.getColumnCount();
        int y = path.get(0) / graph.getColumnCount();

        gc.fillOval(PADDING + x * edgeLength, PADDING + y * edgeLength, ovalR * 2, ovalR * 2);
        gc.beginPath();

        for (int i = 1; i < path.size(); i++) {
            move = getDirection(path.get(i - 1), path.get(i), graph.getColumnCount(), graph.getRowCount());

            if (move == MoveDirection.DOWN) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength + edgeLength);
            } else if (move == MoveDirection.UP) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength - edgeLength);
            } else if (move == MoveDirection.RIGHT) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength + edgeLength, PADDING + ovalR + y * edgeLength);
            } else if (move == MoveDirection.LEFT) {
                gc.moveTo(PADDING + ovalR + x * edgeLength, PADDING + ovalR + y * edgeLength);
                gc.lineTo(PADDING + ovalR + x * edgeLength - edgeLength, PADDING + ovalR + y * edgeLength);
            } else {
                System.err.println("An unexpected error occurred while drawing a path.");
                return;
            }

            x = path.get(i) % graph.getColumnCount();
            y = path.get(i) / graph.getColumnCount();
            gc.fillOval(PADDING + x * edgeLength, PADDING + y * edgeLength, ovalR * 2, ovalR * 2);
        }
        gc.stroke();
        gc.closePath();

        System.out.printf("Distance between nodes %d and %d: %g%n", pf.getStartNodeIndex(), clickedNodeIndex, pf.getDistanceToNode(clickedNodeIndex));
        System.out.printf("Path: %s%n", pf.getPathToNode(clickedNodeIndex));
    }
}
