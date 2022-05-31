package com.example.sudoku.trash;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaSeH2Server {

    public static void main(String[] args) {

        var url = "jdbc:h2:D:/Java_projects/db/database";
        var user = "sa";
        var passwd = "";

        var query = "SELECT * FROM cars";

        try (var con = DriverManager.getConnection(url, user, passwd);
             var st = con.createStatement();
             var rs = st.executeQuery(query)) {

            while (rs.next()) {

                System.out.printf("%d %s %d%n", rs.getInt(1),
                        rs.getString(2), rs.getInt(3));
            }

        } catch (SQLException ex) {

            var lgr = Logger.getLogger(JavaSeH2Server.class.getName());
            lgr.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
}