package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.model.domain.Posto;
import it.uniroma2.dicii.claupiscu.model.domain.Sala;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SalaDao {

    /**
     * Ottiene tutte le sale disponibili
     */
    public List<Sala> trovaTutteSale() throws SQLException {
        String sql = """
            SELECT num_sala, nome_sala, capacita
            FROM sala
            ORDER BY num_sala
            """;

        List<Sala> sale = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sale.add(mapResultSetToSala(rs));
            }
        }

        return sale;
    }

    /**
     * Trova una sala per numero
     */
    public Sala trovaPerNumero(byte numSala) throws SQLException {
        String sql = """
            SELECT num_sala, nome_sala, capacita
            FROM sala
            WHERE num_sala = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToSala(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserisce una nuova sala
     */
    public void inserisci(Sala sala) throws SQLException {
        String sql = """
            INSERT INTO sala (num_sala, nome_sala, capacita)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, sala.getNumSala());
            stmt.setString(2, sala.getNomeSala());
            stmt.setByte(3, sala.getCapacita());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento sala fallito, nessuna riga interessata.");
            }
        }
    }

    /**
     * Aggiorna una sala esistente
     */
    public boolean aggiorna(Sala sala) throws SQLException {
        String sql = """
            UPDATE sala 
            SET nome_sala = ?, capacita = ?
            WHERE num_sala = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, sala.getNomeSala());
            stmt.setByte(2, sala.getCapacita());
            stmt.setByte(3, sala.getNumSala());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una sala
     */
    public boolean elimina(byte numSala) throws SQLException {
        String sql = "DELETE FROM sala WHERE num_sala = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Trova tutti i posti di una sala
     */
    public List<Posto> trovaPostiSala(byte numSala) throws SQLException {
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
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posti.add(mapResultSetToPosto(rs));
                }
            }
        }

        return posti;
    }

    /**
     * Inserisce un posto
     */
    public void inserisciPosto(Posto posto) throws SQLException {
        String sql = """
            INSERT INTO posto (num_sala, fila, num_posto)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, posto.getNumSala());
            stmt.setString(2, String.valueOf(posto.getFila()));
            stmt.setByte(3, posto.getNumPosto());

            stmt.executeUpdate();
        }
    }

    /**
     * Elimina un posto
     */
    public boolean eliminaPosto(Posto posto) throws SQLException {
        String sql = "DELETE FROM posto WHERE num_sala = ? AND fila = ? AND num_posto = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, posto.getNumSala());
            stmt.setString(2, String.valueOf(posto.getFila()));
            stmt.setByte(3, posto.getNumPosto());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina tutti i posti di una sala
     */
    public int eliminaTuttiPostiSala(byte numSala) throws SQLException {
        String sql = "DELETE FROM posto WHERE num_sala = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            return stmt.executeUpdate();
        }
    }

    /**
     * Genera automaticamente i posti per una sala
     */
    public void generaPostiSala(byte numSala, byte capacita) throws SQLException {
        // Logica per generare posti: file da A in poi, massimo 20 posti per fila
        int postiPerFila = 20;
        int numFile = (capacita + postiPerFila - 1) / postiPerFila; // Divisione con arrotondamento per eccesso

        char filaCorrente = 'A';
        int postoCorrente = 1;
        int postiInseriti = 0;

        String sql = """
            INSERT INTO posto (num_sala, fila, num_posto)
            VALUES (?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int fila = 0; fila < numFile && postiInseriti < capacita; fila++) {
                int postiInQuestaFila = Math.min(postiPerFila, capacita - postiInseriti);

                for (int posto = 1; posto <= postiInQuestaFila; posto++) {
                    stmt.setByte(1, numSala);
                    stmt.setString(2, String.valueOf(filaCorrente));
                    stmt.setByte(3, (byte) posto);
                    stmt.addBatch();

                    postiInseriti++;
                }

                filaCorrente++;
            }

            stmt.executeBatch();
        }
    }

    // Metodi di mapping
    private Sala mapResultSetToSala(ResultSet rs) throws SQLException {
        Sala sala = new Sala();
        sala.setNumSala(rs.getByte("num_sala"));
        sala.setNomeSala(rs.getString("nome_sala"));
        sala.setCapacita(rs.getByte("capacita"));
        return sala;
    }

    private Posto mapResultSetToPosto(ResultSet rs) throws SQLException {
        Posto posto = new Posto();
        posto.setNumSala(rs.getByte("num_sala"));
        posto.setFila(rs.getString("fila").charAt(0));
        posto.setNumPosto(rs.getByte("num_posto"));
        return posto;
    }
}