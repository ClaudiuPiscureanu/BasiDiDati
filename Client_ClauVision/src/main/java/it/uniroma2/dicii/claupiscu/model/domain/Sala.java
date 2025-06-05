package it.uniroma2.dicii.claupiscu.model.domain;


import java.util.Objects;

public class Sala {
    private byte numSala;
    private String nomeSala;
    private byte capacita;

    public Sala() {}

    public Sala(byte numSala, String nomeSala, byte capacita) {
        this.numSala = numSala;
        this.nomeSala = nomeSala;
        this.capacita = capacita;
    }

    // Getters e Setters
    public byte getNumSala() { return numSala; }
    public void setNumSala(byte numSala) { this.numSala = numSala; }

    public String getNomeSala() { return nomeSala; }
    public void setNomeSala(String nomeSala) { this.nomeSala = nomeSala; }

    public byte getCapacita() { return capacita; }
    public void setCapacita(byte capacita) { this.capacita = capacita; }

    // Metodi utility
    public int getCapacitaInt() {
        return Byte.toUnsignedInt(capacita);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sala sala = (Sala) o;
        return numSala == sala.numSala;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numSala);
    }

    @Override
    public String toString() {
        return String.format("Sala{num=%d, nome='%s', capacità=%d}",
                Byte.toUnsignedInt(numSala), nomeSala, Byte.toUnsignedInt(capacita));
    }
}