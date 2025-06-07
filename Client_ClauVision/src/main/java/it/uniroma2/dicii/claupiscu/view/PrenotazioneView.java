package it.uniroma2.dicii.claupiscu.view;

import it.uniroma2.dicii.claupiscu.model.domain.Film;
import it.uniroma2.dicii.claupiscu.model.domain.Posto;
import it.uniroma2.dicii.claupiscu.model.domain.Prenotazione;
import it.uniroma2.dicii.claupiscu.model.domain.Proiezione;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class PrenotazioneView{
    private Scanner scanner;

    public PrenotazioneView() {
        this.scanner = new Scanner(System.in);
    }

    public int mostraMenuSelezioneProiezione(List<Proiezione> proiezioni) {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    SELEZIONE PROIEZIONE                         ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");

        for (int i = 0; i < proiezioni.size(); i++) {
            Proiezione p = proiezioni.get(i);
            long minutiMancanti = java.time.Duration.between(
                    LocalDateTime.now(),
                    p.getDataOraInizio()
            ).toMinutes();

            System.out.printf("║ [%d] %-40s ║%n", i + 1, p.getTitoloFilm());
            System.out.printf("║     🎬 %s  ⏰ %s (tra %d min) ║%n",
                    p.getNomeSala(),
                    p.getOrarioFormattato(),
                    minutiMancanti);
            System.out.printf("║     ⏱️  %d min     💰 €%.2f                    ║%n",
                    p.getDurataMinuti(), p.getPrezzo());
            System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        }

        System.out.println("║ [0] Torna al menu principale                                     ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.print("Seleziona proiezione: ");

        return leggiIntero(0, proiezioni.size());
    }

    public int mostraDettagliProiezione(Proiezione proiezione, Film film,
                                        Map<Character, List<Posto>> postiPerFila) {
        clearScreen();

        // Header informazioni film
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ 🎬 %-60s ║%n", film.getTitoloFilm());
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ 🏢 Casa: %-54s ║%n", film.getCasaCinematografica());
        System.out.printf("║ ⏱️  Durata: %d minuti                                          ║%n", film.getDurataMinuti());

        // Gestisci cast lungo
        String cast = film.getCastAttori();
        if (cast.length() > 55) {
            System.out.printf("║ 🎭 Cast: %-55s ║%n", cast.substring(0, 52) + "...");
        } else {
            System.out.printf("║ 🎭 Cast: %-55s ║%n", cast);
        }

        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ 📅 %s - %s - €%.2f              ║%n",
                proiezione.getDataFormattata(),
                proiezione.getOrarioFormattato(),
                proiezione.getPrezzo());
        System.out.printf("║ 🎪 %s                                                    ║%n",
                proiezione.getNomeSala());
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");

        // Mappa posti
        mostraMappaPosti(postiPerFila);

        // Menu opzioni
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║ [1] Seleziona posto                  ║");
        System.out.println("║ [2] Aggiorna posti disponibili       ║");
        System.out.println("║ [0] Torna alla lista proiezioni      ║");
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Scegli azione: ");

        return leggiIntero(0, 2);
    }

    private void mostraMappaPosti(Map<Character, List<Posto>> postiPerFila) {
        System.out.println("\n                           🎬 SCHERMO 🎬");
        System.out.println("    ┌─────────────────────────────────────────────────────┐");

        // Ordina le file alfabeticamente
        postiPerFila.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    char fila = entry.getKey();
                    List<Posto> posti = entry.getValue();

                    System.out.printf(" %c  │ ", fila);

                    // Ordina i posti per numero
                    posti.stream()
                            .sorted((p1, p2) -> Integer.compare(p1.getNumPosto(), p2.getNumPosto()))
                            .forEach(posto -> {
                                switch (posto.getStato()) {
                                    case DISPONIBILE:
                                        System.out.printf("%2d ", posto.getNumPosto());
                                        break;
                                    case OCCUPATO:
                                        System.out.print("██ ");
                                        break;
                                    case SELEZIONATO:
                                        System.out.printf("[%d]", posto.getNumPosto());
                                        break;
                                }
                            });
                    System.out.println(" │");
                });

        System.out.println("    └─────────────────────────────────────────────────────┘");
        System.out.println("    Legenda: [N] = Disponibile, ██ = Occupato");
    }

    public boolean mostraGestionePrenotazioneTemporanea(Prenotazione prenotazione,
                                                        AtomicBoolean scaduta,
                                                        Function<Prenotazione, Boolean> confermaCallback,
                                                        Function<Prenotazione, Boolean> annullaCallback) {

        while (!scaduta.get() && prenotazione.isConfermabile()) {
            clearScreen();

            long minutiRimasti = prenotazione.getMinutiRimanenti();
            long secondiRimasti = java.time.Duration.between(
                    LocalDateTime.now(),
                    prenotazione.getTimestampScadenza()
            ).toSeconds() % 60;

            System.out.println("╔══════════════════════════════════════════════════════════════════╗");
            System.out.println("║                  PRENOTAZIONE TEMPORANEA                        ║");
            System.out.println("╠══════════════════════════════════════════════════════════════════╣");
            System.out.printf("║ Codice: %-56s ║%n", prenotazione.getCodicePrenotazione());
            System.out.printf("║ Posto: %s                                                  ║%n",
                    prenotazione.getCodicePosto());
            System.out.printf("║ Film: %-57s ║%n", prenotazione.getProiezione().getTitoloFilm());
            System.out.printf("║ ⏰ Tempo rimasto: %02d:%02d                                      ║%n",
                    minutiRimasti, Math.max(0, secondiRimasti));
            System.out.println("╠══════════════════════════════════════════════════════════════════╣");
            System.out.println("║ [1] Conferma prenotazione (inserisci ticket pagamento)          ║");
            System.out.println("║ [2] Annulla prenotazione                                         ║");
            System.out.println("║ [0] Aspetta (aggiorna countdown)                                 ║");
            System.out.println("╚══════════════════════════════════════════════════════════════════╝");
            System.out.print("Scegli azione: ");

            int scelta = leggiInteroConTimeout(0, 2, 3000); // 3 secondi timeout

            switch (scelta) {
                case 1:
                    return gestisciConfermaFinale(prenotazione);
                case 2:
                    return annullaCallback.apply(prenotazione);
                case 0:
                default:
                    // Continua il loop per aggiornare
                    break;
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        mostraMessaggio("⚠️ Prenotazione scaduta automaticamente!");
        return false;
    }

    private boolean gestisciConfermaFinale(Prenotazione prenotazione) {
        System.out.print("\nInserisci codice ticket pagamento: ");
        String ticketPag = scanner.nextLine().trim();

        if (ticketPag.isEmpty()) {
            mostraErrore("Codice ticket non valido!");
            return false;
        }

        // Simula chiamata al DAO
        try {
            prenotazione.conferma(ticketPag);
            mostraMessaggio("✅ Prenotazione confermata con successo!");
            mostraRiepilogoPrenotazione(prenotazione);
            return true;
        } catch (Exception e) {
            mostraErrore("Errore durante la conferma: " + e.getMessage());
            return false;
        }
    }

    public void mostraRiepilogoPrenotazione(Prenotazione prenotazione) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    RIEPILOGO PRENOTAZIONE                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Codice: %-56s ║%n", prenotazione.getCodicePrenotazione());
        System.out.printf("║ Film: %-59s ║%n", prenotazione.getProiezione().getTitoloFilm());
        System.out.printf("║ Sala: %-59s ║%n", prenotazione.getProiezione().getNomeSala());
        System.out.printf("║ Posto: %-58s ║%n", prenotazione.getCodicePosto());
        System.out.printf("║ Orario: %-57s ║%n", prenotazione.getProiezione().getOrarioCompleto());
        System.out.printf("║ Prezzo: €%-56.2f ║%n", prenotazione.getProiezione().getPrezzo());
        System.out.printf("║ Ticket: %-57s ║%n", prenotazione.getTicketPag());
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.println("📧 Riceverai email di conferma a breve.");
        System.out.println("Premi INVIO per continuare...");
        scanner.nextLine();
    }

    // Metodi utility
    private int leggiIntero(int min, int max) {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                int valore = Integer.parseInt(input);
                if (valore >= min && valore <= max) {
                    return valore;
                }
                System.out.printf("Inserisci un numero tra %d e %d: ", min, max);
            } catch (NumberFormatException e) {
                System.out.print("Inserisci un numero valido: ");
            }
        }
    }

    private int leggiInteroConTimeout(int min, int max, long timeoutMs) {
        // Per semplicità, implementazione base
        // In produzione useresti input non-bloccante
        return leggiIntero(min, max);
    }

    private void clearScreen() {
        System.out.print("\033[2J\033[H");
        System.out.flush();
    }

    public void mostraMessaggio(String messaggio) {
        System.out.println("\n" + messaggio);
        System.out.println("Premi INVIO per continuare...");
        scanner.nextLine();
    }

    public void mostraErrore(String errore) {
        System.out.println("\n❌ ERRORE: " + errore);
        System.out.println("Premi INVIO per continuare...");
        scanner.nextLine();
    }

    // Altri metodi richiesti dal controller...
    public Posto richiedeSelezioneP posto(Map<Character, List<Posto>> postiPerFila) {
        System.out.println("\n📍 SELEZIONE POSTO");
        System.out.print("Inserisci fila (es. A, B, C...): ");
        String filaInput = scanner.nextLine().trim().toUpperCase();

        if (filaInput.length() != 1) {
            mostraErrore("Fila non valida!");
            return null;
        }

        char fila = filaInput.charAt(0);

        if (!postiPerFila.containsKey(fila)) {
            mostraErrore("Fila non esistente!");
            return null;
        }

        System.out.print("Inserisci numero posto: ");
        int numPosto = leggiIntero(1, 50);

        // Cerca il posto
        List<Posto> postiDiQuestaFila = postiPerFila.get(fila);
        return postiDiQuestaFila.stream()
                .filter(p -> p.getNumPosto() == numPosto)
                .filter(p -> p.getStato() == StatoPosto.DISPONIBILE)
                .findFirst()
                .orElse(null);
    }

    public boolean confermaSelezionePosto(Proiezione proiezione, Posto posto) {
        System.out.println("\n╔══════════════════════════════════════╗");
        System.out.println("║           CONFERMA SELEZIONE         ║");
        System.out.println("╠══════════════════════════════════════╣");
        System.out.printf("║ Film: %-30s ║%n", proiezione.getTitoloFilm());
        System.out.printf("║ Sala: %-30s ║%n", proiezione.getNomeSala());
        System.out.printf("║ Orario: %-28s ║%n", proiezione.getOrarioCompleto());
        System.out.printf("║ Posto: Fila %c, Numero %d           ║%n", posto.getFila(), posto.getNumPosto());
        System.out.printf("║ Prezzo: €%.2f                      ║%n", proiezione.getPrezzo());
        System.out.println("╚══════════════════════════════════════╝");
        System.out.print("Confermi la selezione? (s/n): ");

        String risposta = scanner.nextLine().trim().toLowerCase();
        return risposta.equals("s") || risposta.equals("si");
    }
}