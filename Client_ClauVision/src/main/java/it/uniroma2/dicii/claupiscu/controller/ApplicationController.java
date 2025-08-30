package it.uniroma2.dicii.claupiscu.controller;

import it.uniroma2.dicii.claupiscu.view.StartView;
import it.uniroma2.dicii.claupiscu.controller.GestioneCinemaController;
public class ApplicationController implements Controller{

    @Override
    public void start()  {
        StartView startView = new StartView();
        int choice;
        choice = startView.menuIniziale();
        switch (choice) {
            case 1:
                //go to booking screen
                PrenotazioneController prenotazioneController = new PrenotazioneController();
                System.out.println("Prenotazione avviata!");
                prenotazioneController.start();
                break;
            case 2:
                // go to management screen
                System.out.println("Gestione avviata!");
                GestioneCinemaController gestioneCinemaController = new GestioneCinemaController();
                gestioneCinemaController.start();
        }

    }
}
