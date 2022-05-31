package com.example.sudoku;

import com.example.sudoku.database.ResultDatabase;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class ApplicationRunner extends Application {

    public static Scene sceneGame;
    public static Scene sceneMenu;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu-view.fxml"));
        sceneMenu = new Scene(fxmlLoader.load());

        primaryStage.setTitle("Sudoku");
        primaryStage.setResizable(false);
//        primaryStage.setScene(sceneGame);
        primaryStage.setScene(sceneMenu);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        try {
            saveCurrentJokeId();
            ResultDatabase.saveResultInDatabaseAndFile();
        } catch (Exception e) {
            if (!(e instanceof NullPointerException)) {
                e.printStackTrace();
            }
        }

        super.stop();
    }

    private void saveCurrentJokeId() {
        Path path = Path.of("src", "main", "java", "com", "example",
                "sudoku", "database", "currentJokeId.txt");
        try {
            Files.writeString(path, String.valueOf(Controller.getCurrentJokeNumber()),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}