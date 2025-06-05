package it.uniroma2.dicii.claupiscu.model.domain;

import java.util.List;
import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


public class Film {
    private String titoloFilm;
    private byte durataMinuti;
    private String casaCinematografica;
    private String castAttori; // JSON string nel database

    //lista degli attori deserializzata dal JSON
    private List<Attore> attori;

    public static class Attore{
        @JsonProperty("id_attore") private int idAttore;
        @JsonProperty("nominativo") private String nominativo;

        public Attore(){}
        public Attore(int idAttore, String nominativo){
            this.idAttore = idAttore;
            this.nominativo = nominativo;
        }

        //get set
        public int getIdAttore() { return idAttore; }
        public void setIdAttore(int idAttore) { this.idAttore = idAttore; }
        public String getNominativo() { return nominativo; }
        public void setNominativo(String nominativo) { this.nominativo = nominativo; }
        @Override
        public String toString() {
            return nominativo;
        }

    }

    public Film(){}
    public Film(String titoloFilm, byte durataMinuti, String casaCinematografica) {
        this(titoloFilm, durataMinuti);
        this.casaCinematografica = casaCinematografica;
    }
    // Getters e Setters
    public String getTitoloFilm() { return titoloFilm; }
    public void setTitoloFilm(String titoloFilm) { this.titoloFilm = titoloFilm; }

    public byte getDurataMinuti() { return durataMinuti; }
    public void setDurataMinuti(byte durataMinuti) { this.durataMinuti = durataMinuti; }

    public String getCasaCinematografica() { return casaCinematografica; }
    public void setCasaCinematografica(String casaCinematografica) { this.casaCinematografica = casaCinematografica; }

    public String getCastAttori() { return castAttori; }
    public void setCastAttori(String castAttori) {
        this.castAttori = castAttori;
        this.attori = deserializzaAttori(castAttori);
    }
    public List<Attore> getAttori() { return attori; }
    public void setAttori(List<Attore> attori) {
        this.attori = attori;
        this.castAttori = serializzaAttori(attori);
    }
    // Metodi utility
    public int getDurataMinutiInt() {
        return Byte.toUnsignedInt(durataMinuti);
    }

    public String getDurataFormattata() {
        int durata = getDurataMinutiInt();
        int ore = durata / 60;
        int minuti = durata % 60;
        if (ore > 0) {
            return String.format("%dh %02dm", ore, minuti);
        } else {
            return String.format("%dm", minuti);
        }
    }

    public String getAttoriStringa() {
        if (attori == null || attori.isEmpty()) {
            return "N/A";
        }
        return attori.stream()
                .map(Attore::getNominativo)
                .reduce((a, b) -> a + ", " + b)
                .orElse("N/A");
    }

    // Metodi per gestione JSON degli attori
    private List<Attore> deserializzaAttori(String jsonAttori) {
        if (jsonAttori == null || jsonAttori.trim().isEmpty()) {
            return List.of();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonAttori, new TypeReference<List<Attore>>() {});
        } catch (JsonProcessingException e) {
            System.err.println("Errore deserializzazione attori: " + e.getMessage());
            return List.of();
        }
    }

    private String serializzaAttori(List<Attore> attori) {
        if (attori == null || attori.isEmpty()) {
            return "[]";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(attori);
        } catch (JsonProcessingException e) {
            System.err.println("Errore serializzazione attori: " + e.getMessage());
            return "[]";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Film film = (Film) o;
        return Objects.equals(titoloFilm, film.titoloFilm);
    }

    @Override
    public int hashCode() {
        return Objects.hash(titoloFilm);
    }

    @Override
    public String toString() {
        return String.format("Film{titolo='%s', durata=%s, casa='%s', attori='%s'}",
                titoloFilm, getDurataFormattata(), casaCinematografica, getAttoriStringa());
    }
}



