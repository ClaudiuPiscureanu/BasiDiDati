
package it.uniroma2.dicii.claupiscu.view;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class StartView {
    private void stampaAsciiArt() {
        try (BufferedReader reader = new BufferedReader(new FileReader("/home/claupiscu/Documents/Projects/programmazione/BasiDiDati/Client_ClauVision/src/main/resources/asciiArt.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Errore nella lettura del file ASCII art: " + e.getMessage());
        }
    }
    public int menuInziale()    {
        stampaAsciiArt();

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("[invio]  ");
            String choice = input.nextLine().trim().toLowerCase();
            if (choice.isEmpty()) return 1;
            if (choice.equals("proprietario")) return 2;
            System.out.println("premi invio per iniziare");
        }
    }


}