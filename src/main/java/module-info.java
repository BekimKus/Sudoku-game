module com.example.sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.sudoku to javafx.fxml;
    exports com.example.sudoku;
}