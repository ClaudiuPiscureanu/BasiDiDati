package it.uniroma2.dicii.claupiscu.controller;

import it.uniroma2.dicii.claupiscu.model.dao.PrenotazioneDao;
import it.uniroma2.dicii.claupiscu.model.dao.ProiezioneDao;
import it.uniroma2.dicii.claupiscu.model.domain.Prenotazione;
import it.uniroma2.dicii.claupiscu.model.domain.Proiezione;
import it.uniroma2.dicii.claupiscu.view.PrenotazioneView;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

public class PrenotazioneController {
    private PrenotazioneDao prenotazioneDao;
    private ProiezioneDao proiezioneDao;
    private PrenotazioneView prenotazioneView;

    public PrenotazioneController() {
        this.prenotazioneDao = new PrenotazioneDao();
        this.proiezioneDao = new ProiezioneDao();
        this.prenotazioneView = new PrenotazioneView();
    }

    public void start() {
        boolean continua = true;
        int scelta;
        while (continua) {
            try{
                 scelta = prenotazioneView.mostraMenuPrenotazione();
            } catch (Exception e) {
                throw new RuntimeException("Errore durante la visualizzazione del menu: " + e.getMessage());
            }




            switch (scelta) {
                case 1 -> nuovaPrenotazione();
                case 2 -> confermaPrenotazione();
                case 3 -> annullaPrenotazione();
                case 4 -> visualizzaPrenotazione();
                case 0 -> continua = false;
                default -> prenotazioneView.mostraMessaggioErrore("Scelta non valida");
            }
        }
    }

    private void nuovaPrenotazione() {
        try {
            // 1. Mostra proiezioni disponibili
            List<Proiezione> proiezioni = proiezioneDao.trovaProiezioniFuture();
            if (proiezioni.isEmpty()) {
                prenotazioneView.mostraMessaggioErrore("Nessuna proiezione disponibile");
                return;
            }

            int sceltaProiezione = prenotazioneView.mostraMenuSelezioneProiezione(proiezioni);
            if (sceltaProiezione == 0) return;

            if (sceltaProiezione < 1 || sceltaProiezione > proiezioni.size()) {
                prenotazioneView.mostraMessaggioErrore("Selezione non valida");
                return;
            }

            Proiezione proiezioneDaPrenotare = proiezioni.get(sceltaProiezione - 1);

            // 2. Mostra mappa posti
            List<ProiezioneDao.PostoDisponibile> posti = proiezioneDao.getPostiDisponibili(proiezioneDaPrenotare.getIdProiezione());
            prenotazioneView.mostraMappaPosti(posti, proiezioneDaPrenotare);

            // 3. Selezione posto
            String postoParsed = prenotazioneView.richiediSelezionePosto();
            if (postoParsed == null) return;

            char fila = postoParsed.charAt(0);
            byte numPosto = Byte.parseByte(postoParsed.substring(1));

            // 4. Verifica disponibilità e crea prenotazione temporanea
            prenotazioneView.mostraMessaggio("Creazione prenotazione in corso...");

            PrenotazioneDao.RisultatoPrenotazione risultato = prenotazioneDao.creaPrenotazioneTemporanea(
                    proiezioneDaPrenotare.getIdProiezione(), fila, numPosto
            );

            if (risultato.codiceRisultato == 1) {
                prenotazioneView.mostraSuccesso("Prenotazione temporanea creata!");
                prenotazioneView.mostraDettagliPrenotazioneTemporanea(risultato.codicePrenotazione,
                        proiezioneDaPrenotare, postoParsed);

                // 5. Opzione per confermare immediatamente
                if (prenotazioneView.chiedereConfermaImmediata()) {
                    confermaPrenotazioneEsistente(risultato.codicePrenotazione);
                }
            } else {
                prenotazioneView.mostraMessaggioErrore(risultato.messaggio);
            }

        } catch (SQLException e) {
            prenotazioneView.mostraMessaggioErrore("Errore database: " + e.getMessage());
        } catch (Exception e) {
            prenotazioneView.mostraMessaggioErrore("Errore imprevisto: " + e.getMessage());
        }
    }

    private void confermaPrenotazione() {
        try {
            String codicePrenotazione = prenotazioneView.richiediCodicePrenotazione();
            if (codicePrenotazione == null || codicePrenotazione.trim().isEmpty()) {
                return;
            }

            confermaPrenotazioneEsistente(codicePrenotazione.trim());

        } catch (Exception e) {
            prenotazioneView.mostraMessaggioErrore("Errore: " + e.getMessage());
        }
    }

    private void confermaPrenotazioneEsistente(String codicePrenotazione) {
        try {
            // Verifica che la prenotazione esista e sia confermabile
            Prenotazione prenotazione = prenotazioneDao.trovaPerCodice(codicePrenotazione);
            if (prenotazione == null) {
                prenotazioneView.mostraMessaggioErrore("Prenotazione non trovata");
                return;
            }

            if (!prenotazione.isConfermabile()) {
                prenotazioneView.mostraMessaggioErrore("Prenotazione non confermabile (scaduta o già confermata)");
                return;
            }

            prenotazioneView.mostraDettagliPrenotazione(prenotazione);

            // Simula pagamento
            if (prenotazioneView.confermarePagamento()) {
                String ticketPagamento = generaTicketPagamento();

                PrenotazioneDao.RisultatoPrenotazione risultato = prenotazioneDao.confermaPrenotazione(
                        codicePrenotazione, ticketPagamento
                );

                if (risultato.codiceRisultato == 1) {
                    prenotazioneView.mostraSuccesso("Prenotazione confermata con successo!");
                    prenotazioneView.mostraTicketPagamento(ticketPagamento);
                } else {
                    prenotazioneView.mostraMessaggioErrore(risultato.messaggio);
                }
            }

        } catch (SQLException e) {
            prenotazioneView.mostraMessaggioErrore("Errore database: " + e.getMessage());
        }
    }

    private void annullaPrenotazione() {
        try {
            String codicePrenotazione = prenotazioneView.richiediCodicePrenotazione();
            if (codicePrenotazione == null || codicePrenotazione.trim().isEmpty()) {
                return;
            }

            Prenotazione prenotazione = prenotazioneDao.trovaPerCodice(codicePrenotazione);
            if (prenotazione == null) {
                prenotazioneView.mostraMessaggioErrore("Prenotazione non trovata");
                return;
            }

            prenotazioneView.mostraDettagliPrenotazione(prenotazione);

            if (prenotazioneView.confermareAnnullamento()) {
                PrenotazioneDao.RisultatoPrenotazione risultato = prenotazioneDao.annullaPrenotazione(codicePrenotazione);

                if (risultato.codiceRisultato == 1) {
                    prenotazioneView.mostraSuccesso("Prenotazione annullata con successo");
                } else {
                    prenotazioneView.mostraMessaggioErrore(risultato.messaggio);
                }
            }

        } catch (SQLException e) {
            prenotazioneView.mostraMessaggioErrore("Errore database: " + e.getMessage());
        } catch (Exception e) {
            prenotazioneView.mostraMessaggioErrore("Errore: " + e.getMessage());
        }
    }

    private void visualizzaPrenotazione() {
        try {
            String codicePrenotazione = prenotazioneView.richiediCodicePrenotazione();
            if (codicePrenotazione == null || codicePrenotazione.trim().isEmpty()) {
                return;
            }

            Prenotazione prenotazione = prenotazioneDao.trovaPerCodice(codicePrenotazione);
            if (prenotazione == null) {
                prenotazioneView.mostraMessaggioErrore("Prenotazione non trovata");
                return;
            }

            // Carica anche i dettagli della proiezione
            Proiezione proiezione = proiezioneDao.trovaPerIId(prenotazione.getIdProiezione());
            prenotazione.setProiezione(proiezione);

            prenotazioneView.mostraDettagliCompleti(prenotazione);

        } catch (SQLException e) {
            prenotazioneView.mostraMessaggioErrore("Errore database: " + e.getMessage());
        } catch (Exception e) {
            prenotazioneView.mostraMessaggioErrore("Errore: " + e.getMessage());
        }
    }

    private String generaTicketPagamento() {
        return "PAY_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}