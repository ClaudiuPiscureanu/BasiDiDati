package it.uniroma2.dicii.claupiscu.exception;


public class PrenotazioneEccezioni {

    /**
     * Eccezione base per il sistema di prenotazioni
     */
    public static class PrenotazioneException extends Exception {
        private final String codiceErrore;

        public PrenotazioneException(String messaggio) {
            super(messaggio);
            this.codiceErrore = "GENERIC_ERROR";
        }

        public PrenotazioneException(String messaggio, String codiceErrore) {
            super(messaggio);
            this.codiceErrore = codiceErrore;
        }

        public PrenotazioneException(String messaggio, Throwable causa) {
            super(messaggio, causa);
            this.codiceErrore = "GENERIC_ERROR";
        }

        public PrenotazioneException(String messaggio, String codiceErrore, Throwable causa) {
            super(messaggio, causa);
            this.codiceErrore = codiceErrore;
        }

        public String getCodiceErrore() {
            return codiceErrore;
        }
    }

    /**
     * Eccezione lanciata quando non ci sono proiezioni disponibili
     */
    public static class ProiezioneNonDisponibileException extends PrenotazioneException {
        public ProiezioneNonDisponibileException(String messaggio) {
            super(messaggio, "PROIEZIONE_NON_DISPONIBILE");
        }
    }

    /**
     * Eccezione lanciata quando il posto selezionato non è disponibile
     */
    public static class PostoNonDisponibileException extends PrenotazioneException {
        private final char fila;
        private final byte numeroPosto;

        public PostoNonDisponibileException(char fila, byte numeroPosto) {
            super(String.format("Il posto %c%d non è disponibile", fila, numeroPosto),
                    "POSTO_NON_DISPONIBILE");
            this.fila = fila;
            this.numeroPosto = numeroPosto;
        }

        public char getFila() {
            return fila;
        }

        public byte getNumeroPosto() {
            return numeroPosto;
        }
    }

    /**
     * Eccezione lanciata quando una prenotazione non viene trovata
     */
    public static class PrenotazioneNonTrovataException extends PrenotazioneException {
        private final String codicePrenotazione;

        public PrenotazioneNonTrovataException(String codicePrenotazione) {
            super("Prenotazione con codice " + codicePrenotazione + " non trovata",
                    "PRENOTAZIONE_NON_TROVATA");
            this.codicePrenotazione = codicePrenotazione;
        }

        public String getCodicePrenotazione() {
            return codicePrenotazione;
        }
    }

    /**
     * Eccezione lanciata quando una prenotazione è scaduta
     */
    public static class PrenotazioneScadutaException extends PrenotazioneException {
        private final String codicePrenotazione;

        public PrenotazioneScadutaException(String codicePrenotazione) {
            super("La prenotazione " + codicePrenotazione + " è scaduta",
                    "PRENOTAZIONE_SCADUTA");
            this.codicePrenotazione = codicePrenotazione;
        }

        public String getCodicePrenotazione() {
            return codicePrenotazione;
        }
    }

    /**
     * Eccezione lanciata quando l'input dell'utente non è valido
     */
    public static class InputNonValidoException extends PrenotazioneException {
        public InputNonValidoException(String messaggio) {
            super(messaggio, "INPUT_NON_VALIDO");
        }
    }

    /**
     * Eccezione lanciata per problemi di accesso al database
     */
    public static class DatabaseException extends PrenotazioneException {
        public DatabaseException(String messaggio, Throwable causa) {
            super("Errore database: " + messaggio, "DATABASE_ERROR", causa);
        }

        public DatabaseException(String messaggio) {
            super("Errore database: " + messaggio, "DATABASE_ERROR");
        }
    }

    /**
     * Eccezione lanciata quando si tenta di confermare una prenotazione già confermata
     */
    public static class PrenotazioneGiaConfermataException extends PrenotazioneException {
        public PrenotazioneGiaConfermataException(String codicePrenotazione) {
            super("La prenotazione " + codicePrenotazione + " è già stata confermata",
                    "PRENOTAZIONE_GIA_CONFERMATA");
        }
    }

    /**
     * Eccezione lanciata quando si verifica un errore durante il pagamento
     */
    public static class PagamentoException extends PrenotazioneException {
        public PagamentoException(String messaggio) {
            super(messaggio, "PAGAMENTO_ERROR");
        }

        public PagamentoException(String messaggio, Throwable causa) {
            super(messaggio, "PAGAMENTO_ERROR", causa);
        }
    }

    /**
     * Eccezione lanciata quando si tenta di annullare una prenotazione non annullabile
     */
    public static class PrenotazioneNonAnnullabileException extends PrenotazioneException {
        public PrenotazioneNonAnnullabileException(String messaggio) {
            super(messaggio, "PRENOTAZIONE_NON_ANNULLABILE");
        }
    }
}