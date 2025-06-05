package it.uniroma2.dicii.claupiscu.model.domain;

import java.util.Objects;

public class Posto {
    private byte numSala;
    private char fila;
    private byte numPosto;

    public Posto() {}

    public Posto(byte numSala, char fila, byte numPosto) {
        this.numSala = numSala;
        this.fila = fila;
        this.numPosto = numPosto;
    }

    // Getters e Setters
    public byte getNumSala() { return numSala; }
    public void setNumSala(byte numSala) { this.numSala = numSala; }

    public char getFila() { return fila; }
    public void setFila(char fila) { this.fila = fila; }

    public byte getNumPosto() { return numPosto; }
    public void setNumPosto(byte numPosto) { this.numPosto = numPosto; }

    // Metodi utility
    public String getCodiceCompleto() {
        return String.format("%c%02d", fila, Byte.toUnsignedInt(numPosto));
    }

    public boolean isValid() {
        return numSala > 0 && fila >= 'A' && fila <= 'Z' && numPosto > 0;
    }

    public int getNumSalaInt() {
        return Byte.toUnsignedInt(numSala);
    }

    public int getNumPostoInt() {
        return Byte.toUnsignedInt(numPosto);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Posto posto = (Posto) o;
        return numSala == posto.numSala && fila == posto.fila && numPosto == posto.numPosto;
    }

    @Override
    public int hashCode() {
        return Objects.hash(numSala, fila, numPosto);
    }

    @Override
    public String toString() {
        return String.format("Posto{sala=%d, posto=%s}", getNumSalaInt(), getCodiceCompleto());
    }
}