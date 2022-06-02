/**
 * Moduł aplikacji SGraph generującej ważony graf nieskierowany w postaci siatki i udostępniający działanie algorytmów BFS i Dijkstry.
 */
module org.sgraph {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.synedra.validatorfx;


    opens org.sgraph to javafx.fxml;
    exports org.sgraph;
}