package it.uniroma2.dicii.claupiscu.controller;

import it.uniroma2.dicii.claupiscu.exception.PrenotazioneEccezioni;
import it.uniroma2.dicii.claupiscu.model.dao.PrenotazioneDao;
import it.uniroma2.dicii.claupiscu.model.dao.ProiezioneDao;
import it.uniroma2.dicii.claupiscu.model.domain.Film;
import it.uniroma2.dicii.claupiscu.view.GestioneCinemaView;
import it.uniroma2.dicii.claupiscu.exception.GestioneCinemaException;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GestioneCinemaController implements Controller {
    private static final Logger logger = Logger.getLogger(PrenotazioneController.class.getName());

    private final PrenotazioneDao prenotazioneDao;
    private final ProiezioneDao proiezioneDao;
    private final GestioneCinemaView gestioneCinemaView;

    private static final String PROPERTIES_FILE = "/home/claupiscu/Documents/Projects/programmazione/BasiDiDati/Client_ClauVision/src/main/resources/db.properties";
    public GestioneCinemaController() {
        this.prenotazioneDao = new PrenotazioneDao();
        this.proiezioneDao = new ProiezioneDao();
        this.gestioneCinemaView = new GestioneCinemaView();
    }

    /// Avvio menu principale del sistema di gestione del cinema
    public void start() throws GestioneCinemaException {
        boolean continua = true;
        try {
            if (!verificaPassword()) {
                gestioneCinemaView.mostraMessaggioErrore("Accesso negato. Password errata.");
                return;
            }

            gestioneCinemaView.mostraMessaggio("✅ Accesso autorizzato al sistema di gestione cinema");
            mostraMenuPrincipale();

        } catch (Exception e) {
            gestioneCinemaView.mostraMessaggioErrore("Errore durante l'avvio: " + e.getMessage());
        }

    }



    private boolean verificaPassword() throws GestioneCinemaException {
        String passwordCorretta = leggiPasswordDaFile();
        String passwordInserita = gestioneCinemaView.richiediPassword();

        return passwordCorretta.equals(passwordInserita);
    }

    private String leggiPasswordDaFile() throws GestioneCinemaException {
        Properties properties = new Properties();
        try (FileInputStream fis = new FileInputStream(PROPERTIES_FILE)) {
            properties.load(fis);
            String password = properties.getProperty("admin.password");
            if (password == null || password.trim().isEmpty()) {
                throw new GestioneCinemaException("Password amministratore non configurata");
            }
            return password.trim();
        } catch (IOException e) {
            throw new GestioneCinemaException("Impossibile leggere il file di configurazione: " + e.getMessage());
        }
    }

    private void mostraMenuPrincipale() {
        boolean continua = true;

        while (continua) {
            try {
                int scelta = gestioneCinemaView.mostraMenuPrincipale();

                switch (scelta) {
                    case 1 -> gestioneFilm();
                    case 2 -> gestioneProiezioni();
                    case 3 -> gestioneSale();
                    case 4 -> mostraStatistiche();
                    case 0 -> {
                        continua = false;
                        gestioneCinemaView.mostraMessaggio("Uscita dal sistema di gestione cinema");
                    }
                    default -> gestioneCinemaView.mostraMessaggioErrore("Scelta non valida");
                }

                if (continua && scelta != 0) {
                    gestioneCinemaView.attendiPressione();
                }

            } catch (Exception e) {
                gestioneCinemaView.mostraMessaggioErrore("Errore: " + e.getMessage());
                gestioneCinemaView.attendiPressione();
            }
        }
    }

    private void gestioneFilm() throws SQLException, GestioneCinemaException {
        boolean continua = true;

        while (continua) {
            int scelta = view.mostraMenuGestioneFilm();

            switch (scelta) {
                case 1 -> aggiungiFilm();
                case 2 -> eliminaFilm();
                case 3 -> visualizzaFilm();
                case 0 -> continua = false;
                default -> view.mostraMessaggioErrore("Scelta non valida");
            }
        }
    }

    private void aggiungiFilm() throws SQLException, GestioneCinemaException {
        try {
            String titolo = view.richiediTitoloFilm();
            if (titolo == null || titolo.trim().isEmpty()) {
                throw new GestioneCinemaException("Titolo film obbligatorio");
            }

            // Verifica se il film esiste già
            Film filmEsistente = filmDao.trovaPerTitolo(titolo);
            if (filmEsistente != null) {
                throw new GestioneCinemaException("Film già presente nel sistema");
            }

            int durata = view.richiediDurataFilm();
            if (durata <= 0 || durata > 300) {
                throw new GestioneCinemaException("Durata non valida (deve essere tra 1 e 300 minuti)");
            }

            String casaCinematografica = view.richiediCasaCinematografica();
            List<Film.Attore> attori = view.richiediAttori();

            Film nuovoFilm = new Film();
            nuovoFilm.setTitoloFilm(titolo);
            nuovoFilm.setDurataMinuti((byte) durata);
            nuovoFilm.setCasaCinematografica(casaCinematografica);
            nuovoFilm.setAttori(attori);

            filmDao.inserisci(nuovoFilm);
            view.mostraSuccesso("Film aggiunto con successo!");

        } catch (SQLException e) {
            throw new GestioneCinemaException("Errore durante l'inserimento del film: " + e.getMessage());
        }
    }

    private void eliminaFilm() throws SQLException, GestioneCinemaException {
        List<Film> films = filmDao.trovaTuttiFilm();
        if (films.isEmpty()) {
            view.mostraMessaggio("Nessun film presente nel sistema");
            return;
        }

        view.mostraListaFilm(films);
        String titolo = view.richiediTitoloFilm();

        Film film = filmDao.trovaPerTitolo(titolo);
        if (film == null) {
            throw new GestioneCinemaException("Film non trovato");
        }

        // Verifica se ci sono proiezioni associate
        List<Proiezione> proiezioni = proiezioneDao.trovaPerFilm(titolo);
        if (!proiezioni.isEmpty()) {
            throw new GestioneCinemaException("Impossibile eliminare: esistono proiezioni associate al film");
        }

        if (view.confermaEliminazione("film '" + titolo + "'")) {
            filmDao.elimina(titolo);
            view.mostraSuccesso("Film eliminato con successo!");
        }
    }

    private void visualizzaFilm() throws SQLException {
        List<Film> films = filmDao.trovaTuttiFilm();
        if (films.isEmpty()) {
            view.mostraMessaggio("Nessun film presente nel sistema");
        } else {
            view.mostraListaFilmDettagliata(films);
        }
    }

}
