
package it.uniroma2.dicii.claupiscu.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class StartView {
    private void stampaAsciiArt() {
        // tips: chage the path to the file to your own file
        try (BufferedReader reader = new BufferedReader(new FileReader("/home/claupiscu/Documents/Projects/programmazione/BasiDiDati/Client_ClauVision/src/main/resources/asciiArt.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Errore nella lettura del file ASCII art: " + e.getMessage());
        }
    }
    public int menuIniziale()    {
        stampaAsciiArt();

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("[invio]  ");
            String choice = input.nextLine().trim().toLowerCase();

            if (choice.isEmpty()) {
                // User just pressed Enter, continue to  prenotazione
                System.out.println("Comando Stellare, rispondi!");
                return 1;
            }
            else if (choice.equals("proprietario")) { // enter in proprietario mode
                System.out.println("Non posso rivelare dettagli della mia missione segreta.");
                return 2;
            }
            // If user enters something else, loop back and ask again
        }
    }


}