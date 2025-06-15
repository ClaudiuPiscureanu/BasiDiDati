package it.uniroma2.dicii.claupiscu.model.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Proiezione {
    public enum StatoProiezione {
        PROGRAMMATA, IN_CORSO, TERMINATA
    }

    // Attributi principali
    private short idProiezione;
    private String titoloFilm;
    private byte numSala;
    private BigDecimal prezzo;
    private LocalDateTime dataOraInizio;
    private LocalDateTime dataOraFine;
    private StatoProiezione statoProiezione;

    // Attributi derivati/calcolati (non persistiti direttamente)
    private byte durataMinuti;
    private String nomeSala;

    // Riferimenti agli oggetti correlati (per join lazy)
    private Film film;
    private Sala sala;

    // Costruttori
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
    public short getIdProiezione() {
        return idProiezione;
    }

    public void setIdProiezione(short idProiezione) {
        this.idProiezione = idProiezione;
    }

    public String getTitoloFilm() {
        return titoloFilm;
    }

    public void setTitoloFilm(String titoloFilm) {
        this.titoloFilm = titoloFilm;
    }

    public byte getNumSala() {
        return numSala;
    }

    public void setNumSala(byte numSala) {
        this.numSala = numSala;
    }

    public BigDecimal getPrezzo() {
        return prezzo;
    }

    public void setPrezzo(BigDecimal prezzo) {
        this.prezzo = prezzo;
    }

    public LocalDateTime getDataOraInizio() {
        return dataOraInizio;
    }

    public void setDataOraInizio(LocalDateTime dataOraInizio) {
        this.dataOraInizio = dataOraInizio;
    }

    public LocalDateTime getDataOraFine() {
        return dataOraFine;
    }

    public void setDataOraFine(LocalDateTime dataOraFine) {
        this.dataOraFine = dataOraFine;
    }

    public StatoProiezione getStatoProiezione() {
        return statoProiezione;
    }

    public void setStatoProiezione(StatoProiezione statoProiezione) {
        this.statoProiezione = statoProiezione;
    }

    // Getters/Setters per attributi derivati
    public byte getDurataMinuti() {
        return durataMinuti;
    }

    public void setDurataMinuti(byte durataMinuti) {
        this.durataMinuti = durataMinuti;
    }

    public String getNomeSala() {
        // Priorità: nome esplicito > nome da oggetto sala > fallback
        if (nomeSala != null && !nomeSala.trim().isEmpty()) {
            return nomeSala;
        }
        if (sala != null && sala.getNomeSala() != null) {
            return sala.getNomeSala();
        }
        return "Sala " + Byte.toUnsignedInt(numSala);
    }

    public void setNomeSala(String nomeSala) {
        this.nomeSala = nomeSala;
    }

    // Getters/Setters per riferimenti oggetti
    public Film getFilm() {
        return film;
    }

    public void setFilm(Film film) {
        this.film = film;
        // Sincronizza il titolo se necessario
        if (film != null && film.getTitoloFilm() != null) {
            this.titoloFilm = film.getTitoloFilm();
        }
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
        // Sincronizza il numero sala se necessario
        if (sala != null) {
            this.numSala = sala.getNumSala();
        }
    }

    // Metodi utility per conversioni tipo
    public int getIdProiezioneInt() {
        return Short.toUnsignedInt(idProiezione);
    }

    public int getNumSalaInt() {
        return Byte.toUnsignedInt(numSala);
    }

    public int getDurataMinutiInt() {
        return Byte.toUnsignedInt(durataMinuti);
    }

    // Metodi per formattazione date/orari
    public String getDataFormattata() {
        if (dataOraInizio == null) return "";
        return dataOraInizio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public String getOrarioFormattato() {
        if (dataOraInizio == null || dataOraFine == null) return "";
        String inizio = dataOraInizio.format(DateTimeFormatter.ofPattern("HH:mm"));
        String fine = dataOraFine.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("%s - %s", inizio, fine);
    }

    public String getOrarioCompleto() {
        if (dataOraInizio == null || dataOraFine == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String inizio = dataOraInizio.format(formatter);
        String fine = dataOraFine.format(DateTimeFormatter.ofPattern("HH:mm"));
        return String.format("%s - %s", inizio, fine);
    }

    // Metodi per controllo stato temporale
    public boolean isInCorso() {
        if (dataOraInizio == null || dataOraFine == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return now.isAfter(dataOraInizio) && now.isBefore(dataOraFine);
    }

    public boolean isTerminata() {
        if (dataOraFine == null) return false;
        return LocalDateTime.now().isAfter(dataOraFine);
    }

    public boolean isProgrammata() {
        if (dataOraInizio == null) return false;
        return LocalDateTime.now().isBefore(dataOraInizio);
    }

    /**
     * Aggiorna automaticamente lo stato della proiezione basandosi sull'orario corrente
     */
    public void aggiornaStatoTemporale() {
        if (isTerminata()) {
            this.statoProiezione = StatoProiezione.TERMINATA;
        } else if (isInCorso()) {
            this.statoProiezione = StatoProiezione.IN_CORSO;
        } else if (isProgrammata()) {
            this.statoProiezione = StatoProiezione.PROGRAMMATA;
        }
    }

    /**
     * Validazione dei dati della proiezione
     */
    public boolean isValid() {
        return titoloFilm != null && !titoloFilm.trim().isEmpty() &&
                numSala > 0 &&
                prezzo != null && prezzo.compareTo(BigDecimal.ZERO) > 0 &&
                dataOraInizio != null &&
                dataOraFine != null &&
                dataOraFine.isAfter(dataOraInizio);
    }

    // Equals e HashCode basati sull'ID primario
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