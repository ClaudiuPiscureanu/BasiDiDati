-- ================================================================
-- File: configurazione_claudb.sql
-- Scopo: Configurazione del database per la gestione di sale cinematografiche 
-- Corso di basi di dati ad ingengeria tor vergata :)
-- ================================================================


SET NAMES utf8mb4;
SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL';


DROP SCHEMA IF EXISTS claudb;
CREATE SCHEMA claudb;
USE claudb;

--
-- Table structure for table `cinema`
--

CREATE TABLE cinema(
    nome_cinema VARCHAR(45) NOT NULL,
    indirizzo VARCHAR(50)  NOT NULL, -- dato che non e' un dato essenziale utiliziamo semplicemente una stringa, sara' compito dell'applicazione struttutare la formattazione
    orario_apertura TIME NOT NULL,
    PRIMARY KEY  (nome_cinema)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `sala`
--

CREATE TABLE sala(
    num_sala TINYINT NOT NULL AUTO_INCREMENT,  -- tinyint da 0 a 255 	Da 2^0-1 a 2^8-1 	1 byte 
    nome_cinema VARCHAR(45) NOT NULL,
    PRIMARY KEY(num_sala),
    FOREIGN KEY (nome_cinema) REFERENCES cinema(nome_cinema)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `posto`
--

CREATE TABLE posto (
    id_posto VARCHAR(3) NOT NULL,
    num_sala TINYINT NOT NULL,
    fila CHAR(1),
    num_posto TINYINT NOT NULL,
    PRIMARY KEY (id_posto),
    FOREIGN KEY (num_sala) REFERENCES sala(num_sala)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `film`
--


CREATE TABLE film(
    titolo_film VARCHAR(128) NOT NULL PRIMARY KEY,
    durata TINYINT UNSIGNED NOT NULL CHECK (durata > 0), -- Durata in minuti
    CasaCinematografica VARCHAR(255) NOT NULL
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- Table structure for table `attore`
--
CREATE TABLE attore(
    id_attore TINYINT UNSIGNED NOT NULL,
    nominativo VARCHAR(100),
    descrizione_attore VARCHAR(255),
    PRIMARY KEY (id_attore)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
--
-- Table structure for table `cast_attori`
--

CREATE TABLE cast_attori(
    titolo_film VARCHAR(128),
    id_attore TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY (titolo_film, id_attore),  
    FOREIGN KEY (titolo_film) REFERENCES film(titolo_film) ON DELETE CASCADE, -- il comando on delete cascade, calcella tutti gli attori dopo aver eliminato il film
    FOREIGN KEY (id_attore) REFERENCES  attore(id_attore) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `proiezione`
--

CREATE TABLE proiezione(
    id_proiezione SMALLINT  UNSIGNED NOT NULL AUTO_INCREMENT,        -- The unsigned range is 0 to 65535.
    titolo_film VARCHAR(128) NOT NULL,
    num_sala TINYINT UNSIGNED NOT NULL,
    prezzo DECIMAL(5,2) NOT NULL,
    data_ora_inizio DATETIME NOT NULL,
    PRIMARY KEY(id_proiezione),
    FOREIGN KEY (titolo_film) REFERENCES film(titolo_film),
    FOREIGN KEY (num_sala) REFERENCES sala(num_sala)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `prenotazione`
--

CREATE TABLE prenotazione(
    codice_prenotazione SMALLINT  UNSIGNED NOT NULL AUTO_INCREMENT,    -- The unsigned range is 0 to 65535.
    id_posto VARCHAR(3) NOT NULL,   
    id_proiezione SMALLINT  UNSIGNED NOT NULL,
    data_ora_prenotazione DATETIME NOT NULL,
    data_ora_conferma DATETIME NOT NULL,
    -- data_ora_scadenza DATETIME, -- Nuovo campo per la scadenza della prenotazione
    -- data_ora_annullamento DATETIME, -- Nuovo campo per l'annullamento della prenotazione
    stato BOOLEAN DEFAULT NULL,  -- stato ENUM('in attesa', 'confermata', 'annullata') DEFAULT 'in attesa',
    numero_carta VARCHAR(16),
    intestatario VARCHAR(50),
    cvv VARCHAR(3) NOT NULL, -- a livello client 
    scadenza DATE,
    PRIMARY KEY(codice_prenotazione),
    FOREIGN KEY (id_proiezione) REFERENCES proiezione(id_proiezione),
    FOREIGN KEY (id_posto) REFERENCES posto(id_posto),
    UNIQUE(codice_prenotazione,id_posto)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `report'
--

CREATE TABLE report(
    anno YEAR NOT NULL,
    mese TINYINT NOT NULL,
    num_sala TINYINT NOT NULL,
    perc_annulati DECIMAL(10,2) NOT NULL,
    perc_confermati DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (anno, mese, num_sala),
    FOREIGN KEY (num_sala) REFERENCES sala(num_sala)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE utente(
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    ruolo ENUM('proprietario', 'personale','cliente') NOT NULL,
    PRIMARY KEY (username)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
