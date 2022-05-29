package org.sgraph;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GUI extends Application {
    private Graph graph;
    private GraphicsContext gc;
    private final int size = 800;
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

        //Tworzenie górnego nagłówka
        //Nagłówki guzików
        Label labelColumnTextField = new Label("# of columns");
        labelColumnTextField.setPrefWidth(size * 0.6 / 4);
        labelColumnTextField.setPrefHeight(20);
        labelColumnTextField.setAlignment(Pos.CENTER);

        Label labelRowTextField = new Label("# of rows");
        labelRowTextField.setPrefWidth(size * 0.6 / 4);
        labelRowTextField.setPrefHeight(20);
        labelRowTextField.setAlignment(Pos.CENTER);

        Label labelSubgraphTextField = new Label("# of subgraph");
        labelSubgraphTextField.setPrefWidth(size * 0.6 / 4);
        labelSubgraphTextField.setPrefHeight(20);
        labelSubgraphTextField.setAlignment(Pos.CENTER);

        Label labelRangeTextField = new Label("Weight range");
        labelRangeTextField.setPrefWidth(size * 0.6 / 4);
        labelRangeTextField.setPrefHeight(20);
        labelRangeTextField.setAlignment(Pos.CENTER);

        //Linie przycisków
        HBox upHeadLine = new HBox(10, labelColumnTextField, labelRowTextField, labelSubgraphTextField, labelRangeTextField);

        //Tworzenie dolnego nagłówka
        textFieldColumnCount = new TextField("");
        textFieldColumnCount.setPrefWidth(size * 0.6 / 4);
        textFieldColumnCount.setPrefHeight(20);
        textFieldColumnCount.setAlignment(Pos.CENTER);

        textFieldRowCount = new TextField("");
        textFieldRowCount.setPrefWidth(size * 0.6 / 4);
        textFieldRowCount.setPrefHeight(20);
        textFieldRowCount.setAlignment(Pos.CENTER);

        textFieldSubgraphCount = new TextField("");
        textFieldSubgraphCount.setPrefWidth(size * 0.6 / 4);
        textFieldSubgraphCount.setPrefHeight(20);
        textFieldSubgraphCount.setAlignment(Pos.CENTER);

        textFieldWeightRange = new TextField("");
        textFieldWeightRange.setPrefWidth(size * 0.6 / 4);
        textFieldWeightRange.setPrefHeight(20);
        textFieldWeightRange.setAlignment(Pos.CENTER);

        //Guzik do generowania
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
                //Leniwa funkcja do odczytania wag, ale działa
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
            draw(graph.getColumnCount(), graph.getRowCount());
            enableAllButtons();
        });
        buttonGenerate.setPrefWidth(size * 0.3 / 3);
        buttonGenerate.setPrefHeight(20);
        buttonGenerate.setAlignment(Pos.CENTER);

        //Guzik do wczytania
        buttonFileOpen = new Button("Open from file...");
        buttonFileOpen.setOnAction(actionEvent -> {
            disableAllButtons();
            fileChooser.setTitle("Load from...");
            File file = fileChooser.showOpenDialog(stage);
            //Jeśli zamknięto okno i nie podano pliku
            if (file == null) {
                enableAllButtons();
                return;
            }
            //TODO try catch, jeśli niepoprawny format pliku
            graph = GraphReader.readFromFile(file);

            draw(graph.getColumnCount(), graph.getRowCount());
            enableAllButtons();
        });
        buttonFileOpen.setPrefWidth(size * 0.3 / 3);
        buttonFileOpen.setPrefHeight(20);
        buttonFileOpen.setAlignment(Pos.CENTER);

        //Guzik do zapisu
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
            enableAllButtons();
        });
        buttonFileSave.setPrefWidth(size * 0.3 / 3);
        buttonFileSave.setPrefHeight(20);
        buttonFileSave.setAlignment(Pos.CENTER);

        HBox upBottomLine = new HBox(10, textFieldColumnCount, textFieldRowCount, textFieldSubgraphCount, textFieldWeightRange, buttonGenerate, buttonFileOpen, buttonFileSave);
        //Rysowanie
        Canvas canvas = new Canvas(size, size);
        gc = canvas.getGraphicsContext2D();

        //Ustawienia obiektu do wczytywania z pliku
        fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setInitialFileName("graph.txt"); //Domyślna nazwa zapisanego pliku

        FlowPane root = new FlowPane();

        root.getChildren().add(upHeadLine);
        root.getChildren().add(upBottomLine);
        root.getChildren().add(canvas);

        stage.setScene(new Scene(root, size, size + 50));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public void draw(int columnCount, int rowCount) {

        gc.clearRect(0, 0, size, size);
        gc.setFill(Color.BLACK);
        //Proporcje połączenia względem promienia wierzchołka
        double lineWidthProportion = 2.0 / 3.0;
        double lineLengthProportion = 4.0;

        //Położenie startowe, przerwa od krawędzi ekranu
        double gap = 10.0;//Stałe

        //Skala, Długość promienia punktu,Przesunięcie połączenia
        double ovalR = columnCount > rowCount ? (size - 2 * gap) / (2 * columnCount + (lineLengthProportion - 2.0) * (columnCount - 1)) : (size - 2 * gap) / (2 * rowCount + (lineLengthProportion - 2.0) * (rowCount - 1));

        //długość krawędzi,Odległość międzypunktami
        double edgeLength = lineLengthProportion * ovalR;
        //jego szerokość
        double rectW = lineWidthProportion * ovalR;

        gc.setStroke(Color.BLACK); //Domyślny kolor
        gc.setLineWidth(rectW);

        int parsedNodeIndex, adhNodeIndex;

        //Rysowanie połączeń
        for (int j = 0; j < rowCount; j++) {
            for (int i = 0; i < columnCount; i++) {
                parsedNodeIndex = j * columnCount + i;
                //Rysowanie połączenia pionowego
                if ((adhNodeIndex = checkDown(parsedNodeIndex)) != -1) {
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getNode(parsedNodeIndex).getEdgeOnConnection(graph.getNode(adhNodeIndex))));
                    gc.beginPath();
                    gc.moveTo(gap + ovalR + i * edgeLength, gap + ovalR + j * edgeLength);
                    gc.lineTo(gap + ovalR + i * edgeLength + 0, gap + ovalR + j * edgeLength + edgeLength);
                    gc.stroke();
                    gc.closePath();
                }
                //Rysowanie połączenia poziomego
                if ((adhNodeIndex = checkRight(parsedNodeIndex)) != -1) {
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getNode(parsedNodeIndex).getEdgeOnConnection(graph.getNode(adhNodeIndex))));
                    gc.beginPath();
                    gc.moveTo(gap + ovalR + i * edgeLength, gap + ovalR + j * edgeLength);
                    gc.lineTo(gap + ovalR + i * edgeLength + edgeLength, gap + ovalR + j * edgeLength);
                    gc.stroke();
                    gc.closePath();
                }
            }
        }

        //Rysowanie punktu, wydzieliłem, ale można połączyć z pętlą wyżej
        for (int j = 0; j < rowCount; j++) {
            for (int i = 0; i < columnCount; i++) {
                //gc.setFill(Color.CRIMSON);
                gc.fillOval(gap + i * edgeLength, gap + j * edgeLength, ovalR * 2, ovalR * 2); //Punkt
            }
        }
    }

    //Sprawdzanie kierunku połączenia
    private int checkDown(int index) {
        ArrayList<Node> connectedNodes = graph.getNode(index).getConnectedNodes();
        for (Node n : connectedNodes) {
            if (index + graph.getColumnCount() < graph.getRowCount() * graph.getColumnCount() && index + graph.getColumnCount() == n.getIndex())
                return n.getIndex();
        }
        return -1;
    }

    //Sprawdzanie kierunku połączenia
    private int checkRight(int index) {
        ArrayList<Node> connectedNodes = graph.getNode(index).getConnectedNodes();
        for (Node n : connectedNodes) {
            if (index + 1 == n.getIndex() && index / graph.getColumnCount() == n.getIndex() / graph.getColumnCount())
                return n.getIndex();
        }
        return -1;
    }


    //Wyłącza guziki na czas trwania rysowania
    private void disableAllButtons() {
        buttonGenerate.setDisable(true);
        buttonFileOpen.setDisable(true);
        buttonFileSave.setDisable(true);
    }

    //Włącza po wszystkim guziki
    private void enableAllButtons() {
        buttonGenerate.setDisable(false);
        buttonFileOpen.setDisable(false);
        buttonFileSave.setDisable(false);
    }
}
