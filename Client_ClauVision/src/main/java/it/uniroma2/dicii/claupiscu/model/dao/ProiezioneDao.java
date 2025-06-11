package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.model.domain.Proiezione;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProiezioneDao {

    /**
     * Ottiene tutte le proiezioni future disponibili
     */
    public List<Proiezione> trovaProiezioniFuture() throws SQLException {
        String sql = """
            SELECT p.id_proiezione, p.titolo_film, p.num_sala, p.prezzo,
                   p.data_ora_inizio, p.data_ora_fine, p.stato_proiezione,
                   s.nome_sala, f.durata_minuti
            FROM proiezione p
            JOIN sala s ON p.num_sala = s.num_sala
            JOIN film f ON p.titolo_film = f.titolo_film
            WHERE p.data_ora_inizio > NOW()
              AND p.stato_proiezione = 'PROGRAMMATA'
            ORDER BY p.data_ora_inizio
            """;

        List<Proiezione> proiezioni = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                proiezioni.add(mapResultSetToProiezione(rs));
            }
        }

        return proiezioni;
    }

    /**
     * Trova una proiezione per ID
     */
    public Proiezione trovaPerIId(short idProiezione) throws SQLException {
        String sql = """
            SELECT p.id_proiezione, p.titolo_film, p.num_sala, p.prezzo,
                   p.data_ora_inizio, p.data_ora_fine, p.stato_proiezione,
                   s.nome_sala, f.durata_minuti
            FROM proiezione p
            JOIN sala s ON p.num_sala = s.num_sala
            JOIN film f ON p.titolo_film = f.titolo_film
            WHERE p.id_proiezione = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setShort(1, idProiezione);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToProiezione(rs);
            }
            return null;
        }
    }

    /**
     * Ottiene i posti disponibili per una proiezione usando la vista
     */
    public List<PostoDisponibile> getPostiDisponibili(short idProiezione) throws SQLException {
        String sql = """
            SELECT num_sala, fila, num_posto, stato_posto
            FROM vista_posti_disponibili
            WHERE id_proiezione = ?
            ORDER BY fila, num_posto
            """;

        List<PostoDisponibile> posti = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setShort(1, idProiezione);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                PostoDisponibile posto = new PostoDisponibile();
                posto.numSala = rs.getByte("num_sala");
                posto.fila = rs.getString("fila").charAt(0);
                posto.numPosto = rs.getByte("num_posto");
                posto.disponibile = "DISPONIBILE".equals(rs.getString("stato_posto"));
                posti.add(posto);
            }
        }

        return posti;
    }

    /**
     * Classe per rappresentare lo stato di un posto
     */
    public static class PostoDisponibile {
        public byte numSala;
        public char fila;
        public byte numPosto;
        public boolean disponibile;

        public String getCodice() {
            return String.format("%c%02d", fila, Byte.toUnsignedInt(numPosto));
        }
    }

    /**
     * Mappa un ResultSet a un oggetto Proiezione
     */
    private Proiezione mapResultSetToProiezione(ResultSet rs) throws SQLException {
        Proiezione proiezione = new Proiezione();

        proiezione.setIdProiezione(rs.getShort("id_proiezione"));
        proiezione.setTitoloFilm(rs.getString("titolo_film"));
        proiezione.setNumSala(rs.getByte("num_sala"));
        proiezione.setPrezzo(rs.getBigDecimal("prezzo"));

        Timestamp dataInizio = rs.getTimestamp("data_ora_inizio");
        if (dataInizio != null) {
            proiezione.setDataOraInizio(dataInizio.toLocalDateTime());
        }

        Timestamp dataFine = rs.getTimestamp("data_ora_fine");
        if (dataFine != null) {
            proiezione.setDataOraFine(dataFine.toLocalDateTime());
        }

        String statoStr = rs.getString("stato_proiezione");
        proiezione.setStatoProiezione(Proiezione.StatoProiezione.valueOf(statoStr));

        // Informazioni aggiuntive dalla join
        proiezione.setNomeSala(rs.getString("nome_sala"));
        proiezione.setNomeSala(rs.getString("nome_sala"));
        proiezione.setDurataMinuti(rs.getByte("durata_minuti"));

        return proiezione;
    }
}