package it.uniroma2.dicii.claupiscu.controller;

import it.uniroma2.dicii.claupiscu.exception.PrenotazioneEccezioni.*;
import it.uniroma2.dicii.claupiscu.model.dao.PrenotazioneDao;
import it.uniroma2.dicii.claupiscu.model.dao.ProiezioneDao;
import it.uniroma2.dicii.claupiscu.model.domain.Prenotazione;
import it.uniroma2.dicii.claupiscu.model.domain.Proiezione;
import it.uniroma2.dicii.claupiscu.view.PrenotazioneView;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller per la gestione delle prenotazioni cinematografiche
 *
 * @author Claudio Piscitelli
 * @version 2.0
 */
public class PrenotazioneController {
    private static final Logger logger = Logger.getLogger(PrenotazioneController.class.getName());

    private final PrenotazioneDao prenotazioneDao;
    private final ProiezioneDao proiezioneDao;
    private final PrenotazioneView prenotazioneView;

    public PrenotazioneController() {
        this.prenotazioneDao = new PrenotazioneDao();
        this.proiezioneDao = new ProiezioneDao();
        this.prenotazioneView = new PrenotazioneView();
    }

    /**
     * Avvia il menu principale del sistema di prenotazioni
     */
    public void start() {
        boolean continua = true;

        prenotazioneView.mostraMessaggio("🎬 Benvenuto nel Sistema di Prenotazione Cinema!");

        while (continua) {
            try {
                int scelta = prenotazioneView.mostraMenuPrenotazione();
                continua = processaSceltaMenu(scelta);

            } catch (Exception e) {
                logger.log(Level.SEVERE, "Errore critico nell'applicazione", e);
                prenotazioneView.mostraMessaggioErrore("❌ Si è verificato un errore critico. L'applicazione verrà riavviata.");
                // In caso di errore critico, potresti decidere di continuare o fermare
            }
        }

        prenotazioneView.mostraMessaggio("👋 Grazie per aver utilizzato il nostro sistema!");
    }

    /**
     * Processa la scelta dal menu principale
     */
    private boolean processaSceltaMenu(int scelta) {
        try {
            switch (scelta) {
                case 1 -> nuovaPrenotazione();
                case 2 -> confermaPrenotazione();
                case 3 -> annullaPrenotazione();
                case 4 -> visualizzaPrenotazione();
                case 0 -> {
                    return false;
                }
                default -> throw new InputNonValidoException("Scelta menu non valida: " + scelta);
            }
        } catch (PrenotazioneException e) {
            gestisciEccezionePrenotazione(e);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Errore imprevisto durante l'operazione", e);
            prenotazioneView.mostraMessaggioErrore("⚠️ Si è verificato un errore imprevisto: " + e.getMessage());
        }

        return true;
    }

    /**
     * Gestisce la creazione di una nuova prenotazione
     */
    private void nuovaPrenotazione() throws PrenotazioneException {
        logger.info("Avvio processo nuova prenotazione");

        // 1. Verifica disponibilità proiezioni
        List<Proiezione> proiezioni = caricaProiezioniFuture();

        // 2. Selezione proiezione
        Proiezione proiezioneDaPrenotare = selezionaProiezione(proiezioni);

        // 3. Selezione posto
        String postoParsed = selezionaPosto(proiezioneDaPrenotare);

        // 4. Creazione prenotazione temporanea
        String codicePrenotazione = creaPrenotazioneTemporanea(proiezioneDaPrenotare, postoParsed);

        // 5. Opzione conferma immediata
        if (prenotazioneView.chiedereConfermaImmediata()) {
            confermaPrenotazioneEsistente(codicePrenotazione);
        }

        logger.info("Processo nuova prenotazione completato con successo");

    }

    /**
     * Carica le proiezioni future disponibili
     */
    private List<Proiezione> caricaProiezioniFuture() throws DatabaseException, ProiezioneNonDisponibileException {
        try {
            logger.info("Caricamento proiezioni future");
            List<Proiezione> proiezioni = proiezioneDao.trovaProiezioniFuture();

            if (proiezioni.isEmpty()) {
                throw new ProiezioneNonDisponibileException("Nessuna proiezione disponibile al momento");
            }

            logger.info("Caricate " + proiezioni.size() + " proiezioni");
            return proiezioni;

        } catch (SQLException e) {
            throw new DatabaseException("Impossibile caricare le proiezioni", e);
        }
    }

    /**
     * Gestisce la selezione della proiezione da parte dell'utente
     */
    private Proiezione selezionaProiezione(List<Proiezione> proiezioni) throws InputNonValidoException {
        int sceltaProiezione = prenotazioneView.mostraMenuSelezioneProiezione(proiezioni);

        if (sceltaProiezione == 0) {
            throw new InputNonValidoException("Operazione annullata dall'utente");
        }

        if (sceltaProiezione < 1 || sceltaProiezione > proiezioni.size()) {
            throw new InputNonValidoException("Selezione proiezione non valida: " + sceltaProiezione);
        }

        Proiezione proiezioneSelezionata = proiezioni.get(sceltaProiezione - 1);
        logger.info("Proiezione selezionata: " + proiezioneSelezionata.getTitoloFilm());

        return proiezioneSelezionata;
    }

    /**
     * Gestisce la selezione del posto da parte dell'utente
     */
    private String selezionaPosto(Proiezione proiezione) throws DatabaseException, InputNonValidoException {
        try {
            logger.info("Caricamento mappa posti per proiezione ID: " + proiezione.getIdProiezione());

            List<ProiezioneDao.PostoDisponibile> posti = proiezioneDao.getPostiDisponibili(proiezione.getIdProiezione());
            prenotazioneView.mostraMappaPosti(posti, proiezione);

            String postoParsed = prenotazioneView.richiediSelezionePosto();
            if (postoParsed == null) {
                throw new InputNonValidoException("Selezione posto annullata");
            }

            validaFormatoPosto(postoParsed);
            logger.info("Posto selezionato: " + postoParsed);

            return postoParsed;

        } catch (SQLException e) {
            throw new DatabaseException("Errore nel caricamento dei posti disponibili", e);
        }
    }

    /**
     * Valida il formato del posto inserito dall'utente
     */
    private void validaFormatoPosto(String posto) throws InputNonValidoException {
        if (posto.length() < 2) {
            throw new InputNonValidoException("Formato posto non valido: " + posto);
        }

        char fila = posto.charAt(0);
        if (!Character.isLetter(fila)) {
            throw new InputNonValidoException("Fila non valida: " + fila);
        }

        try {
            Byte.parseByte(posto.substring(1));
        } catch (NumberFormatException e) {
            throw new InputNonValidoException("Numero posto non valido: " + posto.substring(1));
        }
    }

    /**
     * Crea una prenotazione temporanea nel sistema
     */
    private String creaPrenotazioneTemporanea(Proiezione proiezione, String posto)
            throws DatabaseException, PostoNonDisponibileException {
        try {
            prenotazioneView.mostraMessaggio("⏳ Creazione prenotazione in corso...");

            char fila = posto.charAt(0);
            byte numPosto = Byte.parseByte(posto.substring(1));

            PrenotazioneDao.RisultatoPrenotazione risultato = prenotazioneDao.creaPrenotazioneTemporanea(
                    proiezione.getIdProiezione(), fila, numPosto
            );

            if (risultato.codiceRisultato == 1) {
                prenotazioneView.mostraSuccesso("✅ Prenotazione temporanea creata!");
                prenotazioneView.mostraDettagliPrenotazioneTemporanea(
                        risultato.codicePrenotazione, proiezione, posto);

                logger.info("Prenotazione temporanea creata con codice: " + risultato.codicePrenotazione);
                return risultato.codicePrenotazione;
            } else {
                throw new PostoNonDisponibileException(fila, numPosto);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Errore durante la creazione della prenotazione temporanea", e);
        }
    }

    /**
     * Gestisce la conferma di una prenotazione esistente
     */
    private void confermaPrenotazione() throws PrenotazioneException {
        logger.info("Avvio processo conferma prenotazione");
        String codicePrenotazione = richiediCodicePrenotazioneValido();
        confermaPrenotazioneEsistente(codicePrenotazione);
    }

    /**
     * Richiede e valida il codice prenotazione dall'utente
     */
    private String richiediCodicePrenotazioneValido() throws InputNonValidoException {
        String codicePrenotazione = prenotazioneView.richiediCodicePrenotazione();

        if (codicePrenotazione == null || codicePrenotazione.trim().isEmpty()) {
            throw new InputNonValidoException("Codice prenotazione non fornito");
        }

        return codicePrenotazione.trim();
    }

    /**
     * Conferma una prenotazione esistente
     */
    private void confermaPrenotazioneEsistente(String codicePrenotazione) throws PrenotazioneException {
        logger.info("Conferma prenotazione con codice: " + codicePrenotazione);

        Prenotazione prenotazione = trovaPrenotazioneValida(codicePrenotazione);

        if (!prenotazione.isConfermabile()) {
            throw new PrenotazioneScadutaException(codicePrenotazione);
        }

        prenotazioneView.mostraDettagliPrenotazione(prenotazione);

        if (prenotazioneView.confermarePagamento()) {
            processaPagamento(codicePrenotazione);
        }

    }

    /**
     * Processa il pagamento per la prenotazione
     */
    private void processaPagamento(String codicePrenotazione) throws DatabaseException, PagamentoException {
        try {
            prenotazioneView.mostraMessaggio("💳 Elaborazione pagamento...");

            String ticketPagamento = generaTicketPagamento();

            PrenotazioneDao.RisultatoPrenotazione risultato = prenotazioneDao.confermaPrenotazione(
                    codicePrenotazione, ticketPagamento
            );

            if (risultato.codiceRisultato == 1) {
                prenotazioneView.mostraSuccesso("🎉 Prenotazione confermata con successo!");
                prenotazioneView.mostraTicketPagamento(ticketPagamento);
                logger.info("Pagamento completato per prenotazione: " + codicePrenotazione);
            } else {
                throw new PagamentoException("Errore durante il processo di pagamento: " + risultato.messaggio);
            }

        } catch (SQLException e) {
            throw new DatabaseException("Errore di database durante il pagamento", e);
        }
    }

    /**
     * Gestisce l'annullamento di una prenotazione
     */
    private void annullaPrenotazione() throws PrenotazioneException {
        try {
            logger.info("Avvio processo annullamento prenotazione");

            String codicePrenotazione = richiediCodicePrenotazioneValido();
            Prenotazione prenotazione = trovaPrenotazioneValida(codicePrenotazione);

            prenotazioneView.mostraDettagliPrenotazione(prenotazione);

            if (prenotazioneView.confermareAnnullamento()) {
                PrenotazioneDao.RisultatoPrenotazione risultato = prenotazioneDao.annullaPrenotazione(codicePrenotazione);

                if (risultato.codiceRisultato == 1) {
                    prenotazioneView.mostraSuccesso("✅ Prenotazione annullata con successo");
                    logger.info("Prenotazione annullata: " + codicePrenotazione);
                } else {
                    throw new PrenotazioneNonAnnullabileException(risultato.messaggio);
                }
            }

        } catch (SQLException e) {
            throw new DatabaseException("Errore durante l'annullamento della prenotazione", e);
        }
    }

    /**
     * Visualizza i dettagli di una prenotazione
     */
    private void visualizzaPrenotazione() throws PrenotazioneException {
        try {
            logger.info("Avvio visualizzazione prenotazione");

            String codicePrenotazione = richiediCodicePrenotazioneValido();
            Prenotazione prenotazione = trovaPrenotazioneValida(codicePrenotazione);

            // Carica dettagli proiezione
            Proiezione proiezione = proiezioneDao.trovaPerIId(prenotazione.getIdProiezione());
            prenotazione.setProiezione(proiezione);

            prenotazioneView.mostraDettagliCompleti(prenotazione);

        } catch (SQLException e) {
            throw new DatabaseException("Errore durante la visualizzazione della prenotazione", e);
        }
    }

    /**
     * Trova e valida l'esistenza di una prenotazione
     */
    private Prenotazione trovaPrenotazioneValida(String codicePrenotazione)
            throws PrenotazioneNonTrovataException, DatabaseException {
        try {
            Prenotazione prenotazione = prenotazioneDao.trovaPerCodice(codicePrenotazione);

            if (prenotazione == null) {
                throw new PrenotazioneNonTrovataException(codicePrenotazione);
            }

            return prenotazione;

        } catch (SQLException e) {
            throw new DatabaseException("Errore nella ricerca della prenotazione", e);
        }
    }

    /**
     * Gestisce centralmente tutte le eccezioni del sistema prenotazioni
     */
    private void gestisciEccezionePrenotazione(PrenotazioneException e) {
        logger.log(Level.INFO, "Eccezione gestita: " + e.getCodiceErrore(), e);

        switch (e.getCodiceErrore()) {
            case "PROIEZIONE_NON_DISPONIBILE" ->
                    prenotazioneView.mostraMessaggioErrore("ℹ️ " + e.getMessage());

            case "POSTO_NON_DISPONIBILE" -> {
                prenotazioneView.mostraMessaggioErrore("❌ " + e.getMessage());
                if (e instanceof PostoNonDisponibileException pnde) {
                    prenotazioneView.mostraMessaggio("💡 Suggerimento: Prova con un posto vicino a " +
                            pnde.getFila() + pnde.getNumeroPosto());
                }
            }

            case "PRENOTAZIONE_NON_TROVATA" ->
                    prenotazioneView.mostraMessaggioErrore("🔍 " + e.getMessage() + ". Verifica il codice inserito.");

            case "PRENOTAZIONE_SCADUTA" ->
                    prenotazioneView.mostraMessaggioErrore("⏰ " + e.getMessage());

            case "PRENOTAZIONE_GIA_CONFERMATA" ->
                    prenotazioneView.mostraMessaggioErrore("ℹ️ " + e.getMessage());

            case "INPUT_NON_VALIDO" ->
                    prenotazioneView.mostraMessaggioErrore("⚠️ " + e.getMessage());

            case "PAGAMENTO_ERROR" ->
                    prenotazioneView.mostraMessaggioErrore("💳 Errore durante il pagamento: " + e.getMessage());

            case "PRENOTAZIONE_NON_ANNULLABILE" ->
                    prenotazioneView.mostraMessaggioErrore("🚫 " + e.getMessage());

            case "DATABASE_ERROR" -> {
                prenotazioneView.mostraMessaggioErrore("💾 Si è verificato un problema tecnico. Riprova tra qualche istante.");
                logger.log(Level.SEVERE, "Errore database", e);
            }

            default ->
                    prenotazioneView.mostraMessaggioErrore("❌ Errore: " + e.getMessage());
        }
    }

    /**
     * Genera un ticket univoco per il pagamento
     */
    private String generaTicketPagamento() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}