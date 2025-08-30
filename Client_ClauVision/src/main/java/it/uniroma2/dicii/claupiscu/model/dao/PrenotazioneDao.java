
package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.model.domain.Prenotazione;


import java.math.BigDecimal;
import java.sql.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneDao {

    static final String messErroreDefault = " errore sconosciuto : ("; // errore sonarclou d:(
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
                default -> messErroreDefault;
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
                default -> messErroreDefault;
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
                case -3 -> "Stato non valido per l'annullamento";
                default -> messErroreDefault;
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
                    p.stato_prenotazione,
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
    public List<Prenotazione>

    trovaPerProiezione(short idProiezione) throws SQLException {
        String sql = """
            SELECT p.codice_prenotazione, p.num_sala, p.fila, p.num_posto, p.id_proiezione, p.stato_prenotazione,
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
    // Nel file PrenotazioneDao.java, aggiungi questi metodi:


    /**
     * Calcola incassi per periodo
     */
    public BigDecimal calcolaIncassiPeriodo(LocalDateTime inizio, LocalDateTime fine) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(pr.prezzo), 0) as incasso_totale
            FROM prenotazione p
            JOIN proiezione pr ON p.id_proiezione = pr.id_proiezione
            WHERE p.stato_prenotazione = 'CONFERMATA'
            AND p.timestamp_conferma BETWEEN ? AND ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inizio));
            stmt.setTimestamp(2, Timestamp.valueOf(fine));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("incasso_totale");
                }
            }
        }
        return BigDecimal.ZERO;
    }

    /**
     * Ottiene statistiche prenotazioni per periodo
     */
    public StatistichePrenotazioni getStatistichePrenotazioni(LocalDateTime inizio, LocalDateTime fine) throws SQLException {
        String sql = """
            SELECT 
                COUNT(*) as totale_prenotazioni,
                COUNT(CASE WHEN stato_prenotazione = 'CONFERMATA' THEN 1 END) as prenotazioni_confermate,
                COUNT(CASE WHEN stato_prenotazione = 'ANNULLATA' THEN 1 END) as prenotazioni_annullate,
                COUNT(CASE WHEN stato_prenotazione = 'SCADUTA' THEN 1 END) as prenotazioni_scadute
            FROM prenotazione
            WHERE timestamp_creazione BETWEEN ? AND ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inizio));
            stmt.setTimestamp(2, Timestamp.valueOf(fine));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new StatistichePrenotazioni(
                            rs.getInt("totale_prenotazioni"),
                            rs.getInt("prenotazioni_confermate"),
                            rs.getInt("prenotazioni_annullate"),
                            rs.getInt("prenotazioni_scadute")
                    );
                }
            }
        }
        return new StatistichePrenotazioni(0, 0, 0, 0);
    }

    /**
     * Film più prenotati in un periodo
     */
    public List<FilmPopolare> getFilmPiuPrenotati(LocalDateTime inizio, LocalDateTime fine) throws SQLException {
        String sql = """
            SELECT pr.titolo_film, COUNT(*) as num_prenotazioni,
                   SUM(pr.prezzo) as incasso_totale
            FROM prenotazione p
            JOIN proiezione pr ON p.id_proiezione = pr.id_proiezione
            WHERE p.stato_prenotazione = 'CONFERMATA'
            AND p.timestamp_conferma BETWEEN ? AND ?
            GROUP BY pr.titolo_film
            ORDER BY num_prenotazioni DESC, incasso_totale DESC
            LIMIT 10
            """;

        List<FilmPopolare> filmPopolari = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inizio));
            stmt.setTimestamp(2, Timestamp.valueOf(fine));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    filmPopolari.add(new FilmPopolare(
                            rs.getString("titolo_film"),
                            rs.getInt("num_prenotazioni"),
                            rs.getBigDecimal("incasso_totale")
                    ));
                }
            }
        }

        return filmPopolari;
    }

    /**
     * Statistiche per sale
     */
    public List<StatisticheSala> getStatisticheSale(LocalDateTime inizio, LocalDateTime fine) throws SQLException {
        String sql = """
            SELECT s.num_sala, s.nome_sala, s.capacita,
                   COUNT(p.codice_prenotazione) as prenotazioni_totali,
                   COUNT(CASE WHEN p.stato_prenotazione = 'CONFERMATA' THEN 1 END) as prenotazioni_confermate,
                   COALESCE(SUM(pr.prezzo), 0) as incasso_totale
            FROM sala s
            LEFT JOIN proiezione pr ON s.num_sala = pr.num_sala
            LEFT JOIN prenotazione p ON pr.id_proiezione = p.id_proiezione 
                AND p.timestamp_creazione BETWEEN ? AND ?
            GROUP BY s.num_sala, s.nome_sala, s.capacita
            ORDER BY prenotazioni_confermate DESC
            """;

        List<StatisticheSala> statisticheSale = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, Timestamp.valueOf(inizio));
            stmt.setTimestamp(2, Timestamp.valueOf(fine));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    statisticheSale.add(new StatisticheSala(
                            rs.getByte("num_sala"),
                            rs.getString("nome_sala"),
                            rs.getByte("capacita"),
                            rs.getInt("prenotazioni_totali"),
                            rs.getInt("prenotazioni_confermate"),
                            rs.getBigDecimal("incasso_totale")
                    ));
                }
            }
        }

        return statisticheSale;
    }

    /**
     * Statistiche generali del cinema
     */
    public StatisticheGenerali getStatisticheGenerali() throws SQLException {
        String sql = """
            SELECT 
                (SELECT COUNT(*) FROM film) as totale_film,
                (SELECT COUNT(*) FROM sala) as totale_sale,
                (SELECT COUNT(*) FROM proiezione WHERE data_ora_inizio > NOW()) as proiezioni_future,
                (SELECT COUNT(*) FROM prenotazione WHERE stato_prenotazione = 'CONFERMATA') as prenotazioni_totali,
                (SELECT COALESCE(SUM(pr.prezzo), 0) FROM prenotazione p 
                 JOIN proiezione pr ON p.id_proiezione = pr.id_proiezione 
                 WHERE p.stato_prenotazione = 'CONFERMATA') as incasso_totale
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return new StatisticheGenerali(
                        rs.getInt("totale_film"),
                        rs.getInt("totale_sale"),
                        rs.getInt("proiezioni_future"),
                        rs.getInt("prenotazioni_totali"),
                        rs.getBigDecimal("incasso_totale")
                );
            }
        }

        return new StatisticheGenerali(0, 0, 0, 0, BigDecimal.ZERO);
    }

    // Classi per le statistiche
    public static class StatistichePrenotazioni {
        public final int totale;
        public final int confermate;
        public final int annullate;
        public final int scadute;

        public StatistichePrenotazioni(int totale, int confermate, int annullate, int scadute) {
            this.totale = totale;
            this.confermate = confermate;
            this.annullate = annullate;
            this.scadute = scadute;
        }
    }

    public static class FilmPopolare {
        public final String titolo;
        public final int prenotazioni;
        public final BigDecimal incasso;

        public FilmPopolare(String titolo, int prenotazioni, BigDecimal incasso) {
            this.titolo = titolo;
            this.prenotazioni = prenotazioni;
            this.incasso = incasso;
        }
    }

    public static class StatisticheSala {
        public final byte numSala;
        public final String nomeSala;
        public final byte capacita;
        public final int prenotazioniTotali;
        public final int prenotazioniConfermate;
        public final BigDecimal incasso;

        public StatisticheSala(byte numSala, String nomeSala, byte capacita,
                               int prenotazioniTotali, int prenotazioniConfermate, BigDecimal incasso) {
            this.numSala = numSala;
            this.nomeSala = nomeSala;
            this.capacita = capacita;
            this.prenotazioniTotali = prenotazioniTotali;
            this.prenotazioniConfermate = prenotazioniConfermate;
            this.incasso = incasso;
        }
    }

    public static class StatisticheGenerali {
        public final int totaleFilm;
        public final int totaleSale;
        public final int proiezioniFuture;
        public final int prenotazioniTotali;
        public final BigDecimal incassoTotale;

        public StatisticheGenerali(int totaleFilm, int totaleSale, int proiezioniFuture,
                                   int prenotazioniTotali, BigDecimal incassoTotale) {
            this.totaleFilm = totaleFilm;
            this.totaleSale = totaleSale;
            this.proiezioniFuture = proiezioniFuture;
            this.prenotazioniTotali = prenotazioniTotali;
            this.incassoTotale = incassoTotale;
        }
    }
}