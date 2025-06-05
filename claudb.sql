-- ================================================================
-- File: configurazione_claudb.sql
-- Scopo: Configurazione del database per la gestione di sale cinematografiche 
-- Corso di basi di dati a ingegneria tor vergata :)
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
    indirizzo VARCHAR(50)  NOT NULL, -- dato che non e un dato essenziale utilizziamo semplicemente una stringa, sara' compito dell'applicazione strutturare la formattazione
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
    titolo_film VARCHAR(128) NOT NULL,
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
    num_posto TINYINT UNSIGNED NOT NULL,
    id_proiezione SMALLINT  UNSIGNED NOT NULL,
    data_ora_prenotazione DATETIME NOT NULL,
    data_ora_conferma DATETIME NOT NULL,
    stato_prenotazione ENUM('TEMPORANEA', 'CONFERMATA', 'ANNULLATA', 'SCADUTA') DEFAULT 'TEMPORANEA',
    timestamp_creazione DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,  
    ticket_pag VARCHAR(50), -- verrà memorizzato unicamente un ticket e non le informazioni di pagamento per delegare tutta la logica annessa a programmi esterni 
    timestamp_conferma DATETIME NULL,
    timestamp_scadenza DATETIME NOT NULL, -- Calcolato: creazione + 10 minuti
    PRIMARY KEY(codice_prenotazione),
    FOREIGN KEY (id_proiezione) REFERENCES proiezione(id_proiezione),
    FOREIGN KEY (num_sala, fila, num_posto) REFERENCES posto(num_sala, fila, num_posto),
    UNIQUE KEY uk_proiezione_posto (id_proiezione, num_sala, fila, num_posto),
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
    IN p_num_posto TINYINT UNSIGNED,
    OUT p_codice_prenotazione VARCHAR(20),
    OUT p_risultato INT -- 1=successo, 0=posto occupato, -1=errore_proiezione, -2=errore_generico 
)
proc_exit: BEGIN

    -- SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE

    DECLARE v_num_sala TINYINT UNSIGNED;
    DECLARE v_data_inizio DATETIME;
    DECLARE v_prezzo DECIMAL(5,2);
    DECLARE v_lock_name VARCHAR(100);
    DECLARE v_session_id VARCHAR(50);
    DECLARE v_count_existing INT DEFAULT 0;
    DECLARE v_lock_acquired BOOLEAN DEFAULT FALSE;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        IF v_lock_acquired THEN
            DELETE FROM distributed_locks WHERE lock_name = v_lock_name AND session_id = v_session_id;
        END IF;
        SET p_risultato = -2;
    END;

    -- genera sessione ID unico 
    SET v_session_id = CONCAT(CONNECTION_ID(), '_', UNIX_TIMESTAMP(),'_', RAND());

    -- Genera codice prenotazione unico
    -- Formato del codice: RES[Anno][Mese][ID Proiezione][Fila][Numero Posto][Numero Casuale]
    SET p_codice_prenotazione = CONCAT('RES',YEAR(NOW()),MONTH(NOW()),LPAD(p_id_proiezione,4,'0'), UPPER(p_fila),LPAD(p_num_posto,2,'0'),LPAD(floor(RAND() * 1000),3,'0'));

    -- Inizia la transazione
    START TRANSACTION;

    -- verifica che la proiezione esista e recupera i dati necessari
    SELECT num_sala, data_ora_inizio, prezzo
    INTO v_num_sala, v_data_inizio, v_prezzo
    FROM proiezione 
    WHERE id_proiezione = p_id_proiezione
        AND data_ora_inizio > NOW()
        AND stato_proiezione = 'PROGRAMMATA';
    IF v_num_sala IS NULL THEN
        SET p_risultato = -1; -- Proiezione non trovata o non valida
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- creo lock specifico per posto e proiezione
    SET v_lock_name = CONCAT('seat_',p_id_proiezione, '_', v_num_sala, '_', p_fila, '_', p_num_posto);
    
    -- acquisisce lock distribuito con timeout
    INSERT INTO distributed_locks (lock_name, expires_at, session_id) 
    VALUES (v_lock_name, DATE_ADD(NOW(), INTERVAL 15 MINUTE), v_session_id)
    ON DUPLICATE KEY UPDATE lock_name = lock_name; -- fallisce se lock gia esiste

    IF ROW_COUNT() = 0 THEN
        SET p_risultato = 0; -- posto in fase di prenotazione da parte di un altro utente
        ROLLBACK;
        LEAVE proc_exit;
    END IF;
    SET v_lock_acquired = TRUE;
    -- Verifica se il posto è già prenotato
    SELECT COUNT(*) INTO v_count_existing
    from prenotazione
    WHERE  id_proiezione = p_id_proiezione
        AND num_sala  = v_num_sala
        AND fila = p_fila
        AND num_posto = p_num_posto
        AND stato_prenotazione IN ('TEMPORANEA', 'CONFERMATA');
    IF v_count_existing > 0 THEN
        SET p_risultato = 0; -- Posto già prenotato
        DELETE from distributed_locks WHERE lock_name = v_lock_name AND session_id = v_session_id;
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Verifica che il posto esista fisicamente
    SELECT COUNT(*) INTO v_count_existing
    from posto
    WHERE num_sala = v_num_sala AND fila = p_fila AND num_posto = p_num_posto;
    IF v_count_existing = 0 THEN
        SET p_risultato = -1; -- Posto non esistente
        DELETE from distributed_locks WHERE lock_name = v_lock_name AND session_id = v_session_id;
        ROLLBACK;
        LEAVE proc_exit;
    END IF;


    -- Inserisce la prenotazione temporanea
    INSERT into prenotazione(
        codice_prenotazione, 
        id_proiezione, 
        num_sala, 
        fila, 
        num_posto,
        stato_prenotazione,
        timestamp_creazione,
        timestamp_scadenza
    ) VALUES (
        p_codice_prenotazione, 
        p_id_proiezione, 
        v_num_sala, 
        p_fila, 
        p_num_posto,
        'TEMPORANEA',
        NOW(),
        DATE_ADD(NOW(), INTERVAL 10 MINUTE)
    );

    -- log operazione
    INSERT INTO log_operazioni (operazione, codice_prenotazione, id_proiezione, dettagli)
    VALUES ('PRENOTAZIONE_CREATA', p_codice_prenotazione, p_id_proiezione, JSON_OBJECT('posto', CONCAT(p_fila, p_num_posto), 'prezzo', v_prezzo));
    SET p_risultato = 1; -- Successo
    COMMIT;
    
END //
-- =============================================
-- PROCEDURA: Conferma Prenotazione
-- Gestisce il pagamento e la conferma finale
-- =============================================
CREATE PROCEDURE ConfermaPrenotazione(
    IN p_codice_prenotazione VARCHAR(20),
    IN p_ticket_pag VARCHAR(50),
    OUT p_risultato INT -- 1=successo, 0=prenotazione non trovata, -1=scaduta, -2=errore_generico
)
proc_exit: BEGIN 
    DECLARE v_stato_attuale ENUM('TEMPORANEA', 'CONFERMATA', 'ANNULLATA', 'SCADUTA');
    DECLARE v_timestamp_scadenza DATETIME;
    DECLARE v_id_proiezione SMALLINT UNSIGNED;
    DECLARE v_lock_name VARCHAR(100);
    DECLARE v_num_sala TINYINT UNSIGNED;
    DECLARE v_fila CHAR(1);
    DECLARE v_num_posto TINYINT UNSIGNED;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN 
        ROLLBACK;
        SET p_risultato = -2; -- Errore generico
    END;

    START TRANSACTION;
    
    -- Acquisisce lock esclusivo sulla prenotazione
    SELECT stato_prenotazione, timestamp_scadenza, id_proiezione, num_sala, fila, num_posto
    INTO v_stato_attuale, v_timestamp_scadenza, v_id_proiezione, v_num_sala, v_fila, v_num_posto
    FROM prenotazione
    WHERE codice_prenotazione = p_codice_prenotazione
    FOR UPDATE;

    IF v_stato_attuale IS NULL THEN
        SET p_risultato = 0; -- Prenotazione non trovata
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Verifica che sia ancora temporanea e non scaduta
    IF v_stato_attuale != 'TEMPORANEA' THEN
        SET p_risultato = -1; 
        ROLLBACK;
        LEAVE proc_exit;    
    END IF;

    IF NOW() > v_timestamp_scadenza THEN
        UPDATE prenotazione  -- Marca come scaduta
        SET stato_prenotazione = 'SCADUTA'
        WHERE codice_prenotazione = p_codice_prenotazione;
        SET p_risultato = -1; -- Prenotazione scaduta
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Conferma la prenotazione
    UPDATE prenotazione
    SET stato_prenotazione = 'CONFERMATA',
        timestamp_conferma = NOW(),
        ticket_pag = p_ticket_pag
        WHERE codice_prenotazione = p_codice_prenotazione;  
    
    -- rimuove il lock distribuito
    SET v_lock_name = CONCAT('seat_', v_id_proiezione, '_', v_num_sala, '_', v_fila, '_', v_num_posto);
    DELETE FROM distributed_locks 
    WHERE lock_name = v_lock_name;

    -- log operazione
    INSERT INTO log_operazioni (operazione, codice_prenotazione, id_proiezione, dettagli)
        VALUES ('PRENOTAZIONE_CONFERMATA', p_codice_prenotazione, v_id_proiezione, JSON_OBJECT('posto', CONCAT(v_fila, v_num_posto)));
        
    SET p_risultato = 1; -- Successo
    COMMIT;
END //

-- =============================================    
-- PROCEDURA: Annulla Prenotazione
-- Gestisce l'annullamento di una prenotazione fino a 30 minuti prima
-- =============================================

CREATE PROCEDURE AnnullaPrenotazione(
    IN p_codice_prenotazione VARCHAR(20),
    OUT p_risultato INT -- 1=successo, 0=prenotazione non trovata, -1=troppo tardi, -2=errore_generico
)
proc_exit: BEGIN
    DECLARE v_stato_attuale ENUM('TEMPORANEA', 'CONFERMATA', 'ANNULLATA', 'SCADUTA');
    DECLARE v_data_inizio DATETIME;
    DECLARE v_id_proiezione SMALLINT UNSIGNED;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_risultato = -2; -- Errore generico
    END;

    START TRANSACTION;

    -- Verifica prenotazione e orario proiezione
    SELECT p.stato_prenotazione, pr.data_ora_inizio, p.id_proiezione
    INTO v_stato_attuale, v_data_inizio, v_id_proiezione
    FROM prenotazione p
    JOIN proiezione pr ON p.id_proiezione = pr.id_proiezione
    WHERE p.codice_prenotazione = p_codice_prenotazione
    FOR UPDATE;

    IF v_stato_attuale IS NULL THEN
        SET p_risultato = 0; -- Prenotazione non trovata
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Verifica timing (30 minuti prima)
    IF NOW() > DATE_SUB(v_data_inizio, INTERVAL 30 MINUTE) THEN
        SET p_risultato = -1; -- Troppo tardi per annullare
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Annulla la prenotazione 
    IF v_stato_attuale = ('TEMPORANEA' , 'CONFERMATA') THEN
        UPDATE prenotazione
        SET stato_prenotazione = 'ANNULLATA'
        WHERE codice_prenotazione = p_codice_prenotazione;

        -- log operazione
        INSERT INTO log_operazioni (operazione, codice_prenotazione, id_proiezione)
        VALUES ('PRENOTAZIONE_ANNULLATA', p_codice_prenotazione, v_id_proiezione);
       END IF;

        SET p_risultato = 1; -- Successo
        COMMIT;
END //

-- =============================================
-- PROCEDURA: Cleanup Prenotazioni Scadute
-- Eseguita periodicamente per pulizia
-- =============================================

CREATE PROCEDURE CleanupPrenotazioniScadute()
proc_exit: BEGIN
    DECLARE v_count INT DEFAULT 0;

    START TRANSACTION;
    -- Marca come scadute le prenotazioni temporanee scadute
    UPDATE prenotazione
    SET stato_prenotazione = 'SCADUTA'
    WHERE stato_prenotazione = 'TEMPORANEA' 
        AND timestamp_scadenza < NOW();

    SET v_count = ROW_COUNT();

    -- Log delle prenotazioni scadute
    IF v_count > 0 THEN
        INSERT INTO log_operazioni (operazione, id_proiezione, dettagli)
        VALUES ('PRENOTAZIONE_SCADUTA', NULL, JSON_OBJECT('count', v_count));
    END IF;
    
    -- rimuove lock scaduti
    DELETE FROM distributed_locks 
    WHERE expires_at < NOW();

    COMMIT;

END //




-- =============================================
-- EVENTI PROGRAMMATI
-- =============================================

-- Evento per cleanup automatico ogni 5 minuti
CREATE EVENT IF NOT EXISTS cleanup_prenotazioni_scadute
ON SCHEDULE EVERY 5 MINUTE
DO
CALL CleanupPrenotazioniScadute();

-- Evento per pulizia log vecchi (mensile)
CREATE EVENT IF NOT EXISTS cleanup_log_vecchi
ON SCHEDULE EVERY 1 MONTH
STARTS CURRENT_TIMESTAMP
DO
    DELETE FROM log_operazioni WHERE timestamp_operazione < DATE_SUB(NOW(), INTERVAL 12 MONTH);

-- =============================================
-- VISTE PER REPORTING
-- =============================================

-- Vista posti disponibili per proiezione
CREATE VIEW vista_posti_disponibili AS
SELECT 
    pr.id_proiezione,
    pr.titolo_film,
    pr.data_ora_inizio,
    pr.prezzo,
    p.num_sala,
    p.fila,
    p.num_posto,
    CASE 
        WHEN res.codice_prenotazione IS NULL THEN 'DISPONIBILE'
        WHEN res.stato_prenotazione = 'TEMPORANEA' AND res.timestamp_scadenza < NOW() THEN 'DISPONIBILE'
        ELSE 'OCCUPATO'
    END as stato_posto
FROM proiezione pr
JOIN posto p ON pr.num_sala = p.num_sala
LEFT JOIN prenotazione res ON pr.id_proiezione = res.id_proiezione 
    AND p.num_sala = res.num_sala 
    AND p.fila = res.fila 
    AND p.num_posto = res.num_posto
    AND res.stato_prenotazione IN ('TEMPORANEA', 'CONFERMATA')
WHERE pr.data_ora_inizio > NOW()
  AND pr.stato_proiezione = 'PROGRAMMATA';

-- Vista report mensile per sala
CREATE VIEW vista_report_mensile AS
SELECT 
    pr.num_sala,
    s.nome_sala,
    YEAR(res.timestamp_creazione) as anno,
    MONTH(res.timestamp_creazione) as mese,
    COUNT(CASE WHEN res.stato_prenotazione = 'CONFERMATA' THEN 1 END) as prenotazioni_confermate,
    COUNT(CASE WHEN res.stato_prenotazione = 'ANNULLATA' THEN 1 END) as prenotazioni_annullate,
    SUM(CASE WHEN res.stato_prenotazione = 'CONFERMATA' THEN pr.prezzo ELSE 0 END) as incasso_totale
FROM sala s
LEFT JOIN proiezione pr ON s.num_sala = pr.num_sala
LEFT JOIN prenotazione res ON pr.id_proiezione = res.id_proiezione
WHERE res.timestamp_creazione IS NOT NULL
GROUP BY pr.num_sala, s.nome_sala, YEAR(res.timestamp_creazione), MONTH(res.timestamp_creazione);

-- Abilita eventi programmati
SET GLOBAL event_scheduler = ON;

-- =============================================
-- TRIGGER PER SISTEMA CINEMA
-- =============================================

-- TRIGGER: Log automatico cambi stato prenotazione
CREATE TRIGGER log_cambio_stato_prenotazione
    AFTER UPDATE ON prenotazione
    FOR EACH ROW
BEGIN
    IF OLD.stato_prenotazione != NEW.stato_prenotazione THEN
        INSERT INTO log_operazioni (
            operazione, 
            codice_prenotazione, 
            id_proiezione, 
            dettagli
        ) VALUES (
            CASE NEW.stato_prenotazione
                WHEN 'CONFERMATA' THEN 'PRENOTAZIONE_CONFERMATA'
                WHEN 'ANNULLATA' THEN 'PRENOTAZIONE_ANNULLATA'
                WHEN 'SCADUTA' THEN 'PRENOTAZIONE_SCADUTA'
                ELSE 'CAMBIO_STATO'
            END,
            NEW.codice_prenotazione,
            NEW.id_proiezione,
            JSON_OBJECT(
                'stato_precedente', OLD.stato_prenotazione,
                'stato_nuovo', NEW.stato_prenotazione,
                'posto', CONCAT(NEW.fila, NEW.num_posto)
            )
        );
    END IF;
END //

-- TRIGGER: Cleanup automatico lock alla conferma
CREATE TRIGGER cleanup_lock_conferma
    AFTER UPDATE ON prenotazione
    FOR EACH ROW
BEGIN
    DECLARE v_lock_name VARCHAR(100);
    
    -- Rimuove lock quando prenotazione viene confermata
    IF OLD.stato_prenotazione = 'TEMPORANEA' AND NEW.stato_prenotazione = 'CONFERMATA' THEN
        SET v_lock_name = CONCAT('seat_', NEW.id_proiezione, '_', NEW.num_sala, '_', NEW.fila, '_', NEW.num_posto);
        DELETE FROM distributed_locks WHERE lock_name = v_lock_name;
    END IF;
    
    -- Rimuove lock anche quando prenotazione viene annullata o scade
    IF OLD.stato_prenotazione = 'TEMPORANEA' AND NEW.stato_prenotazione IN ('ANNULLATA', 'SCADUTA') THEN
        SET v_lock_name = CONCAT('seat_', NEW.id_proiezione, '_', NEW.num_sala, '_', NEW.fila, '_', NEW.num_posto);
        DELETE FROM distributed_locks WHERE lock_name = v_lock_name;
    END IF;
END //


-- TRIGGER: Aggiornamento automatico stato proiezioni
CREATE TRIGGER aggiorna_stato_proiezione
    BEFORE UPDATE ON proiezione
    FOR EACH ROW
BEGIN
    -- Aggiorna stato basandosi sull'orario corrente
    IF NOW() >= NEW.data_ora_inizio AND NOW() < NEW.data_ora_fine THEN
        SET NEW.stato_proiezione = 'IN_CORSO';
    ELSEIF NOW() >= NEW.data_ora_fine THEN
        SET NEW.stato_proiezione = 'TERMINATA';
    END IF;
END //

DELIMITER ;