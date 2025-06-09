
package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.model.domain.Prenotazione;
import it.uniroma2.dicii.claupiscu.model.domain.Posto;
import it.uniroma2.dicii.claupiscu.model.domain.Proiezione;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDao {

    public static class RisultatoPrenotazione {
        public final int codiceRisultato;
        public final String codicePrenotazione;
        public final String messaggio;

        public RisultatoPrenotazione(int codice, String prenotazione, String msg) {
            this.codiceRisultato = codice;
            this.codicePrenotazione = prenotazione;
            this.messaggio = msg;
        }
    }

    /**
     * Crea una prenotazione temporanea utilizzando la stored procedure
     */
    public RisultatoPrenotazione creaPrenotazioneTemporanea(short idProiezione, char fila, byte numPosto)
            throws SQLException {
        String sql = "CALL CreaPrenotazioneTemporanea(?, ?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            // Parametri di input
            stmt.setShort(1, idProiezione);
            stmt.setString(2, String.valueOf(fila));
            stmt.setByte(3, numPosto);

            // Parametri di output
            stmt.registerOutParameter(4, Types.VARCHAR); // codice_prenotazione
            stmt.registerOutParameter(5, Types.INTEGER); // risultato

            stmt.execute();

            String codicePrenotazione = stmt.getString(4);
            int risultato = stmt.getInt(5);

            String messaggio = switch (risultato) {
                case 1 -> "Prenotazione temporanea creata con successo";
                case 0 -> "Posto già occupato o in prenotazione";
                case -1 -> "Proiezione non valida o posto inesistente";
                case -2 -> "Errore durante la prenotazione";
                default -> "Errore sconosciuto";
            };

            return new RisultatoPrenotazione(risultato, codicePrenotazione, messaggio);
        }
    }

    /**
     * Conferma una prenotazione temporanea
     */
    public RisultatoPrenotazione confermaPrenotazione(String codicePrenotazione, String ticketPagamento)
            throws SQLException {
        String sql = "CALL ConfermaPrenotazione(?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, codicePrenotazione);
            stmt.setString(2, ticketPagamento);
            stmt.registerOutParameter(3, Types.INTEGER);

            stmt.execute();

            int risultato = stmt.getInt(3);

            String messaggio = switch (risultato) {
                case 1 -> "Prenotazione confermata con successo";
                case 0 -> "Prenotazione non trovata";
                case -1 -> "Prenotazione scaduta o già confermata";
                case -2 -> "Errore durante la conferma";
                default -> "Errore sconosciuto";
            };

            return new RisultatoPrenotazione(risultato, codicePrenotazione, messaggio);
        }
    }

    /**
     * Annulla una prenotazione
     */
    public RisultatoPrenotazione annullaPrenotazione(String codicePrenotazione) throws SQLException {
        String sql = "CALL AnnullaPrenotazione(?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             CallableStatement stmt = conn.prepareCall(sql)) {

            stmt.setString(1, codicePrenotazione);
            stmt.registerOutParameter(2, Types.INTEGER);

            stmt.execute();

            int risultato = stmt.getInt(2);

            String messaggio = switch (risultato) {
                case 1 -> "Prenotazione annullata con successo";
                case 0 -> "Prenotazione non trovata";
                case -1 -> "Troppo tardi per annullare (meno di 30 minuti alla proiezione)";
                case -2 -> "Errore durante l'annullamento";
                default -> "Errore sconosciuto";
            };

            return new RisultatoPrenotazione(risultato, codicePrenotazione, messaggio);
        }
    }

    /**
     * Recupera una prenotazione per codice
     */
    public Prenotazione trovaPerCodice(String codicePrenotazione) throws SQLException {
        String sql = """
            SELECT p.codice_prenotazione, p.num_sala, p.fila, p.num_posto, p.id_proiezione,
                   p.data_ora_prenotazione, p.data_ora_conferma, p.stato_prenotazione,
                   p.timestamp_creazione, p.ticket_pag, p.timestamp_conferma, p.timestamp_scadenza
            FROM prenotazione p
            WHERE p.codice_prenotazione = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, codicePrenotazione);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToPrenotazione(rs);
            }
            return null;
        }
    }

    /**
     * Ottiene tutte le prenotazioni per una proiezione
     */
    public List<Prenotazione> trovaPerProiezione(short idProiezione) throws SQLException {
        String sql = """
            SELECT p.codice_prenotazione, p.num_sala, p.fila, p.num_posto, p.id_proiezione,
                   p.data_ora_prenotazione, p.data_ora_conferma, p.stato_prenotazione,
                   p.timestamp_creazione, p.ticket_pag, p.timestamp_conferma, p.timestamp_scadenza
            FROM prenotazione p
            WHERE p.id_proiezione = ?
            ORDER BY p.fila, p.num_posto
            """;

        List<Prenotazione> prenotazioni = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setShort(1, idProiezione);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                prenotazioni.add(mapResultSetToPrenotazione(rs));
            }
        }

        return prenotazioni;
    }

    /**
     * Mappa un ResultSet a un oggetto Prenotazione
     */
    private Prenotazione mapResultSetToPrenotazione(ResultSet rs) throws SQLException {
        Prenotazione prenotazione = new Prenotazione();

        prenotazione.setCodicePrenotazione(rs.getString("codice_prenotazione"));
        prenotazione.setNumSala(rs.getByte("num_sala"));
        prenotazione.setFila(rs.getString("fila").charAt(0));
        prenotazione.setNumPosto(rs.getByte("num_posto"));
        prenotazione.setIdProiezione(rs.getShort("id_proiezione"));

        Timestamp dataOraPrenotazione = rs.getTimestamp("data_ora_prenotazione");
        if (dataOraPrenotazione != null) {
            prenotazione.setDataOraPrenotazione(dataOraPrenotazione.toLocalDateTime());
        }

        Timestamp dataOraConferma = rs.getTimestamp("data_ora_conferma");
        if (dataOraConferma != null) {
            prenotazione.setDataOraConferma(dataOraConferma.toLocalDateTime());
        }

        String statoStr = rs.getString("stato_prenotazione");
        prenotazione.setStatoPrenotazione(Prenotazione.StatoPrenotazione.valueOf(statoStr));

        Timestamp timestampCreazione = rs.getTimestamp("timestamp_creazione");
        if (timestampCreazione != null) {
            prenotazione.setTimestampCreazione(timestampCreazione.toLocalDateTime());
        }

        prenotazione.setTicketPag(rs.getString("ticket_pag"));

        Timestamp timestampConferma = rs.getTimestamp("timestamp_conferma");
        if (timestampConferma != null) {
            prenotazione.setTimestampConferma(timestampConferma.toLocalDateTime());
        }

        Timestamp timestampScadenza = rs.getTimestamp("timestamp_scadenza");
        if (timestampScadenza != null) {
            prenotazione.setTimestampScadenza(timestampScadenza.toLocalDateTime());
        }

        return prenotazione;
    }
}