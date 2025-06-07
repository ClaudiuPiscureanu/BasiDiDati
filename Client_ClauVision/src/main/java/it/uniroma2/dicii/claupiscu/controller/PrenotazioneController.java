package it.uniroma2.dicii.claupiscu.controller;
import it.uniroma2.dicii.claupiscu.model.dao.FilmDao;
import it.uniroma2.dicii.claupiscu.model.dao.PostoDao;
import it.uniroma2.dicii.claupiscu.model.dao.PrenotazioneDao;
import it.uniroma2.dicii.claupiscu.model.dao.ProiezioneDao;
import it.uniroma2.dicii.claupiscu.model.domain.Film;
import it.uniroma2.dicii.claupiscu.model.domain.Posto;
import it.uniroma2.dicii.claupiscu.view.PrenotazioneView;
import java.util.Scanner;

//public class PrenotazioneController implements Controller{
//    @Override
//    public void start() {
//        System.out.println("PrenotazioneController started!");
//    }
//
//
//}
public class PrenotazioneController implements Controller {
    private ProiezioneDao proiezioneDAO;
    private FilmDao filmDAO;
    private PostoDao postoDAO;
    private PrenotazioneDao prenotazioneDAO;
    private PrenotazioneView view;
    private Scanner scanner;

    @Override
    public void start() {

    }

    public void gestisciSelezioneProiezione() {
        while (true) {
            // 1. Carica proiezioni disponibili
            List<Proiezione> proiezioni = proiezioneDAO.getProiezioniProssime();

            if (proiezioni.isEmpty()) {
                view.mostraMessaggio("Nessuna proiezione disponibile nei prossimi 30 minuti - 2 ore");
                return;
            }

            // 2. Mostra menu selezione
            int scelta = view.mostraMenuSelezioneProiezione(proiezioni);

            if (scelta == 0) {
                break; // Torna al menu principale
            }

            if (scelta < 1 || scelta > proiezioni.size()) {
                view.mostraErrore("Selezione non valida!");
                continue;
            }

            // 3. Proiezione selezionata
            Proiezione proiezioneSelezionata = proiezioni.get(scelta - 1);

            // 4. Gestisci dettagli e selezione posto
            boolean prenotazioneCompletata = gestisciDettagliEPosti(proiezioneSelezionata);

            if (prenotazioneCompletata) {
                break; // Prenotazione completata con successo
            }
        }
    }

    private boolean gestisciDettagliEPosti(Proiezione proiezione) {
        while (true) {
            // 1. Carica dettagli film (usando il riferimento nella proiezione)
            Film film = proiezione.getFilm();
            if (film == null) {
                film = filmDAO.getFilmByTitolo(proiezione.getTitoloFilm());
                proiezione.setFilm(film);
            }

            // 2. Carica posti disponibili
            Map<Character, List<Posto>> postiPerFila = postoDAO.getPostiRaggruppatiPerFila(
                    proiezione.getIdProiezioneInt()
            );

            // 3. Mostra interfaccia integrata
            int azione = view.mostraDettagliProiezione(proiezione, film, postiPerFila);

            switch (azione) {
                case 1: // Seleziona posto
                    boolean success = gestisciSelezioneDelPosto(proiezione, postiPerFila);
                    if (success) return true;
                    break;

                case 2: // Refresh posti
                    continue; // Ricarica i dati

                case 0: // Torna indietro
                    return false;

                default:
                    view.mostraErrore("Opzione non valida!");
            }
        }
    }

    private boolean gestisciSelezioneDelPosto(Proiezione proiezione, Map<Character, List<Posto>> postiPerFila) {
        // 1. Input selezione posto
        Posto postoSelezionato = view.richiedeSelezioneP posto(postiPerFila);
        if (postoSelezionato == null) return false;

        // 2. Conferma selezione
        boolean conferma = view.confermaSelezionePosto(proiezione, postoSelezionato);
        if (!conferma) return false;

        // 3. Crea prenotazione temporanea
        RisultatoPrenotazione risultato = prenotazioneDAO.creaPrenotazioneTemporanea(
                (short) proiezione.getIdProiezioneInt(),
                postoSelezionato.getFila(),
                (byte) postoSelezionato.getNumPosto()
        );

        if (!risultato.isSuccesso()) {
            view.mostraErrore("Errore prenotazione: " + risultato.getMessaggio());
            return false;
        }

        // 4. Gestisci timer e conferma
        return gestisciConfermaPrenotazione(risultato, proiezione, postoSelezionato);
    }

    private boolean gestisciConfermaPrenotazione(RisultatoPrenotazione risultato,
                                                 Proiezione proiezione, Posto posto) {
        String codicePrenotazione = risultato.getCodicePrenotazione();

        // Crea oggetto Prenotazione per il tracking
        Prenotazione prenotazione = new Prenotazione(
                codicePrenotazione,
                (short) proiezione.getIdProiezioneInt(),
                (byte) proiezione.getNumSalaInt(),
                posto.getFila(),
                (byte) posto.getNumPosto()
        );
        prenotazione.setProiezione(proiezione);
        prenotazione.setPosto(posto);

        // Timer di scadenza
        Timer timerScadenza = new Timer();
        AtomicBoolean scaduta = new AtomicBoolean(false);

        timerScadenza.schedule(new TimerTask() {
            @Override
            public void run() {
                scaduta.set(true);
                prenotazione.marcaScaduta();
            }
        }, 10 * 60 * 1000); // 10 minuti

        try {
            // Gestione interfaccia con countdown
            boolean risultatoFinale = view.mostraGestionePrenotazioneTemporanea(
                    prenotazione, scaduta, this::confermaPrenotazione, this::annullaPrenotazione
            );

            return risultatoFinale;

        } finally {
            timerScadenza.cancel();
        }
    }

    private boolean confermaPrenotazione(Prenotazione prenotazione, String ticketPag) {
        try {
            // Chiama la stored procedure di conferma
            boolean success = prenotazioneDAO.confermaPrenotazione(
                    prenotazione.getCodicePrenotazione(),
                    ticketPag
            );

            if (success) {
                prenotazione.conferma(ticketPag);
                view.mostraMessaggio("✅ Prenotazione confermata con successo!");
                view.mostraRiepilogoPrenotazione(prenotazione);
                return true;
            } else {
                view.mostraErrore("Errore durante la conferma della prenotazione");
                return false;
            }
        } catch (Exception e) {
            view.mostraErrore("Errore: " + e.getMessage());
            return false;
        }
    }

    private boolean annullaPrenotazione(Prenotazione prenotazione) {
        try {
            boolean success = prenotazioneDAO.annullaPrenotazione(prenotazione.getCodicePrenotazione());

            if (success) {
                prenotazione.annulla();
                view.mostraMessaggio("Prenotazione annullata con successo");
                return true;
            } else {
                view.mostraErrore("Errore durante l'annullamento");
                return false;
            }
        } catch (Exception e) {
            view.mostraErrore("Errore: " + e.getMessage());
            return false;
        }
    }
}
