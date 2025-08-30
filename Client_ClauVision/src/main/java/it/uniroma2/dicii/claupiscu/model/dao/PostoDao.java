package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.model.domain.Posto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostoDao {

    /**
     * Ottiene tutti i posti di una sala
     */
    public List<Posto> trovaPostiPerSala(byte numSala) throws SQLException {
        String sql = """
            SELECT num_sala, fila, num_posto
            FROM posto
            WHERE num_sala = ?
            ORDER BY fila, num_posto
            """;

        List<Posto> posti = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Posto posto = new Posto();
                posto.setNumSala(rs.getByte("num_sala"));
                posto.setFila(rs.getString("fila").charAt(0));
                posto.setNumPosto(rs.getByte("num_posto"));
                posti.add(posto);
            }
        }

        return posti;
    }

    /**
     * Verifica se un posto esiste
     */
    public boolean esistePosto(byte numSala, char fila, byte numPosto) throws SQLException {
        String sql = """
            SELECT COUNT(*) as count
            FROM posto
            WHERE num_sala = ? AND fila = ? AND num_posto = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            stmt.setString(2, String.valueOf(fila));
            stmt.setByte(3, numPosto);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0;
            }
        }

        return false;
    }

    /**
     * Trova un posto specifico
     */
    public Posto trovaPosto(byte numSala, char fila, byte numPosto) throws SQLException {
        String sql = """
            SELECT num_sala, fila, num_posto
            FROM posto
            WHERE num_sala = ? AND fila = ? AND num_posto = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            stmt.setString(2, String.valueOf(fila));
            stmt.setByte(3, numPosto);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Posto posto = new Posto();
                posto.setNumSala(rs.getByte("num_sala"));
                posto.setFila(rs.getString("fila").charAt(0));
                posto.setNumPosto(rs.getByte("num_posto"));
                return posto;
            }
        }

        return null;
    }
}