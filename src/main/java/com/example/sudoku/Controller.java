package com.example.sudoku;

import com.example.sudoku.database.JokesDatabase;
import com.example.sudoku.database.ResultDatabase;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static com.example.sudoku.ApplicationRunner.sceneGame;
import static com.example.sudoku.ApplicationRunner.sceneMenu;
import static com.example.sudoku.BacktrackingAlgorithm.*;
import static com.example.sudoku.BacktrackingAlgorithm.isWin;

public class Controller {

    private static final String SUDOKU_CSS = "sudoku.css";
    private static final int JOKES_AMOUNT = 35;

    private static int[][] boardData = new int[BOARD_SIZE][BOARD_SIZE];
    private static GridPane board = new GridPane();
    private static LocalTime startGame;
    private static int difficult;
    private static int currentJokeNumber = 1;
    private static int jokeId;

    private Stage stage;
    private Timeline clock;
    private Label labelClock = new Label("00:00");

    public void switchToGameSceneEasy(Event event) {
        difficult = 1;
        readCurrentJokeIdFromFile();
        VBox field = createGameField();
        startGame = LocalTime.now();
        sceneGame = new Scene(field);
        sceneGame.getStylesheets().add(SUDOKU_CSS);
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(sceneGame);
        stage.show();
    }

    public void switchToGameSceneMedium(Event event) {
        difficult = 2;
        readCurrentJokeIdFromFile();
        VBox field = createGameField();
        startGame = LocalTime.now();
        sceneGame = new Scene(field);
        sceneGame.getStylesheets().add(SUDOKU_CSS);
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(sceneGame);
        stage.show();
    }

    public void switchToGameSceneHard(Event event) {
        difficult = 3;
        readCurrentJokeIdFromFile();
        VBox field = createGameField();
        startGame = LocalTime.now();
        sceneGame = new Scene(field);
        sceneGame.getStylesheets().add(SUDOKU_CSS);
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(sceneGame);
        stage.show();
    }

    public void switchToMenuScene(Event event) throws IOException {
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(sceneMenu);
        stage.show();
    }

    public VBox createGameField() {
        generateBoardData(answersToWin());
        boardData = copyInitBoard();

        createSudokuGrid();

        initClock();

        Button buttonNewGame = new Button("New Game");
        buttonNewGame.setFont(new Font(16));
        buttonNewGame.setOnAction(event -> {
            clock.play();
            ResultDatabase.insertResultSQL();
            generateBoardData(answersToWin());
            boardData = copyInitBoard();
            createSudokuGrid();
            startGame = LocalTime.now();
        });

        Button buttonMenu = new Button("Menu");
        buttonMenu.setFont(new Font(16));
        buttonMenu.setOnAction(event -> {
            try {
                clock.stop();
//                ResultDatabase.insertResultSQL();
                switchToMenuScene(event);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        Button buttonJokeOfTheDay = new Button("Joke of The Day");
        buttonJokeOfTheDay.setFont(new Font(16));
        buttonJokeOfTheDay.setOnAction(event -> {
//            showJokeAlert();
            showMessageAlert("joke");
            if (currentJokeNumber < JOKES_AMOUNT) {
                currentJokeNumber++;
            } else {
                currentJokeNumber = 1;
            }
        });

        HBox buttons = new HBox(buttonNewGame, buttonMenu, buttonJokeOfTheDay);
        buttons.setAlignment(Pos.TOP_CENTER);

        HBox.setMargin(buttonNewGame, new Insets(0, 25, 15, 25));
        HBox.setMargin(buttonMenu, new Insets(0, 25, 15, 25));
        HBox.setMargin(buttonJokeOfTheDay, new Insets(0, 25, 15, 25));

        VBox.setMargin(labelClock, new Insets(0));

        VBox field = new VBox(board, labelClock, buttons);
        field.setAlignment(Pos.TOP_CENTER);
        field.setSpacing(15.0);
        return field;
    }

    public void showSudokuRules() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText("Sudoku rules");

        String rules = "От игрока требуется заполнить свободные\n" +
                "клетки цифрами от 1 до 9 так, чтобы в каждой\n" +
                "строке, в каждом столбце и в каждом малом квадрате\n" +
                "3×3 каждая цифра встречалась бы только один раз.";

        TextArea textArea = new TextArea(rules);
        textArea.setEditable(false);
        textArea.setFont(new Font("Calibri", 16));
        textArea.setPrefSize(450, 300);
        alert.getDialogPane().setContent(textArea);

        alert.showAndWait();
    }

    public static int answersToWin() {
        return switch (difficult) {
            case 1 -> 15;
            case 2 -> 30;
            case 3 -> 45;
            default -> 3;
        };
    }

    public static long getGameTimeInSeconds() {
        return Duration.between(startGame, LocalTime.now()).getSeconds();
    }

    public static int getDifficult() {
        return difficult;
    }

    public static int getJokeId() {
        return jokeId;
    }

    public static int getCurrentJokeNumber() {
        return currentJokeNumber;
    }

    private void createSudokuGrid() {
        PseudoClass right = PseudoClass.getPseudoClass("right");
        PseudoClass bottom = PseudoClass.getPseudoClass("bottom");

        for (int column = 0; column < BOARD_SIZE; column++) {
            for (int row = 0; row < BOARD_SIZE; row++) {
                StackPane cell = new StackPane();
                cell.getStyleClass().add("cell");
                cell.pseudoClassStateChanged(right, column == 2 || column == 5);
                cell.pseudoClassStateChanged(bottom, row == 2 || row == 5);

                cell.getChildren().add(createTextField(column, row));

                board.add(cell, column, row);
            }
        }
    }

    private TextField createTextField(int column, int row) {
        TextField textField = new TextField();

        textField.setTextFormatter(new TextFormatter<Integer>(value -> {
            if (value.getControlNewText().matches("\\d?")) {
                return value ;
            } else {
                return null ;
            }
        }));

        String text = boardData[row][column] != 0 ? String.valueOf(boardData[row][column]) : null;
        if (boardData[row][column] != NO_VALUE) {
            textField.setEditable(false);
        }
        textField.setText(text);
        textField.setOnAction(event -> {
            if (checkAnswer(List.of(column, row, Integer.parseInt(textField.getText())))) {
                textField.getStyleClass().removeIf(style -> style.equals("wrongAnswer"));
                textField.getStyleClass().add("correctAnswer");
                textField.setEditable(false);
                textField.setOnAction(event1 -> {});
            } else {
//                textField.getStyleClass().set(2, "wrongAnswer");
                textField.getStyleClass().add("wrongAnswer");
            }
            if (isWin()) {
                clock.stop();
                showMessageAlert("win");
            }
        });

        return textField;
    }

    private void showMessageAlert(String messageType) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        String title = "";
        String headerText = "Special joke for you ;)";
        String joke = "";

        if (messageType.equalsIgnoreCase("joke")) {
            joke = JokesDatabase.getJokeFromDB(currentJokeNumber);
            title = "Joke of The Day";
        } else if (messageType.equalsIgnoreCase("win")) {
            jokeId = new Random().nextInt(1, JOKES_AMOUNT + 1);
            joke = JokesDatabase.getJokeFromDB(jokeId);
            headerText = "Congratulation! You win :)\nJoke for the win:\n";
            title = "Result";
        }
        alert.setTitle(title);
        alert.setHeaderText(headerText);

        TextArea textArea = new TextArea(joke);
        textArea.setEditable(false);
        textArea.setFont(new Font("Calibri", 14));
        textArea.setPrefSize(480, 300);
        alert.getDialogPane().setContent(textArea);

        alert.showAndWait();
    }

    private void readCurrentJokeIdFromFile() {
        Path path = Path.of("src", "main", "java", "com", "example",
                "sudoku", "database", "currentJokeId.txt");
        if (Files.exists(path)) {
            try {
                String readString = Files.readString(path);
                currentJokeNumber = readString.equalsIgnoreCase("") ? 1
                        : Integer.parseInt(readString);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void initClock() {
        clock = new Timeline(new KeyFrame(javafx.util.Duration.ZERO, e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("mm:ss");
            labelClock.getStyleClass().add("clock");
            labelClock.setFont(Font.font("Calibri", 24));
            labelClock.setText(LocalTime.now()
                    .minusSeconds(startGame.getSecond())
                    .minusMinutes(startGame.getMinute())
                    .format(formatter));
        }), new KeyFrame(javafx.util.Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }
}