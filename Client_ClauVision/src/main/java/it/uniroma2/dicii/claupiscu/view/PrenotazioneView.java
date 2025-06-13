package it.uniroma2.dicii.claupiscu.view;

import it.uniroma2.dicii.claupiscu.model.dao.ProiezioneDao;
import it.uniroma2.dicii.claupiscu.model.domain.Prenotazione;
import it.uniroma2.dicii.claupiscu.model.domain.Proiezione;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;

public class PrenotazioneView {
    private Scanner scanner;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public PrenotazioneView() {
        this.scanner = new Scanner(System.in);
    }

    public int mostraMenuPrenotazione() throws IOException {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    GESTIONE PRENOTAZIONI                         ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║ [1] Nuova prenotazione                                           ║");
        System.out.println("║ [2] Conferma prenotazione                                        ║");
        System.out.println("║ [3] Annulla prenotazione                                         ║");
        System.out.println("║ [4] Visualizza prenotazione                                      ║");
        System.out.println("║ [0] Torna al menu principale                                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.print("Scelta: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int mostraMenuSelezioneProiezione(List<Proiezione> proiezioni) {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SELEZIONE PROIEZIONE                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");

        for (int i = 0; i < proiezioni.size(); i++) {
            Proiezione p = proiezioni.get(i);
            long minutiMancanti = java.time.Duration.between(
                    LocalDateTime.now(),
                    p.getDataOraInizio()
            ).toMinutes();

            System.out.printf("║ [%d] %-60s ║%n", i + 1, p.getTitoloFilm());
            System.out.printf("║     🎬 Sala: %-8s  ⏰ %s (tra %d min) %10s ║%n",
                    p.getNomeSala(),
                    p.getDataOraInizio().format(FORMATTER),
                    minutiMancanti,
                    " ");
            System.out.printf("║     ⏱️  %d min     💰 €%.2f               %-13s           ║%n",
                    p.getDurataMinuti(), p.getPrezzo(),"");
            System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        }

        System.out.println("║ [0] Torna al menu principale                                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.print("Seleziona proiezione: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void mostraMappaPosti(List<ProiezioneDao.PostoDisponibile> posti, Proiezione proiezione) {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                        MAPPA POSTI                               ║");
        System.out.printf("║ Film: %-58s ║%n", proiezione.getTitoloFilm());
        System.out.printf("║ Orario: %-56s ║%n", proiezione.getDataOraInizio().format(FORMATTER));
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║                           SCHERMO                                ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");

        char filaCorrente = 0;
        for (ProiezioneDao.PostoDisponibile posto : posti) {
            if (posto.fila != filaCorrente) {
                if (filaCorrente != 0) System.out.printf(" %-40s ║%n", " ");
                filaCorrente = posto.fila;
                System.out.printf("║ %c │", posto.fila);
            }

            String simbolo = posto.disponibile ? "o" : "x";
            System.out.printf(" %s", simbolo);
        }
        if (filaCorrente != 0) System.out.printf(" %-40s ║%n", "");

        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║ Legenda: o Disponibile  x Occupato                               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    public String richiediSelezionePosto() {
        System.out.print("Inserisci il posto (es: A05, B12): ");
        String input = scanner.nextLine().trim().toUpperCase();

        if (input.isEmpty() || input.length() < 2) {
            return null;
        }

        // Validazione formato (lettera + numeri)
        if (!input.matches("[A-Z]\\d+")) {
            mostraMessaggioErrore("Formato non valido. Usa formato come A05, B12");
            return null;
        }

        return input;
    }

    public void mostraDettagliPrenotazioneTemporanea(String codice, Proiezione proiezione, String posto) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                 PRENOTAZIONE TEMPORANEA CREATA                  ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Codice: %-60s ║%n", codice);
        System.out.printf("║ Film: %-60s ║%n", proiezione.getTitoloFilm());
        System.out.printf("║ Posto: %-60s ║%n", posto);
        System.out.printf("║ Prezzo: €%-53.2f ║%n", proiezione.getPrezzo());
        System.out.println("║                                                                  ║");
        System.out.println("║ ⚠️  ATTENZIONE: Hai 10 minuti per confermare!                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    public boolean chiedereConfermaImmediata() {
        System.out.print("Vuoi confermare subito la prenotazione? (s/n): ");
        String risposta = scanner.nextLine().trim().toLowerCase();
        return risposta.equals("s") || risposta.equals("si") || risposta.equals("y") || risposta.equals("yes");
    }

    public String richiediCodicePrenotazione() {
        System.out.print("Inserisci il codice prenotazione: ");
        return scanner.nextLine().trim();
    }

    public void mostraDettagliPrenotazione(Prenotazione prenotazione) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    DETTAGLI PRENOTAZIONE                         ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Codice: %-60s ║%n", prenotazione.getCodicePrenotazione());
        System.out.printf("║ Posto: %-60s ║%n", prenotazione.getCodicePosto());
        System.out.printf("║ Stato: %-60s ║%n", prenotazione.getStatoPrenotazione());

        if (prenotazione.getStatoPrenotazione() == Prenotazione.StatoPrenotazione.TEMPORANEA) {
            System.out.printf("║ Scade tra: %-49d minuti ║%n", prenotazione.getMinutiRimanenti());
        }

        if (prenotazione.getTimestampCreazione() != null) {
            System.out.printf("║ Creata: %-54s ║%n", prenotazione.getTimestampCreazione().format(FORMATTER));
        }

        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    public void mostraDettagliCompleti(Prenotazione prenotazione) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    DETTAGLI COMPLETI                             ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Codice: %-55s ║%n", prenotazione.getCodicePrenotazione());

        if (prenotazione.getProiezione() != null) {
            System.out.printf("║ Film: %-60s ║%n", prenotazione.getProiezione().getTitoloFilm());
            System.out.printf("║ Orario: %-60s ║%n", prenotazione.getProiezione().getDataOraInizio().format(FORMATTER));
            System.out.printf("║ Prezzo: €%-53.2f ║%n", prenotazione.getProiezione().getPrezzo());
        }

        System.out.printf("║ Posto: %-60s ║%n", prenotazione.getCodicePosto());
        System.out.printf("║ Stato: %-60s ║%n", prenotazione.getStatoPrenotazione());

        if (prenotazione.getTimestampCreazione() != null) {
            System.out.printf("║ Creata: %-60s ║%n", prenotazione.getTimestampCreazione().format(FORMATTER));
        }

        if (prenotazione.getTimestampConferma() != null) {
            System.out.printf("║ Confermata: %-60s ║%n", prenotazione.getTimestampConferma().format(FORMATTER));
        }

        if (prenotazione.getTicketPag() != null) {
            System.out.printf("║ Ticket: %-54s ║%n", prenotazione.getTicketPag());
        }

        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    public boolean confermarePagamento() {
        System.out.println("\n💳 Procedi con il pagamento?");
        System.out.print("Conferma (s/n): ");
        String risposta = scanner.nextLine().trim().toLowerCase();
        return risposta.equals("s") || risposta.equals("si") || risposta.equals("y") || risposta.equals("yes");
    }

    public boolean confermareAnnullamento() {
        System.out.println("\n⚠️  Sei sicuro di voler annullare questa prenotazione?");
        System.out.print("Conferma annullamento (s/n): ");
        String risposta = scanner.nextLine().trim().toLowerCase();
        return risposta.equals("s") || risposta.equals("si") || risposta.equals("y") || risposta.equals("yes");
    }

    public void mostraTicketPagamento(String ticket) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      PAGAMENTO CONFERMATO                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Ticket di pagamento: %-42s ║%n", ticket);
        System.out.println("║                                                                  ║");
        System.out.println("║ 🎫 Conserva questo ticket come ricevuta del pagamento           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    public void mostraSuccesso(String messaggio) {
        System.out.println("\n✅ " + messaggio);
    }

    public void mostraMessaggioErrore(String messaggio) {
        System.out.println("\n❌ " + messaggio);
    }

    public void mostraMessaggio(String messaggio) {
        System.out.println("\n" + messaggio);
    }

    private void clearScreen() {
        // Semplice clear per console
        System.out.print("\033[2J\033[H");
    }
}