package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.model.domain.Proiezione;
import it.uniroma2.dicii.claupiscu.model.domain.Proiezione.StatoProiezione;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProiezioneDao {

    /**
     * Inserisce una nuova proiezione nel database
     */
    public void inserisci(Proiezione proiezione) throws SQLException {
        String sql = """
            INSERT INTO proiezione (titolo_film, num_sala, prezzo, data_ora_inizio, 
                                    data_ora_fine, stato_proiezione)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, proiezione.getTitoloFilm());
            stmt.setByte(2, proiezione.getNumSala());
            stmt.setBigDecimal(3, proiezione.getPrezzo());
            stmt.setTimestamp(4, Timestamp.valueOf(proiezione.getDataOraInizio()));
            stmt.setTimestamp(5, Timestamp.valueOf(proiezione.getDataOraFine()));
            stmt.setString(6, proiezione.getStatoProiezione().name());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento proiezione fallito, nessuna riga interessata.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    proiezione.setIdProiezione(generatedKeys.getShort(1));
                } else {
                    throw new SQLException("Inserimento proiezione fallito, ID non ottenuto.");
                }
            }
        }
    }

    /**
     * Aggiorna una proiezione esistente
     */
    public boolean aggiorna(Proiezione proiezione) throws SQLException {
        String sql = """
            UPDATE proiezione 
            SET titolo_film = ?, num_sala = ?, prezzo = ?, 
                data_ora_inizio = ?, data_ora_fine = ?, stato_proiezione = ?
            WHERE id_proiezione = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, proiezione.getTitoloFilm());
            stmt.setByte(2, proiezione.getNumSala());
            stmt.setBigDecimal(3, proiezione.getPrezzo());
            stmt.setTimestamp(4, Timestamp.valueOf(proiezione.getDataOraInizio()));
            stmt.setTimestamp(5, Timestamp.valueOf(proiezione.getDataOraFine()));
            stmt.setString(6, proiezione.getStatoProiezione().name());
            stmt.setShort(7, proiezione.getIdProiezione());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina una proiezione per ID
     */
    public boolean elimina(short idProiezione) throws SQLException {
        String sql = "DELETE FROM proiezione WHERE id_proiezione = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setShort(1, idProiezione);
            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Trova una proiezione per ID - versione con Optional
     */
    public Optional<Proiezione> trovaPerIdOptional(short idProiezione) throws SQLException {
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
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToProiezione(rs));
                }
                return Optional.empty();
            }
        }
    }

    /**
     * Trova una proiezione per ID - versione tradizionale
     */
    public Proiezione trovaPerIId(short idProiezione) throws SQLException {
        return trovaPerIdOptional(idProiezione).orElse(null);
    }

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

        return eseguiQueryProiezioni(sql);
    }

    /**
     * Trova proiezioni per film
     */
    public List<Proiezione> trovaPerFilm(String titoloFilm) throws SQLException {
        String sql = """
            SELECT p.id_proiezione, p.titolo_film, p.num_sala, p.prezzo,
                   p.data_ora_inizio, p.data_ora_fine, p.stato_proiezione,
                   s.nome_sala, f.durata_minuti
            FROM proiezione p
            JOIN sala s ON p.num_sala = s.num_sala
            JOIN film f ON p.titolo_film = f.titolo_film
            WHERE p.titolo_film = ?
            ORDER BY p.data_ora_inizio
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titoloFilm);
            return eseguiQueryProiezioni(stmt);
        }
    }

    /**
     * Trova proiezioni per sala in un range di date
     */
    public List<Proiezione> trovaPerSalaEPeriodo(byte numSala, LocalDateTime dataInizio,
                                                 LocalDateTime dataFine) throws SQLException {
        String sql = """
            SELECT p.id_proiezione, p.titolo_film, p.num_sala, p.prezzo,
                   p.data_ora_inizio, p.data_ora_fine, p.stato_proiezione,
                   s.nome_sala, f.durata_minuti
            FROM proiezione p
            JOIN sala s ON p.num_sala = s.num_sala
            JOIN film f ON p.titolo_film = f.titolo_film
            WHERE p.num_sala = ? 
              AND p.data_ora_inizio >= ? 
              AND p.data_ora_inizio <= ?
            ORDER BY p.data_ora_inizio
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            stmt.setTimestamp(2, Timestamp.valueOf(dataInizio));
            stmt.setTimestamp(3, Timestamp.valueOf(dataFine));
            return eseguiQueryProiezioni(stmt);
        }
    }

    /**
     * Aggiorna stati delle proiezioni basandosi sull'orario corrente
     */
    public int aggiornaStatiProiezioni() throws SQLException {
        String sql = """
            UPDATE proiezione 
            SET stato_proiezione = CASE 
                WHEN NOW() > data_ora_fine THEN 'TERMINATA'
                WHEN NOW() BETWEEN data_ora_inizio AND data_ora_fine THEN 'IN_CORSO'
                ELSE stato_proiezione
            END
            WHERE stato_proiezione != 'TERMINATA'
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate();
        }
    }

    /**
     * Verifica se esiste sovrapposizione di orari per una sala
     */
    public boolean verificaSovrapposizione(byte numSala, LocalDateTime dataInizio,
                                           LocalDateTime dataFine, Short idProiezioneEsclusa) throws SQLException {
        String sql = """
            SELECT COUNT(*) 
            FROM proiezione 
            WHERE num_sala = ? 
              AND ((data_ora_inizio <= ? AND data_ora_fine > ?) OR 
                   (data_ora_inizio < ? AND data_ora_fine >= ?))
              AND stato_proiezione != 'TERMINATA'
            """ + (idProiezioneEsclusa != null ? " AND id_proiezione != ?" : "");

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, numSala);
            stmt.setTimestamp(2, Timestamp.valueOf(dataInizio));
            stmt.setTimestamp(3, Timestamp.valueOf(dataInizio));
            stmt.setTimestamp(4, Timestamp.valueOf(dataFine));
            stmt.setTimestamp(5, Timestamp.valueOf(dataFine));

            if (idProiezioneEsclusa != null) {
                stmt.setShort(6, idProiezioneEsclusa);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
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
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    PostoDisponibile posto = new PostoDisponibile();
                    posto.numSala = rs.getByte("num_sala");
                    posto.fila = rs.getString("fila").charAt(0);
                    posto.numPosto = rs.getByte("num_posto");
                    posto.disponibile = "DISPONIBILE".equals(rs.getString("stato_posto"));
                    posti.add(posto);
                }
            }
        }

        return posti;
    }

    // Metodi utility privati

    /**
     * Esegue una query per ottenere lista di proiezioni
     */
    private List<Proiezione> eseguiQueryProiezioni(String sql) throws SQLException {
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return eseguiQueryProiezioni(stmt);
        }
    }

    /**
     * Esegue una query preparata per ottenere lista di proiezioni
     */
    private List<Proiezione> eseguiQueryProiezioni(PreparedStatement stmt) throws SQLException {
        List<Proiezione> proiezioni = new ArrayList<>();

        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                proiezioni.add(mapResultSetToProiezione(rs));
            }
        }

        return proiezioni;
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

        // Gestione sicura delle date
        Timestamp dataInizio = rs.getTimestamp("data_ora_inizio");
        if (dataInizio != null) {
            proiezione.setDataOraInizio(dataInizio.toLocalDateTime());
        }

        Timestamp dataFine = rs.getTimestamp("data_ora_fine");
        if (dataFine != null) {
            proiezione.setDataOraFine(dataFine.toLocalDateTime());
        }

        // Gestione sicura dell'enum stato
        String statoStr = rs.getString("stato_proiezione");
        if (statoStr != null) {
            try {
                proiezione.setStatoProiezione(StatoProiezione.valueOf(statoStr));
            } catch (IllegalArgumentException e) {
                // Fallback su valore di default se stato non riconosciuto
                proiezione.setStatoProiezione(StatoProiezione.PROGRAMMATA);
            }
        }

        // Informazioni aggiuntive dalla join (potrebbero essere null)
        String nomeSala = rs.getString("nome_sala");
        if (nomeSala != null) {
            proiezione.setNomeSala(nomeSala);
        }

        // Gestione sicura della durata
        byte durata = rs.getByte("durata_minuti");
        if (!rs.wasNull()) {
            proiezione.setDurataMinuti(durata);
        }

        return proiezione;
    }

    /**
     * Classe inner per rappresentare lo stato di un posto
     */
    public static class PostoDisponibile {
        public byte numSala;
        public char fila;
        public byte numPosto;
        public boolean disponibile;

        public String getCodice() {
            return String.format("%c%02d", fila, Byte.toUnsignedInt(numPosto));
        }

        public int getNumSalaInt() {
            return Byte.toUnsignedInt(numSala);
        }

        public int getNumPostoInt() {
            return Byte.toUnsignedInt(numPosto);
        }

        @Override
        public String toString() {
            return String.format("Posto{sala=%d, %s, %s}",
                    getNumSalaInt(), getCodice(), disponibile ? "LIBERO" : "OCCUPATO");
        }
    }
}