package com.example.sudoku.database;

import com.example.sudoku.BacktrackingAlgorithm;
import com.example.sudoku.Controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;

public class ResultDatabase {

    private static final int COLUMN_AMOUNT = 7;

    private static final String url = "jdbc:h2:D:/Java_projects/db/database";
    private static final String user = "sa";
    private static final String password = "";
    private static final String[] tableHeader = { "RESULT_ID", "START_TIME", "GAME_TIME", "COMPLEXITY",
            "UNRAVELED_NUMBERS", "NUMBERS_TO_WIN", "JOKE_ID" };
    private static Object[][] queriedRecords;

    public static void saveResultInDatabaseAndFile() {
        insertResultSQL();
        selectSQL("SELECT * FROM results");
        filePrintTable(tableHeader, queriedRecords, 2);
    }

    public static void insertResultSQL() {
        Statement statement;
        String queryInsert = "";

        try (final Connection connection = DriverManager.getConnection(url, user, password)) {
            int result_id = countRow(connection) + 1;
            String startGame = LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            long timeInSeconds = Controller.getGameTimeInSeconds();
            int minutes = (int) (timeInSeconds / 60);
            int seconds = (int) (timeInSeconds % 60);
            String gameTime = String.format("%02d:%02d", minutes, seconds);

            String complexity = switch (Controller.getDifficult()) {
                case 1 -> "easy";
                case 2 -> "medium";
                case 3 -> "hard";
                default -> "test";
            };
            int unraveledNumbers = BacktrackingAlgorithm.getCountCorrectAnswers();
            int numberToWin = Controller.answersToWin();
            Integer jokeId = Controller.getJokeId();
            jokeId = jokeId > 0 ? jokeId : null;

            Formatter formatter = new Formatter();
            queryInsert = formatter.format("INSERT INTO results VALUES(%d, '%s', '%s', '%s', %d, %d, %d);",
                    result_id, startGame, gameTime, complexity, unraveledNumbers, numberToWin, jokeId).toString();
            System.out.println(queryInsert);

            statement = connection.createStatement();
            statement.execute(queryInsert);

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public static void selectSQL(String selectSQLStatement) {
        PreparedStatement statement;
        ResultSet resultSet;
        int rows;

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            rows = countRow(connection);

            // Yes there is so let's declare our 2D Object Array...
            queriedRecords = new Object[rows][COLUMN_AMOUNT];

            // And now fill the array...
            statement = connection.prepareStatement(selectSQLStatement);
            resultSet = statement.executeQuery();

            for (int counter = 0; resultSet.next();) {
                queriedRecords[counter][0] = resultSet.getInt("RESULT_ID");
                queriedRecords[counter][1] = resultSet.getString("START_TIME");
                queriedRecords[counter][2] = resultSet.getString("GAME_TIME");
                queriedRecords[counter][3] = resultSet.getString("COMPLEXITY");
                queriedRecords[counter][4] = resultSet.getInt("UNRAVELED_NUMBERS");
                queriedRecords[counter][5] = resultSet.getInt("NUMBERS_TO_WIN");
                queriedRecords[counter][6] = resultSet.getInt("JOKE_ID");
                counter++;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void consolePrintTable(String[] headerData, Object[][] tableData,
                                         int spacesBetweenCells) {
        if (tableData.length == 0) {
            return;
        }

        // Get the widest Cell needed so that all the
        // table cells will be the same when printed.
        int widestCell = getWidestCellLength(headerData, tableData) + spacesBetweenCells;

        String format;
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= COLUMN_AMOUNT; i++) {
            builder.append("%-");
            builder.append(widestCell);
            builder.append("s");
        }
        builder.append("\n");
        format = builder.toString();

        //Print The Header (if any)...
        if (headerData != null && headerData.length > 0) {
            for(int i = 0; i < headerData.length; i++) {
                System.out.printf("%-" + widestCell + "s", headerData[i]);
            }
            System.out.println();
        }

        // Display the Table data...
        for (final Object[] row : tableData) {
            System.out.format(format, row);
        }
    }

    public static void filePrintTable(String[] headerData, Object[][] tableData,
                                         int spacesBetweenCells) {
        if (tableData.length == 0) {
            return;
        }

        // Get the widest Cell needed so that all the
        // table cells will be the same when printed.
        int widestCell = getWidestCellLength(headerData, tableData) + spacesBetweenCells;

        String format;
        StringBuilder builder = new StringBuilder();

        for (int i = 1; i <= COLUMN_AMOUNT; i++) {
            builder.append("%-");
            builder.append(widestCell);
            builder.append("s");
        }
        builder.append("\n");
        format = builder.toString();

        Path path = Path.of("src", "main", "java", "com", "example",
                "sudoku", "database", "resultDBOutput.txt");

        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (Formatter formatter = new Formatter(
                Files.newBufferedWriter(path, StandardOpenOption.WRITE, StandardOpenOption.CREATE))) {
            //Print The Header (if any)...
            if (headerData != null && headerData.length > 0) {
                for (String headerDatum : headerData) {
                    formatter.format("%-" + widestCell + "s", headerDatum);
                }
                formatter.format("\n");
            }

            // Display the Table data...
            for (final Object[] row : tableData) {
                formatter.format(format, row);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int countRow(Connection connection) {
        PreparedStatement statement;
        ResultSet resultSet;
        int resultSetCount = 0;
        String rowCountSQL = "SELECT COUNT(*) AS rCount FROM results;";

        try {
            //Get the number of records within that will be
            //retrieved from your query...
            statement = connection.prepareStatement(rowCountSQL);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                resultSetCount = resultSet.getInt("rCount");
            }
            // Are there records to display?
            if (resultSetCount == 0) {
                // No there isn't
//                System.out.println("There are NO Records!");
                return -1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return resultSetCount;
    }

    private static int getWidestCellLength(String[] headerData, Object[][] tableData) {
        int widestCell = 0;
        for (Object[] tableData1 : tableData) {
            for (int j = 0; j < tableData[0].length; j++) {
                int length = tableData1[j].toString().length();
                if (length > widestCell) {
                    widestCell = length;
                }
            }
        }
        //Now check for the widest in header (if any)
        if (headerData != null && headerData.length > 0) {
            for (int i = 0; i < headerData.length; i++) {
                if (headerData[i].length() > widestCell) {
                    widestCell = headerData[i].length();
                }
            }
        }
        return widestCell;
    }
}
