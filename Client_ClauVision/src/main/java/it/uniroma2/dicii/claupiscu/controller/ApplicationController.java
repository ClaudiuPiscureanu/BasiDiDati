package it.uniroma2.dicii.claupiscu.controller;

import it.uniroma2.dicii.claupiscu.view.StartView;

public class ApplicationController implements Controller{
    GestioneCinemaController gestioneCinemaController = new GestioneCinemaController();
    PrenotazioneController prenotazioneController = new PrenotazioneController();

    @Override
    public void start() {
        System.out.println("ApplicationController started!");
        StartView startView = new StartView();
        int choise;
        choise = startView.startView();
        switch (choise) {
            case 1:
                //prenota biglietto
                System.out.println("GestioneCinemaController started!");
                gestioneCinemaController.start();
                break;
            case 2:
                //annulla prenotazione
                System.out.println("PrenotazioneController started!");
                prenotazioneController.start();
                break;

            case 3:
                //manutenzione
                System.out.println("ManutenzioneController started!");

        }

    }
}
