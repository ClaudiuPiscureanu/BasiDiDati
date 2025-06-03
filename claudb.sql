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
    num_sala TINYINT UNSIGNED NOT NULL AUTO_INCREMENT,  -- tinyint da 0 a 255 	Da 2^0-1 a 2^8-1 	1 byte 
    nome_sala VARCHAR(45) NOT NULL,
    capacita TINYINT UNSIGNED NOT NULL, -- La capienza della sala, da 0 a 255 posti
    PRIMARY KEY(num_sala)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `posto`
--

CREATE TABLE posto (
    num_sala TINYINT UNSIGNED NOT NULL,
    fila CHAR(1) NOT NULL,
    num_posto TINYINT UNSIGNED NOT NULL,
    PRIMARY KEY(num_sala, fila, num_posto),
    FOREIGN KEY (num_sala) REFERENCES sala(num_sala) ON DELETE CASCADE
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `film`
--


CREATE TABLE film(
    titolo_film VARCHAR(128) NOT NULL PRIMARY KEY,
    durata_minuti TINYINT UNSIGNED NOT NULL CHECK (durata_minuti > 0), -- Durata in minuti
    casa_cinematografica VARCHAR(100),  
    cast_attori TEXT, -- elenco degli attori, in formato JSON, per esempio: [{"id_attore": 1, "nominativo": "Attore 1"}, {"id_attore": 2, "nominativo": "Attore 2"}]
    PRIMARY KEY (titolo_film)
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
    data_ora_fine DATETIME NOT NULL, -- AGGIUNTO per calcoli
        stato_proiezione ENUM('PROGRAMMATA', 'IN_CORSO', 'TERMINATA') DEFAULT 'PROGRAMMATA',
    PRIMARY KEY(id_proiezione),
    FOREIGN KEY (titolo_film) REFERENCES film(titolo_film) ON DELETE CASCADE,
    FOREIGN KEY (num_sala) REFERENCES sala(num_sala) ON DELETE CASCADE,
    INDEX idx_data_inizio (data_ora_inizio),
    INDEX idx_sala_data (num_sala, data_ora_inizio)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `prenotazione`
--

CREATE TABLE prenotazione(
    codice_prenotazione VARCHAR(20) NOT NULL,   -- The unsigned range is 0 to 65535.
    num_sala TINYINT UNSIGNED NOT NULL,
    fila CHAR(1) NOT NULL,
    numero_posto TINYINT UNSIGNED NOT NULL,
    id_proiezione SMALLINT  UNSIGNED NOT NULL,
    data_ora_prenotazione DATETIME NOT NULL,
    data_ora_conferma DATETIME NOT NULL,
    stato_prenotazione ENUM('TEMPORANEA', 'CONFERMATA', 'ANNULLATA', 'SCADUTA') DEFAULT 'TEMPORANEA',
    timestamp_creazione DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  
    ticket_pag VARCHAR(50),--verrà memorizzato unicamente un ticket e non le informazioni di pagamento per delegare tutta la logica annessa a programmi esterni 
    timestamp_conferma DATETIME NULL,
    timestamp_scadenza DATETIME NOT NULL, -- Calcolato: creazione + 10 minuti
    PRIMARY KEY(codice_prenotazione),
    FOREIGN KEY (id_proiezione) REFERENCES proiezione(id_proiezione),
    FOREIGN KEY (num_sala, fila, numero_posto) REFERENCES posto(num_sala, fila, numero_posto),
    UNIQUE KEY uk_proiezione_posto (id_proiezione, num_sala, fila, numero_posto),
    INDEX idx_stato_scadenza (stato_prenotazione, timestamp_scadenza),
    INDEX idx_proiezione (id_proiezione),
    INDEX idx_timestamp_creazione (timestamp_creazione)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Table structure for table `Lock Distribuiti` (per gestione concorrenza)
--
CREATE TABLE distributed_locks(
    lock_name VARCHAR(100) NOT NULL,
    acquired_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NOT NULL,
    session_id VARCHAR(50) NOT NULL,
    PRIMARY KEY(lock_name),
    INDEX idx_expires (expires_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `Log Operazioni' (per audit e recovery)
--
CREATE TABLE log_operazioni(
    id_log BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    operazione ENUM('PRENOTAZIONE_CREATA', 'PRENOTAZIONE_CONFERMATA', 'PRENOTAZIONE_ANNULLATA', 'PRENOTAZIONE_SCADUTA') NOT NULL,
    codice_prenotazione VARCHAR(20),
    id_proiezione SMALLINT UNSIGNED,
    dettagli JSON,
    timestamp_operazione TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY(id_log),
    INDEX idx_timestamp (timestamp_operazione),
    INDEX idx_prenotazione (codice_prenotazione)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


--
-- Table structure for table `report'
--

CREATE TABLE report(
    anno YEAR NOT NULL,
    mese TINYINT NOT NULL,
    num_sala TINYINT UNSIGNED NOT NULL,
    perc_annullati DECIMAL(10,2) NOT NULL,
    perc_confermati DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (anno, mese, num_sala),
    FOREIGN KEY (num_sala) REFERENCES sala(num_sala)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


CREATE TABLE utente(
    username VARCHAR(50) NOT NULL,
    password VARCHAR(50) NOT NULL,
    ruolo ENUM('proprietario', 'personale','cliente','guest') NOT NULL,
    PRIMARY KEY (username)
)ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;


-- =============================================
-- STORED PROCEDURES PER SISTEMA CINEMA
-- =============================================

DELIMITER //

-- =============================================
-- PROCEDURA: Crea Prenotazione Temporanea
-- Gestisce lock e concorrenza per la selezione posto
-- =============================================

CREATE PROCEDURE CreaPrenotazioneTemporanea(
    IN p_id_proiezione SMALLINT UNSIGNED,
    IN p_fila CHAR(1),
    IN p_numero_posto TINYINT UNSIGNED,
    OUT p_codice_prenotazione VARCHAR(20),
    OUT p_risultato