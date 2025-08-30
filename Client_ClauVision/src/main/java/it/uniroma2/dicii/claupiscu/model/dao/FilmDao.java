package it.uniroma2.dicii.claupiscu.model.dao;

import it.uniroma2.dicii.claupiscu.model.domain.Film;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FilmDao {

    /**
     * Ottiene tutti i film disponibili
     */
    public List<Film> trovaTuttiFilm() throws SQLException {
        String sql = """
            SELECT titolo_film, durata_minuti, casa_cinematografica, cast_attori
            FROM film
            ORDER BY titolo_film
            """;

        List<Film> films = new ArrayList<>();

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                films.add(mapResultSetToFilm(rs));
            }
        }

        return films;
    }

    /**
     * Trova un film per titolo
     */
    public Film trovaPerTitolo(String titolo) throws SQLException {
        String sql = """
            SELECT titolo_film, durata_minuti, casa_cinematografica, cast_attori
            FROM film
            WHERE titolo_film = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titolo);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapResultSetToFilm(rs);
            }
            return null;
        }
    }

    /**
     * Mappa un ResultSet a un oggetto Film
     */
    private Film mapResultSetToFilm(ResultSet rs) throws SQLException {
        Film film = new Film();

        film.setTitoloFilm(rs.getString("titolo_film"));
        film.setDurataMinuti(rs.getByte("durata_minuti"));
        film.setCasaCinematografica(rs.getString("casa_cinematografica"));

        String castJson = rs.getString("cast_attori");
        if (castJson != null && !castJson.trim().isEmpty()) {
            film.setCastAttori(castJson);
        }

        return film;
    }

    /**
     * Inserisce un nuovo film nel database
     */
    public void inserisci(Film film) throws SQLException {
        String sql = """
            INSERT INTO film (titolo_film, durata_minuti, casa_cinematografica, cast_attori)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, film.getTitoloFilm());
            stmt.setByte(2, film.getDurataMinuti());
            stmt.setString(3, film.getCasaCinematografica());
            stmt.setString(4, film.getCastAttori());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Inserimento film fallito, nessuna riga interessata.");
            }
        }
    }

    /**
     * Aggiorna un film esistente
     */
    public boolean aggiorna(Film film) throws SQLException {
        String sql = """
            UPDATE film 
            SET durata_minuti = ?, casa_cinematografica = ?, cast_attori = ?
            WHERE titolo_film = ?
            """;

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setByte(1, film.getDurataMinuti());
            stmt.setString(2, film.getCasaCinematografica());
            stmt.setString(3, film.getCastAttori());
            stmt.setString(4, film.getTitoloFilm());

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Elimina un film per titolo
     */
    public boolean elimina(String titoloFilm) throws SQLException {
        String sql = "DELETE FROM film WHERE titolo_film = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, titoloFilm);
            return stmt.executeUpdate() > 0;
        }
    }

}