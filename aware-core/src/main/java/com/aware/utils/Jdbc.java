package com.aware.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This class will encapsulate the processes between the client and a MySQL database via JDBC API.
 */
public class Jdbc {
    private final static String TAG = "JDBC";
    private static Connection connection;
    private static int transaction = 0;

    private static class JdbcConnectionException extends Exception {
        private JdbcConnectionException(String message) {
            super(message);
        }
    }

    /**
     * Inserts data into a remote database table.
     *
     * @param table name of table to insert data into
     * @param rows list of the rows of data to insert
     * @return true if the data is inserted successfully, false otherwise
     */
    public static boolean insertData(String table, JSONArray rows) {
        if (rows.length() == 0) return true;

        Jdbc.transaction ++;
        try {
            List<String> fields = new ArrayList<>();
            Iterator<String> fieldIterator = rows.getJSONObject(0).keys();
            while (fieldIterator.hasNext()) {
                fields.add(fieldIterator.next());
            }
            Jdbc.insertBatch(table, fields, rows);
        } catch (JSONException | SQLException | JdbcConnectionException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Establish a connection to the database of the currently joined study.
     */
    private static void connect() throws JdbcConnectionException {
        String connectionUrl = "jdbc:mysql://10.4.137.127:3306/aware1?user=aware&password=password";  // TODO RIO: form connection URL
//        String connectionUrl = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s",
//                Aware_Preferences.DB_HOST, Aware_Preferences.DB_PORT, Aware_Preferences.DB_NAME,
//                Aware_Preferences.DB_USERNAME, Aware_Preferences.DB_PASSWORD);
        Log.i(TAG, "Establishing connection to remote database...");

        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(connectionUrl);
            Log.i(TAG, "Connected to remote database...");
        } catch (Exception e) {
            Log.e(TAG, "Failed to establish connection to database, reason: " + e.getMessage());
            e.printStackTrace();
            throw new JdbcConnectionException(e.getMessage());
        }
    }

    /**
     * Closes the current database connection.
     */
    private static void disconnect() {
        try {
            Log.i(TAG, "Closing connection to remote database...");
            if (connection != null && !connection.isClosed()) Jdbc.connection.close();
            Log.i(TAG, "Closed connection to remote database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Batch inserts data into a remote database table.
     *
     * @param table name of table to batch insert data into
     * @param fields list of the table fields
     * @param rows list of the rows of data to insert
     * @throws JdbcConnectionException
     * @throws JSONException
     */
    private static synchronized void insertBatch(String table, List<String> fields, JSONArray rows)
            throws JdbcConnectionException, JSONException, SQLException {
        try {
            if (Jdbc.connection == null || Jdbc.connection.isClosed()) {
                connect();
            }
            Log.i(TAG, "Inserting " + rows.length() + " row(s) of data into remote table '" +
                    table + "'...");

            List<Character> sqlParamPlaceholder = new ArrayList<>();
            for (int i = 0; i < fields.size(); i ++) sqlParamPlaceholder.add('?');

            String sqlStatement = String.format("INSERT INTO %s (%s) VALUES (%s)", table,
                    TextUtils.join(",", fields), TextUtils.join(",", sqlParamPlaceholder));
            PreparedStatement ps = Jdbc.connection.prepareStatement(sqlStatement);

            for (int i = 0; i < rows.length(); i++) {
                JSONObject row = rows.getJSONObject(i);
                int paramIndex = 1;

                for (String field: fields) {
                    ps.setString(paramIndex, row.getString(field));
                    paramIndex ++;
                }
                ps.addBatch();
            }

            ps.executeBatch();
            Log.i(TAG, "Inserted " + rows.length() + " row(s) of data into remote table '" + table);
        } finally {
            Jdbc.transaction --;
            if (Jdbc.transaction == 0) disconnect();
        }
    }
}
