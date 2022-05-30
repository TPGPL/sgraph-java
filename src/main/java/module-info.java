module org.sgraph {
    requires javafx.controls;
    requires javafx.fxml;
    requires net.synedra.validatorfx;


    opens org.sgraph to javafx.fxml;
    exports org.sgraph;
}