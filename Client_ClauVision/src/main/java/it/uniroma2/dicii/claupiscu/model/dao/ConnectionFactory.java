package it.uniroma2.dicii.claupiscu.model.dao;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class ConnectionFactory {
    private static Connection connection;

    private ConnectionFactory() {}

    static {
        try (InputStream input = new FileInputStream("resources/db.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            String connectionUrl = properties.getProperty("CONNECTION_URL");
            String user = properties.getProperty("LOGIN_USER");
            String pass = properties.getProperty("LOGIN_PASS");

            connection = DriverManager.getConnection(connectionUrl, user, pass);
        } catch (IOException | SQLException e) {
            System.err.println("Errore durante l'inizializzazione della connessione al database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Restituisce la connessione al database
     * @return Connection al database
     * @throws SQLException se la connessione non è disponibile
     */
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            reconnect();
        }
        return connection;
    }

    /**
     * Riconnette al database in caso di perdita di connessione
     * @throws SQLException se non riesce a riconnettersi
     */
    private static void reconnect() throws SQLException {
        try (InputStream input = new FileInputStream("resources/db.properties")) {
            Properties properties = new Properties();
            properties.load(input);

            String connectionUrl = properties.getProperty("CONNECTION_URL");
            String user = properties.getProperty("LOGIN_USER");
            String pass = properties.getProperty("LOGIN_PASS");

            connection = DriverManager.getConnection(connectionUrl, user, pass);
        } catch (IOException e) {
            throw new SQLException("Impossibile caricare le proprietà del database", e);
        }
    }

    /**
     * Chiude la connessione al database
     */
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }

    /**
     * Verifica se la connessione è attiva
     * @return true se la connessione è attiva, false altrimenti
     */
    public static boolean isConnectionValid() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }
}