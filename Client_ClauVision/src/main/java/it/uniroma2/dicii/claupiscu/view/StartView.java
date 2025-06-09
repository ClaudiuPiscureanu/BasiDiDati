
package it.uniroma2.dicii.claupiscu.view;

import java.io.IOException;
import java.util.Scanner;

public class StartView {
    int choice;
    public int menuInziale () throws IOException {
      System.out.println("Benvenuto in Claupiscu");
            System.out.println("Scegli quale operazione vuoi eseguire:");
            System.out.println("1. Vedi film disponibili");
            System.out.println("2. Annulla una prenotazione");
            System.out.println("3. Manutenzione");

        Scanner input = new Scanner(System.in);
        int choice = 0;
        while (true) {
            System.out.print("->  ");
            choice = input.nextInt();
            if (choice >= 1 && choice <= 3) {
                break;
            }
            System.out.println("opzione non valida, scegli tra 1 e 3: ");
        }

        return choice;
}
}