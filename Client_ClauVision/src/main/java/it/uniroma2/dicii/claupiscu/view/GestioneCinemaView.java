package it.uniroma2.dicii.claupiscu.view;

import it.uniroma2.dicii.claupiscu.model.domain.Film;

import java.util.List;
import java.util.Scanner;

public class GestioneCinemaView {
    public int mostraMenuGestioneCinema;
    Scanner scanner;

    public GestioneCinemaView() {
        this.scanner = new Scanner(System.in);
    }

    public String richiediPassword() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                   ACCESSO SISTEMA GESTIONE                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("                                                                  ");
        System.out.println("              🔐 Inserire password amministratore                 ");
        System.out.println("                                                                  ");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.print("Password: ");
        return scanner.nextLine().trim();
    }

    public int mostraMenuPrincipale() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    GESTIONE CINEMA - ADMIN                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║ [1] 🎬 Gestione Film                                            ║");
        System.out.println("║ [2] 🎭 Gestione Proiezioni                                      ║");
        System.out.println("║ [3] 🏛️  Gestione Sale                                            ║");
        System.out.println("║ [4] 📊 Statistiche Cinema                                       ║");
        System.out.println("║ [0] ↩️  Torna al menu principale                                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.print("Scelta: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public int mostraMenuGestioneFilm() {
        clearScreen();
        System.out.println("╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    GESTIONE FILM - ADMIN                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");
        System.out.println("║ [1] ➕ Aggiungi Film                                            ║");
        System.out.println("║ [2] ✏️  Modifica Film                                            ║");
        System.out.println("║ [3] ❌ Elimina Film                                             ║");
        System.out.println("║ [4] 📋 Visualizza Film                                          ║");
        System.out.println("║ [0] ↩️  Torna al menu principale                                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
        System.out.print("Scelta: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void attendiPressione() {
        System.out.println("\n⏸️  Premere INVIO per continuare...");
        scanner.nextLine();
    }

    public String richiediTitoloFilm() {
        System.out.print("\n🎬 Inserire il titolo del film: ");
        return scanner.nextLine().trim();
    }

    public int richiediDurataFilm() {
        System.out.print("⏱️  Inserire la durata in minuti (1-255): ");
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public String richiediCasaCinematografica() {
        System.out.print("🏢 Inserire la casa cinematografica: ");
        return scanner.nextLine().trim();
    }

    public List<Film.Attore> richiediAttori() {
        List<Film.Attore> attori = new java.util.ArrayList<>();
        System.out.println("\n👥 Inserimento attori (lasciare vuoto per terminare):");

        int idAttore = 1;
        while (true) {
            System.out.print("Attore " + idAttore + " - Nome (o INVIO per terminare): ");
            String nome = scanner.nextLine().trim();

            if (nome.isEmpty()) {
                break;
            }

            attori.add(new Film.Attore(idAttore, nome));
            idAttore++;
        }

        return attori;
    }

    public void mostraListaFilm(List<Film> films) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         ELENCO FILM                             ║");
        System.out.println("╠══════════════════════════════════════════════════════════════════╣");

        if (films.isEmpty()) {
            System.out.println("║ Nessun film presente nel sistema                                ║");
        } else {
            for (int i = 0; i < films.size(); i++) {
                Film film = films.get(i);
                System.out.printf("║ %2d. %-56s ║%n", i + 1, film.getTitoloFilm());
                System.out.printf("║     Durata: %-10s Casa: %-30s ║%n",
                        film.getDurataFormattata(),
                        film.getCasaCinematografica());
                System.out.printf("║     Attori: %-47s ║%n", film.getAttoriStringa());

                if (i < films.size() - 1) {
                    System.out.println("╠──────────────────────────────────────────────────────────────────╣");
                }
            }
        }

        System.out.println("╚══════════════════════════════════════════════════════════════════╝");
    }

    public Film richiediModificheFilm(Film filmEsistente) {
        System.out.println("\n✏️  MODIFICA FILM: " + filmEsistente.getTitoloFilm());
        System.out.println("(Lasciare vuoto per mantenere il valore attuale)");

        Film filmModificato = new Film();
        filmModificato.setTitoloFilm(filmEsistente.getTitoloFilm()); // Il titolo non si può modificare

        // Modifica durata
        System.out.printf("Durata attuale: %s minuti%n", filmEsistente.getDurataFormattata());
        System.out.print("Nuova durata (minuti): ");
        String durataInput = scanner.nextLine().trim();
        if (durataInput.isEmpty()) {
            filmModificato.setDurataMinuti(filmEsistente.getDurataMinuti());
        } else {
            try {
                int nuovaDurata = Integer.parseInt(durataInput);
                if (nuovaDurata > 0 && nuovaDurata <= 255) {
                    filmModificato.setDurataMinuti((byte) nuovaDurata);
                } else {
                    filmModificato.setDurataMinuti(filmEsistente.getDurataMinuti());
                    mostraMessaggioErrore("Durata non valida, mantenuto valore precedente");
                }
            } catch (NumberFormatException e) {
                filmModificato.setDurataMinuti(filmEsistente.getDurataMinuti());
                mostraMessaggioErrore("Durata non valida, mantenuto valore precedente");
            }
        }

        // Modifica casa cinematografica
        System.out.printf("Casa cinematografica attuale: %s%n", filmEsistente.getCasaCinematografica());
        System.out.print("Nuova casa cinematografica: ");
        String casaInput = scanner.nextLine().trim();
        if (casaInput.isEmpty()) {
            filmModificato.setCasaCinematografica(filmEsistente.getCasaCinematografica());
        } else {
            filmModificato.setCasaCinematografica(casaInput);
        }

        // Modifica attori
        System.out.printf("Attori attuali: %s%n", filmEsistente.getAttoriStringa());
        System.out.print("Modificare gli attori? (s/N): ");
        String modificaAttori = scanner.nextLine().trim().toLowerCase();
        if (modificaAttori.equals("s") || modificaAttori.equals("si")) {
            filmModificato.setAttori(richiediAttori());
        } else {
            filmModificato.setAttori(filmEsistente.getAttori());
        }

        return filmModificato;
    }


    public boolean confermaEliminazione(String elemento) {
        System.out.printf("\n⚠️  Sei sicuro di voler eliminare %s? (s/N): ", elemento);
        String risposta = scanner.nextLine().trim().toLowerCase();
        return risposta.equals("s") || risposta.equals("si");
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
        System.out.print("\033[2J\033[H");
    }
}