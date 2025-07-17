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
        System.out.println("║                                                                  ║");
        System.out.println("║              🔐 Inserire password amministratore                 ║");
        System.out.println("║                                                                  ║");
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

    public void mostraListaFilm(List<Film> films) {
    }

    public int mostraMenuGestioneFilm() {
        return 0;
    }
}
