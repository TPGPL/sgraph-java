module org.sgraph {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.sgraph to javafx.fxml;
    exports org.sgraph;
}