
package it.uniroma2.dicii.claupiscu.view;

import java.util.Scanner;

public class StartView {
    private Scanner  scanner = new Scanner(System.in);

    public StartView() {}
    public int startView () {
        boolean continua = true;

        while (continua) {
            stampaOpzioni();
            int scelta = leggiScelta();

            switch (scelta) {
                case 1:
                    return 1;
                case 2:
                    return 2;
                case 3:
                    return 3;
                case 0:
                    continua = false;
                    System.out.println("Arrivederci!");
                    break;
                default:
                    System.out.println("Opzione non valida. Riprova.");
                    break;
            }

            if (continua) {
                pausaEContinua();
            }
        }
        return 0;
    }

    private void stampaOpzioni() {
        System.out.println("\n" + "=".repeat(40));
        System.out.println("         MENU PRINCIPALE");
        System.out.println("=".repeat(40));
        System.out.println("1. Prenotare un biglietto");
        System.out.println("2. Annullare una prenotazione");
        System.out.println("3. Manutenzione");
        System.out.println("0. Esci");
        System.out.println("=".repeat(40));
        System.out.print("Seleziona un'opzione: ");
    }

    private int leggiScelta() {
        try {
            int scelta = scanner.nextInt();
            scanner.nextLine(); // Consuma il newline rimasto
            return scelta;
        } catch (Exception e) {
            scanner.nextLine(); // Pulisce l'input non valido
            return -1; // Ritorna un valore non valido
        }
    }
    private void pausaEContinua() {
        System.out.println("\nPremi INVIO per continuare...");
        scanner.nextLine();
    }

    /**
     * Chiude le risorse utilizzate dalla View
     */
    public void chiudi() {
        if (scanner != null) {
            scanner.close();
        }
    }
}
