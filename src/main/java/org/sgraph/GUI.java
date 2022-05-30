package org.sgraph;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GUI extends Application {
    private static Graph graph;
    private static GraphicsContext gc;
    private static final int size=600;
    private static FileChooser fileChooser;

    //Nagłówki guzików
    private static Label labelForNumberOfColumns;
    private static Label labelForNumberOfRows;
    private static Label labelForNumberOfSubGraphs;
    private static Label labelForWeightRange;

    //Pola tekstowe
    private static TextField textFieldForNumberOfColumns;
    private static TextField textFieldForNumberOfRows;
    private static TextField textFieldForNumberOfSubGraphs;
    private static TextField textFieldForWeightRange;

    //Guziki
    private static Button buttonGen;
    private static Button buttonOpen;
    private static Button buttonSave;

    //Linie przycisków
    private static HBox upHeadLine;
    private static HBox upBottomLine;

    //Rysowanie
    private static Canvas canvas;
    private static FlowPane root;

    @Override
    public void start(Stage stage) throws Exception {
        stage.setTitle("Test");

        //Tworzenie górnego nagłówka
        labelForNumberOfColumns=new Label("# of columns");
        labelForNumberOfColumns.setPrefWidth(size*0.6/4);
        labelForNumberOfColumns.setPrefHeight(20);
        labelForNumberOfColumns.setAlignment(Pos.CENTER);

        labelForNumberOfRows=new Label("# of rows");
        labelForNumberOfRows.setPrefWidth(size*0.6/4);
        labelForNumberOfRows.setPrefHeight(20);
        labelForNumberOfRows.setAlignment(Pos.CENTER);

        labelForNumberOfSubGraphs=new Label("# of subgraph");
        labelForNumberOfSubGraphs.setPrefWidth(size*0.6/4);
        labelForNumberOfSubGraphs.setPrefHeight(20);
        labelForNumberOfSubGraphs.setAlignment(Pos.CENTER);

        labelForWeightRange=new Label("Weight range");
        labelForWeightRange.setPrefWidth(size*0.6/4);
        labelForWeightRange.setPrefHeight(20);
        labelForWeightRange.setAlignment(Pos.CENTER);

        upHeadLine =new HBox(10,labelForNumberOfColumns,labelForNumberOfRows,labelForNumberOfSubGraphs,labelForWeightRange);

        //Tworzenie dolnego nagłówka
        textFieldForNumberOfColumns=new TextField("");
        textFieldForNumberOfColumns.setPrefWidth(size*0.6/4);
        textFieldForNumberOfColumns.setPrefHeight(20);
        textFieldForNumberOfColumns.setAlignment(Pos.CENTER);

        textFieldForNumberOfRows=new TextField("");
        textFieldForNumberOfRows.setPrefWidth(size*0.6/4);
        textFieldForNumberOfRows.setPrefHeight(20);
        textFieldForNumberOfRows.setAlignment(Pos.CENTER);

        textFieldForNumberOfSubGraphs=new TextField("");
        textFieldForNumberOfSubGraphs.setPrefWidth(size*0.6/4);
        textFieldForNumberOfSubGraphs.setPrefHeight(20);
        textFieldForNumberOfSubGraphs.setAlignment(Pos.CENTER);

        textFieldForWeightRange=new TextField("");
        textFieldForWeightRange.setPrefWidth(size*0.6/4);
        textFieldForWeightRange.setPrefHeight(20);
        textFieldForWeightRange.setAlignment(Pos.CENTER);

        //Guzik do generowania
        buttonGen=new Button("Generate");
        buttonGen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                turnOfButton();
                int col,row,sub;
                double min,max;
                String stringColumns=textFieldForNumberOfColumns.getText();
                String stringRows=textFieldForNumberOfRows.getText();
                String stringSubGraph=textFieldForNumberOfSubGraphs.getText();
                String stringRange=textFieldForWeightRange.getText();
                try{
                    col=Integer.parseInt(stringColumns);
                    row=Integer.parseInt(stringRows);
                    sub=Integer.parseInt(stringSubGraph);
                    //Leniwa funkcja do odczytania wag, ale działa
                    min=Double.parseDouble(stringRange.split("-")[0]);
                    max=Double.parseDouble(stringRange.split("-")[1]);

                }catch(NumberFormatException e){
                    System.err.println("Text fields have not corrected format");
                    turnOnButton();
                    return;
                }catch(ArrayIndexOutOfBoundsException ee){
                    System.err.println("Range do not have -, or not correted format");
                    turnOnButton();
                    return;
                }
                //TODO try catch, jeśli niepoprawne dane do grafu
                graph=GraphGenerator.generate(col,row,sub,min,max);
                draw(graph.getColumnCount(),graph.getRowCount());
                turnOnButton();
            }
        });
        buttonGen.setPrefWidth(size*0.3/3);
        buttonGen.setPrefHeight(20);
        buttonGen.setAlignment(Pos.CENTER);

        //Guzik do wczytania
        buttonOpen=new Button("Open from file...");
        buttonOpen.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                turnOfButton();
                fileChooser.setTitle("Load from...");
                File file=fileChooser.showOpenDialog(stage);
                //Jeśli zamknięto okno i nie podano pliku
                if(file==null){
                    turnOnButton();
                    return;
                }
                //TODO try catch, jeśli niepoprawny format pliku
                graph=GraphReader.readFromFile(file);

                draw(graph.getColumnCount(),graph.getRowCount());
                turnOnButton();
            }
        });
        buttonOpen.setPrefWidth(size*0.3/3);
        buttonOpen.setPrefHeight(20);
        buttonOpen.setAlignment(Pos.CENTER);

        //Guzik do zapisu
        buttonSave=new Button("Save to file...");
        buttonSave.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                turnOfButton();
                fileChooser.setTitle("Save to...");
                File file=fileChooser.showSaveDialog(stage);
                if(file==null){
                    turnOnButton();
                    return;
                }
                //TODO try catch
                try {
                    graph.readToFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                    turnOnButton();
                    return;
                }
                turnOnButton();
            }
        });
        buttonSave.setPrefWidth(size*0.3/3);
        buttonSave.setPrefHeight(20);
        buttonSave.setAlignment(Pos.CENTER);

        upBottomLine=new HBox(10,textFieldForNumberOfColumns,textFieldForNumberOfRows,textFieldForNumberOfSubGraphs,textFieldForWeightRange,buttonGen,buttonOpen,buttonSave);

        canvas = new Canvas(size, size);
        gc = canvas.getGraphicsContext2D();

        //Ustawienia obiektu do wczytywania z pliku
        fileChooser=new FileChooser();
        fileChooser.setInitialDirectory(new File(System. getProperty("user.dir")));
        fileChooser.setInitialFileName("graph.txt"); //Domyślna nazwa zapisanego pliku

        root = new FlowPane();

        //Obsługa myszki
        root.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x,y,r;
                if(graph==null)
                    return; //ignoruj
                x=event.getSceneX()-canvas.getLayoutX();
                y=event.getSceneY()-canvas.getLayoutY();
                //poza canvas
                if(x<0 || y<0 || x>size || y>size)
                    return; //Ignoruj
                int posX,posY,devide;
                //maks
                if(graph.getColumnCount()>graph.getRowCount())
                    devide=graph.getColumnCount();
                else
                    devide=graph.getRowCount();
                r=(size-20.0)/(devide*4-2);
                //Przesunięcie luki (10-r) i znormalizowanie
                posX= (int)((x-10+r)/(4*r));
                posY= (int)((y-10+r)/(4*r));
                //Czy trafiono w punkt
                if(posX<0 || posY<0 || posX>graph.getColumnCount()-1 || posY>graph.getRowCount()-1)
                    return; //ignoruj
                drawNodes(graph.getNode(posX +posY*graph.getColumnCount()));
            }
        });

        root.getChildren().add(upHeadLine);
        root.getChildren().add(upBottomLine);
        root.getChildren().add(canvas);

        stage.setScene(new Scene(root, size, size+50));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    public static void draw(int columns,int verses) {

        gc.clearRect(0,0,size,size);
        gc.setFill(Color.BLACK);
        //Proporcje połączenia względem promienia wierzchołka
        double lineWidthProportion=2.0/3.0;
        double lineLenghtProportion=4.0;

        //Położenie startowe, przerwa od krawędzi ekranu
        double gap = 10.0;//Stałe

        //Skala, Długość promienia punktu,Przesunięcie połączenia
        double ovalR=  columns>verses ? (size-2*gap)/(2*columns+(lineLenghtProportion-2.0)*(columns-1)) : (size-2*gap)/(2*verses+(lineLenghtProportion-2.0)*(verses-1));

        //długość krawędzi,Odległość międzypunktami
        double edgeLenght=lineLenghtProportion*ovalR;
        //jego szerokość
        double rectW=lineWidthProportion*ovalR;

        gc.setStroke(Color.BLACK); //Domyślny kolor
        gc.setLineWidth(rectW);

        int edge,drawing;

        //Rysowanie połączeń
        for (int j = 0; j < verses; j++) {
            for (int i = 0; i < columns; i++) {
                edge=j*columns+i;
                //Rysowanie połączenia pionowego
                if((drawing=checkDown(edge))!=-1) {
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getNode(edge).getEdgeOnConnection(graph.getNode(drawing))));
                    gc.beginPath();
                    gc.moveTo(gap + ovalR + i * edgeLenght,gap + ovalR + j * edgeLenght);
                    gc.lineTo(gap + ovalR + i * edgeLenght + 0,gap + ovalR + j * edgeLenght+edgeLenght);
                    gc.stroke();
                    gc.closePath();
                }
                //Rysowanie połączenia poziomego
                if((drawing=checkRight(edge))!=-1){
                    gc.setStroke(graph.getEdgeValueRange().getHSBValue(graph.getNode(edge).getEdgeOnConnection(graph.getNode(drawing))));
                    gc.beginPath();
                    gc.moveTo(gap + ovalR + i * edgeLenght,gap + ovalR + j * edgeLenght);
                    gc.lineTo(gap + ovalR + i * edgeLenght + edgeLenght,gap + ovalR + j * edgeLenght);
                    gc.stroke();
                    gc.closePath();
                }
            }
        }

        //Rysowanie punktu, wydzieliłem, ale można połączyć z pętlą wyżej
        for (int j = 0; j < verses; j++) {
            for (int i = 0; i < columns; i++) {
                //gc.setFill(Color.CRIMSON);
                gc.fillOval(gap + i * edgeLenght, gap + j * edgeLenght, ovalR*2, ovalR*2); //Punkt
            }
        }
    }

    //Sprawdzanie kierunku połączenia
    private static int checkDown(int edge){
        ArrayList<Node> neighbour=graph.getNode(edge).getConnectedNodes();
        for (Node n:neighbour) {
            if (edge + graph.getColumnCount() < graph.getRowCount() * graph.getColumnCount() && edge + graph.getColumnCount() == n.getIndex())
                return n.getIndex();
        }
        return -1;
    }
    //Sprawdzanie kierunku połączenia
    private static int checkRight(int edge){
        ArrayList<Node> neighbour=graph.getNode(edge).getConnectedNodes();
        for (Node n:neighbour) {
            if (edge + 1 == n.getIndex() && edge / graph.getColumnCount() == n.getIndex() / graph.getColumnCount())
                return n.getIndex();
        }
        return -1;
    }

    private static void drawNodes(Node start){
        //TODO
        //Tutaj powinna ruszyć djikstra
        //djikstra(start)
        int columns= graph.getColumnCount();
        int verses=graph.getRowCount();

        //Proporcje połączenia względem promienia wierzchołka
        double lineWidthProportion=2.0/3.0;
        double lineLenghtProportion=4.0;

        //Położenie startowe, przerwa od krawędzi ekranu
        double gap = 10.0;//Stałe

        //Skala, Długość promienia punktu,Przesunięcie połączenia
        double ovalR=  columns>verses ? (size-2*gap)/(2*columns+(lineLenghtProportion-2.0)*(columns-1)) : (size-2*gap)/(2*verses+(lineLenghtProportion-2.0)*(verses-1));

        //długość krawędzi,Odległość międzypunktami
        double edgeLenght=lineLenghtProportion*ovalR;
        //jego szerokość
        double rectW=lineWidthProportion*ovalR;

        gc.setStroke(Color.BLACK); //Domyślny kolor
        gc.setLineWidth(rectW);

        //Rysowanie punktów
        for (int j = 0; j < verses; j++) {
            for (int i = 0; i < columns; i++) {
                    //gc.setFill(Color.CRIMSON); //Ustawianie koloru
                    gc.fillOval(gap + i * edgeLenght, gap + j * edgeLenght, ovalR*2, ovalR*2); //Punkt
            }
        }
    }


    //Wyłącza guziki na czas trwania rysowania
    private static void turnOfButton(){
        buttonGen.setDisable(true);
        buttonOpen.setDisable(true);
        buttonSave.setDisable(true);
    }
    //Włącza po wszystkim guziki
    private static void turnOnButton(){
        buttonGen.setDisable(false);
        buttonOpen.setDisable(false);
        buttonSave.setDisable(false);
    }
}
