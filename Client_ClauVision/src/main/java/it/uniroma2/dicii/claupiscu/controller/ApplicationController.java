package it.uniroma2.dicii.claupiscu.controller;

import it.uniroma2.dicii.claupiscu.view.StartView;

import java.io.IOException;

public class ApplicationController implements Controller{
    GestioneCinemaController gestioneCinemaController = new GestioneCinemaController();
    PrenotazioneController prenotazioneController = new PrenotazioneController();

    @Override
    public void start()  {
        StartView startView = new StartView();
        int choice;
        System.out.println("ApplicationController started!");
        choice = startView.menuInziale();
        switch (choice) {
            case 1:
                //prenota biglietto
                System.out.println("GestioneCinemaController started!");
                prenotazioneController.start();
                break;
            case 2:
                //manutenzione
                gestioneCinemaController.start();
                System.out.println("ManutenzioneController started!");
                break;
        }

    }
}
