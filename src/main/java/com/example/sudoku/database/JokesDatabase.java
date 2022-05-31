package com.example.sudoku.database;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JokesDatabase {

    private static final int TEXT_COLUMN_INDEX = 2;

    public static String getJokeFromDB(int id) {
        String url = "jdbc:h2:D:/Java_projects/db/database";
        String user = "sa";
        String password = "";

        String query = "SELECT * FROM jokes";
        String joke = "";

        try (Connection connection = DriverManager.getConnection(url, user, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            for (int i = 1; resultSet.next(); i++) {
                if (i == id) {
                    joke = resultSet.getString(TEXT_COLUMN_INDEX).replaceAll("\\\\n", "\n");
                    break;
                }
            }

        } catch (SQLException ex) {
            Logger logger = Logger.getLogger(JokesDatabase.class.getName());
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return joke;
    }
}