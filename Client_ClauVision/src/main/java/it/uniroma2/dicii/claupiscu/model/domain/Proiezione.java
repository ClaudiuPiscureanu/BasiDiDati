package it.uniroma2.dicii.claupiscu.model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Proiezione {
    public enum StatoProiezione {
        PROGRAMMATA, IN_CORSO, TERMINATA
    }

    private short idProiezione;
    private String titoloFilm;
    private byte numSala;
    private BigDecimal prezzo;
    private LocalDateTime dataOraInizio;
    private byte DurataMinuti;
    private LocalDateTime dataOraFine;
    private StatoProiezione statoProiezione;

    // Riferimenti agli oggetti correlati (per join)
    private Film film;
    private Sala sala;

    public Proiezione() {
        this.statoProiezione = StatoProiezione.PROGRAMMATA;
    }
    public Proiezione(String titoloFilm, byte numSala, LocalDateTime dataOraInizio,
                      LocalDateTime dataOraFine, BigDecimal prezzo) {
        this();
        this.titoloFilm = titoloFilm;
        this.numSala = numSala;
        this.dataOraInizio = dataOraInizio;
        this.dataOraFine = dataOraFine;
        this.prezzo = prezzo;
    }
    // Getters e Setters
    public short getIdProiezione() { return idProiezione; }
    public void setIdProiezione(short idProiezione) { this.idProiezione = idProiezione; }

    public String getTitoloFilm() { return titoloFilm; }
    public void setTitoloFilm(String titoloFilm) { this.titoloFilm = titoloFilm; }

    public byte getNumSala() { return numSala; }
    public void setNumSala(byte numSala) { this.numSala = numSala; }

    public String getNomeSala() {
        return this.sala != null ? this.sala.getNomeSala() : "Sala " + numSala;
    }
    public BigDecimal getPrezzo() { return prezzo; }
    public void setPrezzo(BigDecimal prezzo) { this.prezzo = prezzo; }

    public int getDurataMinuti() {
        return Byte.toUnsignedInt(DurataMinuti);
    }

    public void setDurataMinuti(byte durataMinuti) {
        DurataMinuti = durataMinuti;
    }

    public LocalDateTime getDataOraInizio() { return dataOraInizio; }
    public void setDataOraInizio(LocalDateTime dataOraInizio) { this.dataOraInizio = dataOraInizio; }

    public LocalDateTime getDataOraFine() { return dataOraFine; }
    public void setDataOraFine(LocalDateTime dataOraFine) { this.dataOraFine = dataOraFine; }

    public StatoProiezione getStatoProiezione() { return statoProiezione; }
    public void setStatoProiezione(StatoProiezione statoProiezione) { this.statoProiezione = statoProiezione; }

    public Film getFilm() { return film; }
    public void setFilm(Film film) { this.film = film; }

    public Sala getSala() { return sala; }
    public void setSala(Sala sala) { this.sala = sala; }
    // Metodi utility
    public int getIdProiezioneInt() {
        return Short.toUnsignedInt(idProiezione);
    }

    public int getNumSalaInt() {
        return Byte.toUnsignedInt(numSala);
    }

    public String getDataFormattata() {
        return dataOraInizio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getOrarioFormattato() {
        String inizio = dataOraInizio.format(DateTimeFormatter.ofPattern("HH:mm"));
        String fine = dataOraFine.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("%s - %s", inizio, fine);
    }

    public String getOrarioCompleto() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String inizio = dataOraInizio.format(formatter);
        String fine = dataOraFine.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("%s - %s", inizio, fine);
    }

    public boolean isInCorso() {
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(dataOraInizio) && now.isBefore(dataOraFine);
    }

    public boolean isTerminata() {
        return LocalDateTime.now().isAfter(dataOraFine);
    }

    public boolean isProgrammata() {
        return LocalDateTime.now().isBefore(dataOraInizio);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Proiezione that = (Proiezione) o;
        return idProiezione == that.idProiezione;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idProiezione);
    }

    @Override
    public String toString() {
        return String.format("Proiezione{id=%d, film='%s', sala=%d, orario='%s', prezzo=€%.2f, stato=%s}",
                getIdProiezioneInt(), titoloFilm, getNumSalaInt(), getOrarioCompleto(), prezzo, statoProiezione);
    }
}


