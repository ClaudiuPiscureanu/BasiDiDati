package it.uniroma2.dicii.claupiscu.controller;

import it.uniroma2.dicii.claupiscu.model.dao.*;
import it.uniroma2.dicii.claupiscu.model.domain.*;
import it.uniroma2.dicii.claupiscu.view.GestioneCinemaView;
import it.uniroma2.dicii.claupiscu.exception.GestioneCinemaException;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class GestioneCinemaController implements Controller {
    private static final Logger logger = Logger.getLogger(GestioneCinemaController.class.getName());

    private final PrenotazioneDao prenotazioneDao;
    private final ProiezioneDao proiezioneDao;
    private final FilmDao filmDao;
    private final SalaDao salaDao;
    private final GestioneCinemaView gestioneCinemaView;

    private static final String PROPERTIES_FILE = "/home/claupiscu/Documents/Projects/programmazione/BasiDiDati/Client_ClauVision/src/main/resources/db.properties";

    public GestioneCinemaController() {
        this.prenotazioneDao = new PrenotazioneDao();
        this.proiezioneDao = new ProiezioneDao();
        this.filmDao = new FilmDao();
        this.salaDao = new SalaDao();
        this.gestioneCinemaView = new GestioneCinemaView();
    }

    @Override
    public void start() {
        try {
            if (verificaPassword()) {
                mostraMenuPrincipale();
            } else {
                gestioneCinemaView.mostraMessaggioErrore("Password non corretta. Accesso negato.");
            }
        } catch (GestioneCinemaException e) {
            gestioneCinemaView.mostraMessaggioErrore("Errore: " + e.getMessage());
        }
    }

    private boolean verificaPassword() throws GestioneCinemaException {
        String passwordInserita = gestioneCinemaView.richiediPassword();
        String passwordCorretta = leggiPasswordDaFile();
        return passwordInserita.equals(passwordCorretta);
    }

    private String leggiPasswordDaFile() throws GestioneCinemaException {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            props.load(fis);
            //proSystem.out.println();
            return props.getProperty("admin.password", "admin123");
        } catch (IOException e) {
            throw new GestioneCinemaException("Errore lettura file configurazione: " + e.getMessage());
        }
    }

    private void mostraMenuPrincipale() {
        boolean continua = true;
        while (continua) {
            int scelta = gestioneCinemaView.mostraMenuPrincipale();
            try {
                switch (scelta) {
                    case 1 -> gestioneFilm();
//                    case 2 -> gestioneProiezioni();
//                    case 3 -> gestioneSale();
//                    case 4 -> mostraStatistiche();
                    case 0 -> continua = false;
                    default -> gestioneCinemaView.mostraMessaggioErrore("Scelta non valida");
                }
            } catch (SQLException | GestioneCinemaException e) {
                gestioneCinemaView.mostraMessaggioErrore("Errore: " + e.getMessage());
                logger.severe("Errore nel menu principale: " + e.getMessage());
            }
        }
    }

    // ===== GESTIONE FILM =====
    private void gestioneFilm() throws SQLException, GestioneCinemaException {
        boolean continua = true;
        while (continua) {
            int scelta = gestioneCinemaView.mostraMenuGestioneFilm();
            switch (scelta) {
                case 1 -> aggiungiFilm();
                case 2 -> modificaFilm();
                case 3 -> eliminaFilm();
                case 4 -> visualizzaFilm();
                case 0 -> continua = false;
                default -> gestioneCinemaView.mostraMessaggioErrore("Scelta non valida");
            }
        }
    }

    private void aggiungiFilm() throws SQLException, GestioneCinemaException {
        try {
            String titolo = gestioneCinemaView.richiediTitoloFilm();

            // Verifica se esiste già
            Film esistente = filmDao.trovaPerTitolo(titolo);
            if (esistente != null) {
                throw new GestioneCinemaException("Film già presente nel sistema");
            }

            int durata = gestioneCinemaView.richiediDurataFilm();
            if (durata <= 0 || durata > 255) {
                throw new GestioneCinemaException("Durata non valida (1-255 minuti)");
            }

            String casa = gestioneCinemaView.richiediCasaCinematografica();
            List<Film.Attore> attori = gestioneCinemaView.richiediAttori();

            Film nuovoFilm = new Film();
            nuovoFilm.setTitoloFilm(titolo);
            nuovoFilm.setDurataMinuti((byte) durata);
            nuovoFilm.setCasaCinematografica(casa);
            nuovoFilm.setAttori(attori);

            filmDao.inserisci(nuovoFilm);
            gestioneCinemaView.mostraSuccesso("Film aggiunto con successo!");

        } catch (Exception e) {
            throw new GestioneCinemaException("Errore aggiunta film: " + e.getMessage());
        }
    }

    private void modificaFilm() throws SQLException, GestioneCinemaException {
        List<Film> films = filmDao.trovaTuttiFilm();
        if (films.isEmpty()) {
            gestioneCinemaView.mostraMessaggio("Nessun film presente nel sistema");
            return;
        }

        gestioneCinemaView.mostraListaFilm(films);
        String titolo = gestioneCinemaView.richiediTitoloFilm();

        Film film = filmDao.trovaPerTitolo(titolo);
        if (film == null) {
            throw new GestioneCinemaException("Film non trovato");
        }

        // Mostra dati attuali e richiedi modifiche
        Film filmModificato = gestioneCinemaView.richiediModificheFilm(film);

        if (filmDao.aggiorna(filmModificato)) {
            gestioneCinemaView.mostraSuccesso("Film modificato con successo!");
        } else {
            throw new GestioneCinemaException("Errore durante la modifica del film");
        }
    }

    private void eliminaFilm() throws SQLException, GestioneCinemaException {
        List<Film> films = filmDao.trovaTuttiFilm();
        if (films.isEmpty()) {
            gestioneCinemaView.mostraMessaggio("Nessun film presente nel sistema");
            return;
        }

        gestioneCinemaView.mostraListaFilm(films);
        String titolo = gestioneCinemaView.richiediTitoloFilm();

        Film film = filmDao.trovaPerTitolo(titolo);
        if (film == null) {
            throw new GestioneCinemaException("Film non trovato");
        }

         //Verifica se ci sono proiezioni associate
        List<Proiezione> proiezioni = proiezioneDao.trovaPerFilm(titolo);
        if (!proiezioni.isEmpty()) {
            throw new GestioneCinemaException("Impossibile eliminare: esistono proiezioni associate al film");
        }

        if (gestioneCinemaView.confermaEliminazione("film '" + titolo + "'")) {
            filmDao.elimina(titolo);
            gestioneCinemaView.mostraSuccesso("Film eliminato con successo!");
        }
    }

    private void visualizzaFilm() throws SQLException {
        List<Film> films = filmDao.trovaTuttiFilm();
        if (films.isEmpty()) {
            gestioneCinemaView.mostraMessaggio("Nessun film presente nel sistema");
        } else {
            gestioneCinemaView.mostraListaFilm(films);
        }
        gestioneCinemaView.attendiPressione();
    }

//    // ===== GESTIONE PROIEZIONI =====
//    private void gestioneProiezioni() throws SQLException, GestioneCinemaException {
//        boolean continua = true;
//        while (continua) {
//            int scelta = gestioneCinemaView.mostraMenuGestioneProiezione();
//            switch (scelta) {
//                case 1 -> aggiungiProiezione();
//                case 2 -> modificaProiezione();
//                case 3 -> eliminaProiezione();
//                case 4 -> visualizzaProiezioni();
//                case 0 -> continua = false;
//                default -> gestioneCinemaView.mostraMessaggioErrore("Scelta non valida");
//            }
//        }
//    }
//
//    private void aggiungiProiezione() throws SQLException, GestioneCinemaException {
//        // Verifica che ci siano film disponibili
//        List<Film> films = filmDao.trovaTuttiFilm();
//        if (films.isEmpty()) {
//            throw new GestioneCinemaException("Nessun film disponibile. Aggiungere prima un film.");
//        }
//
//        // Verifica che ci siano sale disponibili
//        List<Sala> sale = salaDao.trovaTutteSale();
//        if (sale.isEmpty()) {
//            throw new GestioneCinemaException("Nessuna sala disponibile. Aggiungere prima una sala.");
//        }
//
//        Proiezione nuovaProiezione = gestioneCinemaView.richiediDatiProiezione(films, sale);
//
//        // Verifica sovrapposizioni
//        if (proiezioneDao.verificaSovrapposizione(
//                nuovaProiezione.getNumSala(),
//                nuovaProiezione.getDataOraInizio(),
//                nuovaProiezione.getDataOraFine(),
//                null)) {
//            throw new GestioneCinemaException("Sovrapposizione orari con altra proiezione nella stessa sala");
//        }
//
//        proiezioneDao.inserisci(nuovaProiezione);
//        gestioneCinemaView.mostraSuccesso("Proiezione aggiunta con successo!");
//    }
//
//    private void modificaProiezione() throws SQLException, GestioneCinemaException {
//        List<Proiezione> proiezioni = proiezioneDao.trovaProiezioniFuture();
//        if (proiezioni.isEmpty()) {
//            gestioneCinemaView.mostraMessaggio("Nessuna proiezione futura disponibile per modifiche");
//            return;
//        }
//
//        gestioneCinemaView.mostraListaProiezioni(proiezioni);
//        short idProiezione = gestioneCinemaView.richiediIdProiezione();
//
//        Proiezione proiezione = proiezioneDao.trovaPerIId(idProiezione);
//        if (proiezione == null) {
//            throw new GestioneCinemaException("Proiezione non trovata");
//        }
//
//        // Verifica se è modificabile (non iniziata)
//        if (proiezione.getDataOraInizio().isBefore(LocalDateTime.now())) {
//            throw new GestioneCinemaException("Impossibile modificare una proiezione già iniziata");
//        }
//
//        Proiezione proiezioneModificata = gestioneCinemaView.richiediModificheProiezione(proiezione);
//
//        // Verifica sovrapposizioni escludendo la proiezione corrente
//        if (proiezioneDao.verificaSovrapposizione(
//                proiezioneModificata.getNumSala(),
//                proiezioneModificata.getDataOraInizio(),
//                proiezioneModificata.getDataOraFine(),
//                idProiezione)) {
//            throw new GestioneCinemaException("Sovrapposizione orari con altra proiezione nella stessa sala");
//        }
//
//        if (proiezioneDao.aggiorna(proiezioneModificata)) {
//            gestioneCinemaView.mostraSuccesso("Proiezione modificata con successo!");
//        } else {
//            throw new GestioneCinemaException("Errore durante la modifica della proiezione");
//        }
//    }
//
//    private void eliminaProiezione() throws SQLException, GestioneCinemaException {
//        List<Proiezione> proiezioni = proiezioneDao.trovaProiezioniFuture();
//        if (proiezioni.isEmpty()) {
//            gestioneCinemaView.mostraMessaggio("Nessuna proiezione futura disponibile per eliminazione");
//            return;
//        }
//
//        gestioneCinemaView.mostraListaProiezioni(proiezioni);
//        short idProiezione = gestioneCinemaView.richiediIdProiezione();
//
//        Proiezione proiezione = proiezioneDao.trovaPerIId(idProiezione);
//        if (proiezione == null) {
//            throw new GestioneCinemaException("Proiezione non trovata");
//        }
//
//        // Verifica se ci sono prenotazioni
//        List<Prenotazione> prenotazioni = prenotazioneDao.trovaPerProiezione(idProiezione);
//        if (!prenotazioni.isEmpty()) {
//            if (!gestioneCinemaView.confermaEliminazioneConPrenotazioni(prenotazioni.size())) {
//                return;
//            }
//        }
//
//        if (gestioneCinemaView.confermaEliminazione("proiezione del " + proiezione.getOrarioCompleto())) {
//            proiezioneDao.elimina(idProiezione);
//            gestioneCinemaView.mostraSuccesso("Proiezione eliminata con successo!");
//        }
//    }
//
//    private void visualizzaProiezioni() throws SQLException {
//        List<Proiezione> proiezioni = proiezioneDao.trovaProiezioniFuture();
//        if (proiezioni.isEmpty()) {
//            gestioneCinemaView.mostraMessaggio("Nessuna proiezione programmata");
//        } else {
//            gestioneCinemaView.mostraListaProiezioni(proiezioni);
//        }
//        gestioneCinemaView.attendiPressione();
//    }
//
//    // ===== GESTIONE SALE =====
//    private void gestioneSale() throws SQLException, GestioneCinemaException {
//        boolean continua = true;
//        while (continua) {
//            int scelta = gestioneCinemaView.mostraMenuGestioneSale();
//            switch (scelta) {
//                case 1 -> aggiungiSala();
//                case 2 -> modificaSala();
//                case 3 -> eliminaSala();
//                case 4 -> modificaPostiSala();
//                case 5 -> visualizzaSale();
//                case 0 -> continua = false;
//                default -> gestioneCinemaView.mostraMessaggioErrore("Scelta non valida");
//            }
//        }
//    }
//
//    private void aggiungiSala() throws SQLException, GestioneCinemaException {
//        Sala nuovaSala = gestioneCinemaView.richiediDatiSala();
//
//        // Verifica se esiste già
//        Sala esistente = salaDao.trovaPerNumero(nuovaSala.getNumSala());
//        if (esistente != null) {
//            throw new GestioneCinemaException("Sala già presente nel sistema");
//        }
//
//        salaDao.inserisci(nuovaSala);
//
//        // Genera automaticamente i posti per la sala
//        salaDao.generaPostiSala(nuovaSala.getNumSala(), nuovaSala.getCapacita());
//
//        gestioneCinemaView.mostraSuccesso("Sala aggiunta con successo con " +
//                nuovaSala.getCapacitaInt() + " posti!");
//    }
//
//    private void modificaSala() throws SQLException, GestioneCinemaException {
//        List<Sala> sale = salaDao.trovaTutteSale();
//        if (sale.isEmpty()) {
//            gestioneCinemaView.mostraMessaggio("Nessuna sala presente nel sistema");
//            return;
//        }
//
//        gestioneCinemaView.mostraListaSale(sale);
//        byte numSala = gestioneCinemaView.richiediNumeroSala();
//
//        Sala sala = salaDao.trovaPerNumero(numSala);
//        if (sala == null) {
//            throw new GestioneCinemaException("Sala non trovata");
//        }
//
//        Sala salaModificata = gestioneCinemaView.richiediModificheSala(sala);
//
//        if (salaDao.aggiorna(salaModificata)) {
//            gestioneCinemaView.mostraSuccesso("Sala modificata con successo!");
//        } else {
//            throw new GestioneCinemaException("Errore durante la modifica della sala");
//        }
//    }
//
//    private void eliminaSala() throws SQLException, GestioneCinemaException {
//        List<Sala> sale = salaDao.trovaTutteSale();
//        if (sale.isEmpty()) {
//            gestioneCinemaView.mostraMessaggio("Nessuna sala presente nel sistema");
//            return;
//        }
//
//        gestioneCinemaView.mostraListaSale(sale);
//        byte numSala = gestioneCinemaView.richiediNumeroSala();
//
//        Sala sala = salaDao.trovaPerNumero(numSala);
//        if (sala == null) {
//            throw new GestioneCinemaException("Sala non trovata");
//        }
//
//        // Verifica se ci sono proiezioni future
//        List<Proiezione> proiezioni = proiezioneDao.trovaPerSalaEPeriodo(
//                numSala, LocalDateTime.now(), LocalDateTime.now().plusYears(1));
//        if (!proiezioni.isEmpty()) {
//            throw new GestioneCinemaException("Impossibile eliminare: esistono proiezioni programmate per questa sala");
//        }
//
//        if (gestioneCinemaView.confermaEliminazione("sala " + sala.getNomeSala())) {
//            salaDao.elimina(numSala);
//            gestioneCinemaView.mostraSuccesso("Sala eliminata con successo!");
//        }
//    }
//
//    private void modificaPostiSala() throws SQLException, GestioneCinemaException {
//        List<Sala> sale = salaDao.trovaTutteSale();
//        if (sale.isEmpty()) {
//            gestioneCinemaView.mostraMessaggio("Nessuna sala presente nel sistema");
//            return;
//        }
//
//        gestioneCinemaView.mostraListaSale(sale);
//        byte numSala = gestioneCinemaView.richiediNumeroSala();
//
//        Sala sala = salaDao.trovaPerNumero(numSala);
//        if (sala == null) {
//            throw new GestioneCinemaException("Sala non trovata");
//        }
//
//        List<Posto> posti = salaDao.trovaPostiSala(numSala);
//        gestioneCinemaView.mostraPostiSala(posti, sala);
//
//        int sceltaPosti = gestioneCinemaView.mostraMenuModificaPosti();
//        switch (sceltaPosti) {
//            case 1 -> aggiungiPostiSala(sala);
//            case 2 -> rimuoviPostiSala(sala);
//            case 3 -> riorganizzaPostiSala(sala);
//        }
//    }
//
//    private void aggiungiPostiSala(Sala sala) throws SQLException, GestioneCinemaException {
//        int nuoviPosti = gestioneCinemaView.richiediNumeroNuoviPosti();
//        List<Posto> postiDaAggiungere = gestioneCinemaView.richiediDettagliNuoviPosti(nuoviPosti, sala);
//
//        for (Posto posto : postiDaAggiungere) {
//            salaDao.inserisciPosto(posto);
//        }
//
//        // Aggiorna capacità sala
//        sala.setCapacita((byte) (sala.getCapacita() + nuoviPosti));
//        salaDao.aggiorna(sala);
//
//        gestioneCinemaView.mostraSuccesso("Aggiunti " + nuoviPosti + " posti alla sala!");
//    }
//
//    private void rimuoviPostiSala(Sala sala) throws SQLException, GestioneCinemaException {
//        List<Posto> posti = salaDao.trovaPostiSala(sala.getNumSala());
//        List<Posto> postiDaRimuovere = gestioneCinemaView.selezionaPostiDaRimuovere(posti);
//
//        for (Posto posto : postiDaRimuovere) {
//            salaDao.eliminaPosto(posto);
//        }
//
//        // Aggiorna capacità sala
//        sala.setCapacita((byte) (sala.getCapacita() - postiDaRimuovere.size()));
//        salaDao.aggiorna(sala);
//
//        gestioneCinemaView.mostraSuccesso("Rimossi " + postiDaRimuovere.size() + " posti dalla sala!");
//    }
//
//    private void riorganizzaPostiSala(Sala sala) throws SQLException, GestioneCinemaException {
//        // Elimina tutti i posti esistenti
//        salaDao.eliminaTuttiPostiSala(sala.getNumSala());
//
//        // Richiedi nuova configurazione
//        int nuovaCapacita = gestioneCinemaView.richiediNuovaCapacitaSala();
//
//        // Rigenera i posti
//        salaDao.generaPostiSala(sala.getNumSala(), (byte) nuovaCapacita);
//
//        // Aggiorna capacità sala
//        sala.setCapacita((byte) nuovaCapacita);
//        salaDao.aggiorna(sala);
//
//        gestioneCinemaView.mostraSuccesso("Sala riorganizzata con " + nuovaCapacita + " posti!");
//    }
//
//    private void visualizzaSale() throws SQLException {
//        List<Sala> sale = salaDao.trovaTutteSale();
//        if (sale.isEmpty()) {
//            gestioneCinemaView.mostraMessaggio("Nessuna sala presente nel sistema");
//        } else {
//            gestioneCinemaView.mostraListaSaleDettagliate(sale);
//        }
//        gestioneCinemaView.attendiPressione();
//    }
//
//    // ===== STATISTICHE =====
//    private void mostraStatistiche() throws SQLException {
//        boolean continua = true;
//        while (continua) {
//            int scelta = gestioneCinemaView.mostraMenuStatistiche();
//            switch (scelta) {
//                case 1 -> statisticheIncassi();
//                case 2 -> statistichePrenotazioni();
//                case 3 -> statisticheFilmPopolari();
//                case 4 -> statisticheSale();
//                case 5 -> statisticheGenerali();
//                case 0 -> continua = false;
//                default -> gestioneCinemaView.mostraMessaggioErrore("Scelta non valida");
//            }
//        }
//    }
//
//    private void statisticheIncassi() throws SQLException {
//        var periodo = gestioneCinemaView.richiediPeriodoStatistiche();
//        var incassi = prenotazioneDao.calcolaIncassiPeriodo(periodo.getInizio(), periodo.getFine());
//        gestioneCinemaView.mostraStatisticheIncassi(incassi, periodo);
//    }
//
//    private void statistichePrenotazioni() throws SQLException {
//        var periodo = gestioneCinemaView.richiediPeriodoStatistiche();
//        var statsPrenotazioni = prenotazioneDao.getStatistichePrenotazioni(periodo.getInizio(), periodo.getFine());
//        gestioneCinemaView.mostraStatistichePrenotazioni(statsPrenotazioni, periodo);
//    }
//
//    private void statisticheFilmPopolari() throws SQLException {
//        var periodo = gestioneCinemaView.richiediPeriodoStatistiche();
//        var filmPopolari = prenotazioneDao.getFilmPiuPrenotati(periodo.getInizio(), periodo.getFine());
//        gestioneCinemaView.mostraStatisticheFilm(filmPopolari, periodo);
//    }
//
//    private void statisticheSale() throws SQLException {
//        var periodo = gestioneCinemaView.richiediPeriodoStatistiche();
//        var statsSale = prenotazioneDao.getStatisticheSale(periodo.getInizio(), periodo.getFine());
//        gestioneCinemaView.mostraStatisticheSale(statsSale, periodo);
//    }
//
//    private void statisticheGenerali() throws SQLException {
//        var statsGenerali = prenotazioneDao.getStatisticheGenerali();
//        gestioneCinemaView.mostraStatisticheGenerali(statsGenerali);
//    }
}