package it.uniroma2.dicii.claupiscu.model.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Prenotazione {
    public enum StatoPrenotazione {
        TEMPORANEA, CONFERMATA, ANNULLATA, SCADUTA
    }

    private String codicePrenotazione;
    private byte numSala;
    private char fila;
    private byte numPosto;
    private short idProiezione;
    private LocalDateTime dataOraPrenotazione;
    private LocalDateTime dataOraConferma;
    private StatoPrenotazione statoPrenotazione;
    private LocalDateTime timestampCreazione;
    private String ticketPag;
    private LocalDateTime timestampConferma;
    private LocalDateTime timestampScadenza;

    // Riferimenti agli oggetti correlati
    private Proiezione proiezione;
    private Posto posto;

    public Prenotazione() {
        this.statoPrenotazione = StatoPrenotazione.TEMPORANEA;
        this.timestampCreazione = LocalDateTime.now();
        // Scadenza default: 10 minuti come da schema DB
        this.timestampScadenza = timestampCreazione.plusMinutes(10);
    }

    public Prenotazione(String codicePrenotazione, short idProiezione, byte numSala,
                        char fila, byte numPosto) {
        this();
        this.codicePrenotazione = codicePrenotazione;
        this.idProiezione = idProiezione;
        this.numSala = numSala;
        this.fila = fila;
        this.numPosto = numPosto;
    }

    // Getters e Setters
    public String getCodicePrenotazione() { return codicePrenotazione; }
    public void setCodicePrenotazione(String codicePrenotazione) { this.codicePrenotazione = codicePrenotazione; }

    public byte getNumSala() { return numSala; }
    public void setNumSala(byte numSala) { this.numSala = numSala; }

    public char getFila() { return fila; }
    public void setFila(char fila) { this.fila = fila; }

    public byte getNumPosto() { return numPosto; }
    public void setNumPosto(byte numPosto) { this.numPosto = numPosto; }

    public short getIdProiezione() { return idProiezione; }
    public void setIdProiezione(short idProiezione) { this.idProiezione = idProiezione; }

    public LocalDateTime getDataOraPrenotazione() { return dataOraPrenotazione; }
    public void setDataOraPrenotazione(LocalDateTime dataOraPrenotazione) { this.dataOraPrenotazione = dataOraPrenotazione; }

    public LocalDateTime getDataOraConferma() { return dataOraConferma; }
    public void setDataOraConferma(LocalDateTime dataOraConferma) { this.dataOraConferma = dataOraConferma; }

    public StatoPrenotazione getStatoPrenotazione() { return statoPrenotazione; }
    public void setStatoPrenotazione(StatoPrenotazione statoPrenotazione) { this.statoPrenotazione = statoPrenotazione; }

    public LocalDateTime getTimestampCreazione() { return timestampCreazione; }
    public void setTimestampCreazione(LocalDateTime timestampCreazione) { this.timestampCreazione = timestampCreazione; }

    public String getTicketPag() { return ticketPag; }
    public void setTicketPag(String ticketPag) { this.ticketPag = ticketPag; }

    public LocalDateTime getTimestampConferma() { return timestampConferma; }
    public void setTimestampConferma(LocalDateTime timestampConferma) { this.timestampConferma = timestampConferma; }

    public LocalDateTime getTimestampScadenza() { return timestampScadenza; }
    public void setTimestampScadenza(LocalDateTime timestampScadenza) { this.timestampScadenza = timestampScadenza; }

    public Proiezione getProiezione() { return proiezione; }
    public void setProiezione(Proiezione proiezione) { this.proiezione = proiezione; }

    public Posto getPosto() { return posto; }
    public void setPosto(Posto posto) { this.posto = posto; }

    // Metodi utility
    public int getNumSalaInt() {
        return Byte.toUnsignedInt(numSala);
    }

    public int getNumPostoInt() {
        return Byte.toUnsignedInt(numPosto);
    }

    public int getIdProiezioneInt() {
        return Short.toUnsignedInt(idProiezione);
    }

    public String getCodicePosto() {
        return String.format("%c%02d", fila, getNumPostoInt());
    }

    public boolean isScaduta() {
        return timestampScadenza != null && LocalDateTime.now().isAfter(timestampScadenza);
    }

    public boolean isConfermabile() {
        return statoPrenotazione == StatoPrenotazione.TEMPORANEA && !isScaduta();
    }

    public long getMinutiRimanenti() {
        if (timestampScadenza == null || isScaduta()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), timestampScadenza).toMinutes();
    }

    public void conferma(String ticketPagamento) {
        if (isConfermabile()) {
            this.statoPrenotazione = StatoPrenotazione.CONFERMATA;
            this.timestampConferma = LocalDateTime.now();
            this.dataOraConferma = this.timestampConferma;
            this.ticketPag = ticketPagamento;
        }
    }

    public void annulla() {
        if (statoPrenotazione == StatoPrenotazione.TEMPORANEA ||
                statoPrenotazione == StatoPrenotazione.CONFERMATA) {
            this.statoPrenotazione = StatoPrenotazione.ANNULLATA;
        }
    }

    public void marcaScaduta() {
        if (statoPrenotazione == StatoPrenotazione.TEMPORANEA) {
            this.statoPrenotazione = StatoPrenotazione.SCADUTA;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Prenotazione that = (Prenotazione) o;
        return Objects.equals(codicePrenotazione, that.codicePrenotazione);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codicePrenotazione);
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return String.format("Prenotazione{codice='%s', proiezione=%d, posto=%s, stato=%s, creata=%s}",
                codicePrenotazione, getIdProiezioneInt(), getCodicePosto(), statoPrenotazione,
                timestampCreazione.format(formatter));
    }
}