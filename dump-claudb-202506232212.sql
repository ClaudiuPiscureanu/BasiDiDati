/*M!999999\- enable the sandbox mode */ 
-- MariaDB dump 10.19  Distrib 10.11.11-MariaDB, for Linux (x86_64)
--
-- Host: localhost    Database: claudb
-- ------------------------------------------------------
-- Server version	10.11.11-MariaDB

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `cinema`
--

DROP TABLE IF EXISTS `cinema`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `cinema` (
  `nome_cinema` varchar(45) NOT NULL,
  `indirizzo` varchar(50) NOT NULL,
  `orario_apertura` time NOT NULL,
  PRIMARY KEY (`nome_cinema`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `distributed_locks`
--

DROP TABLE IF EXISTS `distributed_locks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `distributed_locks` (
  `lock_name` varchar(100) NOT NULL,
  `acquired_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `expires_at` timestamp NOT NULL,
  `session_id` varchar(50) NOT NULL,
  PRIMARY KEY (`lock_name`),
  KEY `idx_expires` (`expires_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `film`
--

DROP TABLE IF EXISTS `film`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `film` (
  `titolo_film` varchar(128) NOT NULL,
  `durata_minuti` tinyint(3) unsigned NOT NULL CHECK (`durata_minuti` > 0),
  `casa_cinematografica` varchar(100) DEFAULT NULL,
  `cast_attori` text DEFAULT NULL,
  PRIMARY KEY (`titolo_film`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `log_operazioni`
--

DROP TABLE IF EXISTS `log_operazioni`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `log_operazioni` (
  `id_log` bigint(20) unsigned NOT NULL AUTO_INCREMENT,
  `operazione` enum('PRENOTAZIONE_CREATA','PRENOTAZIONE_CONFERMATA','PRENOTAZIONE_ANNULLATA','PRENOTAZIONE_SCADUTA') NOT NULL,
  `codice_prenotazione` varchar(20) DEFAULT NULL,
  `id_proiezione` smallint(5) unsigned DEFAULT NULL,
  `dettagli` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`dettagli`)),
  `timestamp_operazione` timestamp NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`id_log`),
  KEY `idx_timestamp` (`timestamp_operazione`),
  KEY `idx_prenotazione` (`codice_prenotazione`),
  KEY `log_operazioni_proiezione_FK` (`id_proiezione`),
  CONSTRAINT `log_operazioni_prenotazione_FK` FOREIGN KEY (`codice_prenotazione`) REFERENCES `prenotazione` (`codice_prenotazione`),
  CONSTRAINT `log_operazioni_proiezione_FK` FOREIGN KEY (`id_proiezione`) REFERENCES `proiezione` (`id_proiezione`)
) ENGINE=InnoDB AUTO_INCREMENT=43 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `posto`
--

DROP TABLE IF EXISTS `posto`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `posto` (
  `num_sala` tinyint(3) unsigned NOT NULL,
  `fila` char(1) NOT NULL,
  `num_posto` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY (`num_sala`,`fila`,`num_posto`),
  CONSTRAINT `posto_ibfk_1` FOREIGN KEY (`num_sala`) REFERENCES `sala` (`num_sala`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prenotazione`
--

DROP TABLE IF EXISTS `prenotazione`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `prenotazione` (
  `codice_prenotazione` varchar(20) NOT NULL,
  `num_sala` tinyint(3) unsigned NOT NULL,
  `fila` char(1) NOT NULL,
  `num_posto` tinyint(3) unsigned NOT NULL,
  `id_proiezione` smallint(5) unsigned NOT NULL,
  `stato_prenotazione` enum('TEMPORANEA','CONFERMATA','ANNULLATA','SCADUTA') DEFAULT 'TEMPORANEA',
  `timestamp_creazione` datetime NOT NULL DEFAULT current_timestamp(),
  `ticket_pag` varchar(50) DEFAULT NULL,
  `timestamp_conferma` datetime DEFAULT NULL,
  `timestamp_scadenza` datetime NOT NULL,
  PRIMARY KEY (`codice_prenotazione`),
  UNIQUE KEY `uk_proiezione_posto` (`id_proiezione`,`num_sala`,`fila`,`num_posto`),
  KEY `num_sala` (`num_sala`,`fila`,`num_posto`),
  KEY `idx_stato_scadenza` (`stato_prenotazione`,`timestamp_scadenza`),
  KEY `idx_proiezione` (`id_proiezione`),
  KEY `idx_timestamp_creazione` (`timestamp_creazione`),
  CONSTRAINT `prenotazione_ibfk_1` FOREIGN KEY (`id_proiezione`) REFERENCES `proiezione` (`id_proiezione`),
  CONSTRAINT `prenotazione_ibfk_2` FOREIGN KEY (`num_sala`, `fila`, `num_posto`) REFERENCES `posto` (`num_sala`, `fila`, `num_posto`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER log_cambio_stato_prenotazione
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
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER cleanup_lock_conferma
    AFTER UPDATE ON prenotazione
    FOR EACH ROW
BEGIN
    DECLARE v_lock_name VARCHAR(100);
    
    
    IF OLD.stato_prenotazione = 'TEMPORANEA' AND NEW.stato_prenotazione = 'CONFERMATA' THEN
        SET v_lock_name = CONCAT('seat_', NEW.id_proiezione, '_', NEW.num_sala, '_', NEW.fila, '_', NEW.num_posto);
        DELETE FROM distributed_locks WHERE lock_name = v_lock_name;
    END IF;
    
    
    IF OLD.stato_prenotazione = 'TEMPORANEA' AND NEW.stato_prenotazione IN ('ANNULLATA', 'SCADUTA') THEN
        SET v_lock_name = CONCAT('seat_', NEW.id_proiezione, '_', NEW.num_sala, '_', NEW.fila, '_', NEW.num_posto);
        DELETE FROM distributed_locks WHERE lock_name = v_lock_name;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `proiezione`
--

DROP TABLE IF EXISTS `proiezione`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `proiezione` (
  `id_proiezione` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `titolo_film` varchar(128) NOT NULL,
  `num_sala` tinyint(3) unsigned NOT NULL,
  `prezzo` decimal(5,2) NOT NULL,
  `data_ora_inizio` datetime NOT NULL,
  `data_ora_fine` datetime NOT NULL,
  `stato_proiezione` enum('PROGRAMMATA','IN_CORSO','TERMINATA') DEFAULT 'PROGRAMMATA',
  PRIMARY KEY (`id_proiezione`),
  KEY `titolo_film` (`titolo_film`),
  KEY `idx_data_inizio` (`data_ora_inizio`),
  KEY `idx_sala_data` (`num_sala`,`data_ora_inizio`),
  CONSTRAINT `proiezione_ibfk_1` FOREIGN KEY (`titolo_film`) REFERENCES `film` (`titolo_film`) ON DELETE CASCADE,
  CONSTRAINT `proiezione_ibfk_2` FOREIGN KEY (`num_sala`) REFERENCES `sala` (`num_sala`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER aggiorna_stato_proiezione
    BEFORE UPDATE ON proiezione
    FOR EACH ROW
BEGIN
    
    IF NOW() >= NEW.data_ora_inizio AND NOW() < NEW.data_ora_fine THEN
        SET NEW.stato_proiezione = 'IN_CORSO';
    ELSEIF NOW() >= NEW.data_ora_fine THEN
        SET NEW.stato_proiezione = 'TERMINATA';
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `report`
--

DROP TABLE IF EXISTS `report`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `report` (
  `anno` year(4) NOT NULL,
  `mese` tinyint(4) NOT NULL,
  `num_sala` tinyint(3) unsigned NOT NULL,
  `perc_annullati` decimal(10,2) NOT NULL,
  `perc_confermati` decimal(10,2) NOT NULL,
  PRIMARY KEY (`anno`,`mese`,`num_sala`),
  KEY `num_sala` (`num_sala`),
  CONSTRAINT `report_ibfk_1` FOREIGN KEY (`num_sala`) REFERENCES `sala` (`num_sala`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `sala`
--

DROP TABLE IF EXISTS `sala`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `sala` (
  `num_sala` tinyint(3) unsigned NOT NULL AUTO_INCREMENT,
  `nome_sala` varchar(45) NOT NULL,
  `capacita` tinyint(3) unsigned NOT NULL,
  PRIMARY KEY (`num_sala`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `utente`
--

DROP TABLE IF EXISTS `utente`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8mb4 */;
CREATE TABLE `utente` (
  `username` varchar(50) NOT NULL,
  `password` varchar(50) NOT NULL,
  `ruolo` enum('proprietario','personale','cliente','guest') NOT NULL,
  PRIMARY KEY (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary table structure for view `vista_posti_disponibili`
--

DROP TABLE IF EXISTS `vista_posti_disponibili`;
/*!50001 DROP VIEW IF EXISTS `vista_posti_disponibili`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8mb4;
/*!50001 CREATE VIEW `vista_posti_disponibili` AS SELECT
 1 AS `id_proiezione`,
  1 AS `titolo_film`,
  1 AS `data_ora_inizio`,
  1 AS `prezzo`,
  1 AS `num_sala`,
  1 AS `fila`,
  1 AS `num_posto`,
  1 AS `stato_posto` */;
SET character_set_client = @saved_cs_client;

--
-- Temporary table structure for view `vista_report_mensile`
--

DROP TABLE IF EXISTS `vista_report_mensile`;
/*!50001 DROP VIEW IF EXISTS `vista_report_mensile`*/;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8mb4;
/*!50001 CREATE VIEW `vista_report_mensile` AS SELECT
 1 AS `num_sala`,
  1 AS `nome_sala`,
  1 AS `anno`,
  1 AS `mese`,
  1 AS `prenotazioni_confermate`,
  1 AS `prenotazioni_annullate`,
  1 AS `incasso_totale` */;
SET character_set_client = @saved_cs_client;

--
-- Dumping events for database 'claudb'
--
/*!50106 SET @save_time_zone= @@TIME_ZONE */ ;
/*!50106 DROP EVENT IF EXISTS `cleanup_log_vecchi` */;
DELIMITER ;;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;;
/*!50003 SET character_set_client  = utf8mb4 */ ;;
/*!50003 SET character_set_results = utf8mb4 */ ;;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;;
/*!50003 SET @saved_time_zone      = @@time_zone */ ;;
/*!50003 SET time_zone             = 'SYSTEM' */ ;;
/*!50106 CREATE*/ /*!50117 DEFINER=`root`@`localhost`*/ /*!50106 EVENT `cleanup_log_vecchi` ON SCHEDULE EVERY 1 MONTH STARTS '2025-06-12 11:31:57' ON COMPLETION NOT PRESERVE ENABLE DO DELETE FROM log_operazioni WHERE timestamp_operazione < DATE_SUB(NOW(), INTERVAL 12 MONTH) */ ;;
/*!50003 SET time_zone             = @saved_time_zone */ ;;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;;
/*!50003 SET character_set_client  = @saved_cs_client */ ;;
/*!50003 SET character_set_results = @saved_cs_results */ ;;
/*!50003 SET collation_connection  = @saved_col_connection */ ;;
/*!50106 DROP EVENT IF EXISTS `cleanup_prenotazioni_scadute` */;;
DELIMITER ;;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;;
/*!50003 SET character_set_client  = utf8mb4 */ ;;
/*!50003 SET character_set_results = utf8mb4 */ ;;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;;
/*!50003 SET @saved_time_zone      = @@time_zone */ ;;
/*!50003 SET time_zone             = 'SYSTEM' */ ;;
/*!50106 CREATE*/ /*!50117 DEFINER=`root`@`localhost`*/ /*!50106 EVENT `cleanup_prenotazioni_scadute` ON SCHEDULE EVERY 5 MINUTE STARTS '2025-06-12 11:31:57' ON COMPLETION NOT PRESERVE ENABLE DO CALL CleanupPrenotazioniScadute() */ ;;
/*!50003 SET time_zone             = @saved_time_zone */ ;;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;;
/*!50003 SET character_set_client  = @saved_cs_client */ ;;
/*!50003 SET character_set_results = @saved_cs_results */ ;;
/*!50003 SET collation_connection  = @saved_col_connection */ ;;
DELIMITER ;
/*!50106 SET TIME_ZONE= @save_time_zone */ ;

--
-- Dumping routines for database 'claudb'
--
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 DROP PROCEDURE IF EXISTS `AnnullaPrenotazione` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `AnnullaPrenotazione`(
    IN p_codice_prenotazione VARCHAR(20),
    OUT p_risultato INT 
)
proc_exit: BEGIN
    DECLARE v_stato_attuale ENUM('TEMPORANEA', 'CONFERMATA', 'ANNULLATA', 'SCADUTA');
    DECLARE v_data_inizio DATETIME;
    DECLARE v_id_proiezione SMALLINT UNSIGNED;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_risultato = -2; 
    END;

    START TRANSACTION;

    
    SELECT p.stato_prenotazione, pr.data_ora_inizio, p.id_proiezione
    INTO v_stato_attuale, v_data_inizio, v_id_proiezione
    FROM prenotazione p
    JOIN proiezione pr ON p.id_proiezione = pr.id_proiezione
    WHERE p.codice_prenotazione = p_codice_prenotazione
    FOR UPDATE;

    IF v_stato_attuale IS NULL THEN
        SET p_risultato = 0; 
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    
    IF NOW() > DATE_SUB(v_data_inizio, INTERVAL 30 MINUTE) THEN
        SET p_risultato = -1; 
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    
    IF v_stato_attuale = ('TEMPORANEA' , 'CONFERMATA') THEN
        UPDATE prenotazione
        SET stato_prenotazione = 'ANNULLATA'
        WHERE codice_prenotazione = p_codice_prenotazione;

        
        INSERT INTO log_operazioni (operazione, codice_prenotazione, id_proiezione)
        VALUES ('PRENOTAZIONE_ANNULLATA', p_codice_prenotazione, v_id_proiezione);
       END IF;

        SET p_risultato = 1; 
        COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CleanupPrenotazioniScadute` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CleanupPrenotazioniScadute`()
proc_exit: BEGIN
    DECLARE v_count INT DEFAULT 0;

    START TRANSACTION;
    
    UPDATE prenotazione
    SET stato_prenotazione = 'SCADUTA'
    WHERE stato_prenotazione = 'TEMPORANEA' 
        AND timestamp_scadenza < NOW();

    SET v_count = ROW_COUNT();

    
    IF v_count > 0 THEN
        INSERT INTO log_operazioni (operazione, id_proiezione, dettagli)
        VALUES ('PRENOTAZIONE_SCADUTA', NULL, JSON_OBJECT('count', v_count));
    END IF;
    
    
    DELETE FROM distributed_locks 
    WHERE expires_at < NOW();

    COMMIT;

END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,STRICT_ALL_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,TRADITIONAL,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 DROP PROCEDURE IF EXISTS `ConfermaPrenotazione` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `ConfermaPrenotazione`(
    IN p_codice_prenotazione VARCHAR(20),
    IN p_ticket_pag VARCHAR(50),
    OUT p_risultato INT 
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
        SET p_risultato = -2; 
    END;

    START TRANSACTION;
    
    
    SELECT stato_prenotazione, timestamp_scadenza, id_proiezione, num_sala, fila, num_posto
    INTO v_stato_attuale, v_timestamp_scadenza, v_id_proiezione, v_num_sala, v_fila, v_num_posto
    FROM prenotazione
    WHERE codice_prenotazione = p_codice_prenotazione
    FOR UPDATE;

    IF v_stato_attuale IS NULL THEN
        SET p_risultato = 0; 
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    
    IF v_stato_attuale != 'TEMPORANEA' THEN
        SET p_risultato = -1; 
        ROLLBACK;
        LEAVE proc_exit;    
    END IF;

    IF NOW() > v_timestamp_scadenza THEN
        UPDATE prenotazione  
        SET stato_prenotazione = 'SCADUTA'
        WHERE codice_prenotazione = p_codice_prenotazione;
        SET p_risultato = -1; 
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    
    UPDATE prenotazione
    SET stato_prenotazione = 'CONFERMATA',
        timestamp_conferma = NOW(),
        ticket_pag = p_ticket_pag
        WHERE codice_prenotazione = p_codice_prenotazione;  
    
    
    SET v_lock_name = CONCAT('seat_', v_id_proiezione, '_', v_num_sala, '_', v_fila, '_', v_num_posto);
    DELETE FROM distributed_locks 
    WHERE lock_name = v_lock_name;

    
    INSERT INTO log_operazioni (operazione, codice_prenotazione, id_proiezione, dettagli)
        VALUES ('PRENOTAZIONE_CONFERMATA', p_codice_prenotazione, v_id_proiezione, JSON_OBJECT('posto', CONCAT(v_fila, v_num_posto)));
        
    SET p_risultato = 1; 
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CreaPrenotazioneTemporanea` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CreaPrenotazioneTemporanea`(IN p_id_proiezione smallint unsigned, IN p_fila char,
                                                                  IN p_num_posto tinyint unsigned,
                                                                  OUT p_codice_prenotazione varchar(20),
                                                                  OUT p_risultato int)
proc_exit: BEGIN
    -- Costanti per i timeout
    DECLARE LOCK_TIMEOUT_MINUTES INT DEFAULT 15;          -- Durata del lock distribuito
    DECLARE PRENOTAZIONE_TIMEOUT_MINUTES INT DEFAULT 10;  -- Tempo per confermare la prenotazione
    DECLARE CONFERMA_TIMEOUT_MINUTES INT DEFAULT 20;      -- Tempo massimo per la conferma finale
    
    -- Variabili per i dati della proiezione
    DECLARE v_num_sala TINYINT UNSIGNED;     -- Numero della sala della proiezione
    DECLARE v_data_inizio DATETIME;          -- Data e ora di inizio proiezione
    DECLARE v_prezzo DECIMAL(5,2);           -- Prezzo del biglietto
    
    -- Variabili per la gestione del lock
    DECLARE v_lock_name VARCHAR(100);        -- Nome del lock distribuito
    DECLARE v_session_id VARCHAR(50);        -- ID univoco della sessione
    DECLARE v_lock_acquired BOOLEAN DEFAULT FALSE;  -- Flag per tracciare l'acquisizione del lock
    
    -- Variabili per le verifiche
    DECLARE v_count_existing INT DEFAULT 0;   -- Contatore per le query di verifica

    -- Gestione degli errori
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        -- Rilascia il lock se era stato acquisito
        IF v_lock_acquired THEN
            DELETE FROM distributed_locks 
            WHERE lock_name = v_lock_name AND session_id = v_session_id;
        END IF;
        SET p_risultato = -2;  -- Errore di sistema
    END;

    -- Imposta il livello di isolamento massimo per evitare race conditions
    SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE;

    -- Genera ID sessione univoco combinando ID connessione, timestamp e numero casuale
    SET v_session_id = CONCAT(CONNECTION_ID(), '_', UNIX_TIMESTAMP(),'_', RAND());

    -- Genera codice prenotazione univoco
    -- Formato: RES[Anno][Mese][ID Proiezione][Fila][Numero Posto][Numero Casuale]
    SET p_codice_prenotazione = CONCAT('RES',
        YEAR(NOW()),
        MONTH(NOW()),
        LPAD(p_id_proiezione,4,'0'), 
        UPPER(p_fila),
        LPAD(p_num_posto,2,'0'),
        LPAD(floor(RAND() * 1000),3,'0'));

    -- Inizia la transazione
    START TRANSACTION;

    -- Verifica esistenza proiezione e recupera dati necessari
    -- Controlla anche che la proiezione sia futura e programmata
    SELECT num_sala, data_ora_inizio, prezzo
    INTO v_num_sala, v_data_inizio, v_prezzo
    FROM proiezione
    WHERE id_proiezione = p_id_proiezione
        AND data_ora_inizio > NOW()
        AND stato_proiezione = 'PROGRAMMATA';

    -- Se la proiezione non esiste o non è valida, annulla
    IF v_num_sala IS NULL THEN
        SET p_risultato = -1; 
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Crea il nome del lock specifico per questo posto
    SET v_lock_name = CONCAT('seat_',
        p_id_proiezione, '_', 
        v_num_sala, '_', 
        p_fila, '_', 
        p_num_posto);

    -- Tenta di acquisire il lock distribuito
    -- Se il lock esiste già, l'inserimento fallisce per vincolo UNIQUE
    INSERT INTO distributed_locks (lock_name, expires_at, session_id)
    VALUES (v_lock_name, 
        DATE_ADD(NOW(), INTERVAL LOCK_TIMEOUT_MINUTES MINUTE), 
        v_session_id)
    ON DUPLICATE KEY UPDATE lock_name = lock_name;

    -- Se ROW_COUNT = 0, significa che il lock esisteva già
    IF ROW_COUNT() = 0 THEN
        SET p_risultato = 0; -- Posto in fase di prenotazione da altro utente
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    SET v_lock_acquired = TRUE;

    -- Verifica che il posto non sia già prenotato
    -- Considera sia prenotazioni temporanee che confermate
    SELECT COUNT(*) INTO v_count_existing
    FROM prenotazione
    WHERE id_proiezione = p_id_proiezione
        AND num_sala = v_num_sala
        AND fila = p_fila
        AND num_posto = p_num_posto
        AND stato_prenotazione IN ('TEMPORANEA', 'CONFERMATA');

    -- Se esiste già una prenotazione, annulla
    IF v_count_existing > 0 THEN
        SET p_risultato = 0; -- Posto già prenotato
        DELETE FROM distributed_locks 
        WHERE lock_name = v_lock_name 
        AND session_id = v_session_id;
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Verifica che il posto esista fisicamente nella sala
    SELECT COUNT(*) INTO v_count_existing
    FROM posto
    WHERE num_sala = v_num_sala 
    AND fila = p_fila 
    AND num_posto = p_num_posto;

    -- Se il posto non esiste nella configurazione della sala, annulla
    IF v_count_existing = 0 THEN
        SET p_risultato = -1; -- Posto non esistente
        DELETE FROM distributed_locks 
        WHERE lock_name = v_lock_name 
        AND session_id = v_session_id;
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- Crea la prenotazione temporanea
    INSERT INTO prenotazione(
        codice_prenotazione,
        id_proiezione,
        num_sala,
        fila,
        num_posto,
        stato_prenotazione,
        timestamp_creazione,
        timestamp_scadenza       -- Scadenza per confermare la prenotazione
           ) VALUES (
        p_codice_prenotazione,
        p_id_proiezione,
        v_num_sala,
        p_fila,
        p_num_posto,
        'TEMPORANEA',
        NOW(),
        DATE_ADD(NOW(), INTERVAL PRENOTAZIONE_TIMEOUT_MINUTES MINUTE)
          );

    -- Registra l'operazione nel log
    INSERT INTO log_operazioni (
        operazione, 
        codice_prenotazione, 
        id_proiezione, 
        dettagli
    )
    VALUES (
        'PRENOTAZIONE_CREATA', 
        p_codice_prenotazione, 
        p_id_proiezione, 
        JSON_OBJECT(
            'posto', CONCAT(p_fila, p_num_posto), 
            'prezzo', v_prezzo
        )
    );

    SET p_risultato = 1; -- Operazione completata con successo
    COMMIT;
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 DROP PROCEDURE IF EXISTS `CreaPrenotazioneTemporaneaDebug` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `CreaPrenotazioneTemporaneaDebug`(IN p_id_proiezione smallint unsigned,
                                                                       IN p_fila char, IN p_num_posto tinyint unsigned,
                                                                       OUT p_codice_prenotazione varchar(20),
                                                                       OUT p_risultato int, OUT p_debug_info text)
proc_exit: BEGIN

    DECLARE v_num_sala TINYINT UNSIGNED;
    DECLARE v_data_inizio DATETIME;
    DECLARE v_prezzo DECIMAL(5,2);
    DECLARE v_lock_name VARCHAR(100);
    DECLARE v_session_id VARCHAR(50);
    DECLARE v_count_existing INT DEFAULT 0;
    DECLARE v_lock_acquired BOOLEAN DEFAULT FALSE;
    DECLARE v_debug_step VARCHAR(50) DEFAULT 'INIT';
    DECLARE v_error_msg TEXT DEFAULT '';

    -- Handler per errori SQL con debug dettagliato
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        GET DIAGNOSTICS CONDITION 1 
            @error_code = RETURNED_SQLSTATE,
            @error_msg = MESSAGE_TEXT;
        
        SET p_debug_info = CONCAT(
            'ERRORE SQL al step: ', v_debug_step,
            ' | SQLSTATE: ', @error_code,
            ' | Messaggio: ', @error_msg,
            ' | Session ID: ', IFNULL(v_session_id, 'NULL'),
            ' | Lock Name: ', IFNULL(v_lock_name, 'NULL'),
            ' | Parametri: ID=', p_id_proiezione, ', Fila=', p_fila, ', Posto=', p_num_posto
        );
        
        ROLLBACK;
        IF v_lock_acquired THEN
            DELETE FROM distributed_locks WHERE lock_name = v_lock_name AND session_id = v_session_id;
        END IF;
        SET p_risultato = -2;
    END;

    -- STEP 1: Inizializzazione
    SET v_debug_step = 'INITIALIZATION';
    SET v_session_id = CONCAT(CONNECTION_ID(), '_', UNIX_TIMESTAMP(),'_', RAND());
    SET p_codice_prenotazione = CONCAT('RES',YEAR(NOW()),MONTH(NOW()),LPAD(p_id_proiezione,4,'0'), UPPER(p_fila),LPAD(p_num_posto,2,'0'),LPAD(FLOOR(RAND() * 1000),3,'0'));
    
    SET p_debug_info = CONCAT('Step: ', v_debug_step, ' | Session ID: ', v_session_id, ' | Codice: ', p_codice_prenotazione);

    -- STEP 2: Inizio transazione
    SET v_debug_step = 'START_TRANSACTION';
    START TRANSACTION;
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' - OK');

    -- STEP 3: Verifica proiezione
    SET v_debug_step = 'CHECK_PROJECTION';
    SELECT num_sala, data_ora_inizio, prezzo
    INTO v_num_sala, v_data_inizio, v_prezzo
    FROM proiezione 
    WHERE id_proiezione = p_id_proiezione
        AND data_ora_inizio > NOW()
        AND stato_proiezione = 'PROGRAMMATA';
    
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, 
        ' | Sala trovata: ', IFNULL(v_num_sala, 'NULL'),
        ' | Data inizio: ', IFNULL(v_data_inizio, 'NULL'),
        ' | Prezzo: ', IFNULL(v_prezzo, 'NULL'));
    
    IF v_num_sala IS NULL THEN
        SET p_risultato = -1;
        SET p_debug_info = CONCAT(p_debug_info, ' | ERRORE: Proiezione non trovata o non valida');
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- STEP 4: Creazione lock
    SET v_debug_step = 'CREATE_LOCK';
    SET v_lock_name = CONCAT('seat_',p_id_proiezione, '_', v_num_sala, '_', p_fila, '_', p_num_posto);
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' | Lock name: ', v_lock_name);
    
    -- STEP 5: Inserimento lock
    SET v_debug_step = 'INSERT_LOCK';
    INSERT INTO distributed_locks (lock_name, expires_at, session_id) 
    VALUES (v_lock_name, DATE_ADD(NOW(), INTERVAL 15 MINUTE), v_session_id)
    ON DUPLICATE KEY UPDATE lock_name = lock_name;

    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' | ROW_COUNT: ', ROW_COUNT());

    IF ROW_COUNT() = 0 THEN
        SET p_risultato = 0;
        SET p_debug_info = CONCAT(p_debug_info, ' | ERRORE: Lock già esistente');
        ROLLBACK;
        LEAVE proc_exit;
    END IF;
    SET v_lock_acquired = TRUE;

    -- STEP 6: Verifica prenotazioni esistenti
    SET v_debug_step = 'CHECK_EXISTING_RESERVATIONS';
    SELECT COUNT(*) INTO v_count_existing
    FROM prenotazione
    WHERE id_proiezione = p_id_proiezione
        AND num_sala = v_num_sala
        AND fila = p_fila
        AND num_posto = p_num_posto
        AND stato_prenotazione IN ('TEMPORANEA', 'CONFERMATA');
    
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' | Prenotazioni esistenti: ', v_count_existing);
    
    IF v_count_existing > 0 THEN
        SET p_risultato = 0;
        SET p_debug_info = CONCAT(p_debug_info, ' | ERRORE: Posto già prenotato');
        DELETE FROM distributed_locks WHERE lock_name = v_lock_name AND session_id = v_session_id;
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- STEP 7: Verifica esistenza posto
    SET v_debug_step = 'CHECK_SEAT_EXISTS';
    SELECT COUNT(*) INTO v_count_existing
    FROM posto
    WHERE num_sala = v_num_sala AND fila = p_fila AND num_posto = p_num_posto;
    
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' | Posto esiste: ', v_count_existing);
    
    IF v_count_existing = 0 THEN
        SET p_risultato = -1;
        SET p_debug_info = CONCAT(p_debug_info, ' | ERRORE: Posto non esiste nella sala');
        DELETE FROM distributed_locks WHERE lock_name = v_lock_name AND session_id = v_session_id;
        ROLLBACK;
        LEAVE proc_exit;
    END IF;

    -- STEP 8: Inserimento prenotazione
    SET v_debug_step = 'INSERT_RESERVATION';
    INSERT INTO prenotazione(
        codice_prenotazione, 
        id_proiezione, 
        num_sala, 
        fila, 
        num_posto,
        stato_prenotazione,
        timestamp_creazione,
        timestamp_scadenza,
        data_ora_prenotazione,
        data_ora_conferma
    ) VALUES (
        p_codice_prenotazione, 
        p_id_proiezione, 
        v_num_sala, 
        p_fila,
        p_num_posto,
        'TEMPORANEA',
        NOW(),
        DATE_ADD(NOW(), INTERVAL 10 MINUTE),
        NOW(),
        DATE_ADD(NOW(), INTERVAL 20 MINUTE)
    );
    
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' - Prenotazione inserita');

    -- STEP 9: Log operazione
    SET v_debug_step = 'INSERT_LOG';
    INSERT INTO log_operazioni (operazione, codice_prenotazione, id_proiezione, dettagli)
    VALUES ('PRENOTAZIONE_CREATA', p_codice_prenotazione, p_id_proiezione, 
            JSON_OBJECT('posto', CONCAT(p_fila, p_num_posto), 'prezzo', v_prezzo));
    
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' - Log inserito');

    -- STEP 10: Commit
    SET v_debug_step = 'COMMIT';
    SET p_risultato = 1;
    COMMIT;
    SET p_debug_info = CONCAT(p_debug_info, ' | Step: ', v_debug_step, ' - SUCCESSO!');
    
END ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'IGNORE_SPACE,STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION' */ ;
/*!50003 DROP PROCEDURE IF EXISTS `login` */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_general_ci */ ;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `login`(
    in v_username varchar(50),
    in v_password varchar(50),
    out p_ruolo int -- identifica il ruolo
    -- out p_risultato int -- 1=successo, -1= password/username errati

)
begin
    declare v_user_role ENUM('proprietario','resto'); -- per ora non ho altre figure a parte il proprietario, ma uso un enum in caso di ampliamento delle funzionalità
    select ruolo from utente
        where username = v_username
        and password  = md5(v_password)
        into v_user_role;

    if v_user_role ='proprietario' then
        set p_ruolo = 1;
    else
        set p_ruolo = 2;
    end if;
end ;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Final view structure for view `vista_posti_disponibili`
--

/*!50001 DROP VIEW IF EXISTS `vista_posti_disponibili`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vista_posti_disponibili` AS select `pr`.`id_proiezione` AS `id_proiezione`,`pr`.`titolo_film` AS `titolo_film`,`pr`.`data_ora_inizio` AS `data_ora_inizio`,`pr`.`prezzo` AS `prezzo`,`p`.`num_sala` AS `num_sala`,`p`.`fila` AS `fila`,`p`.`num_posto` AS `num_posto`,case when `res`.`codice_prenotazione` is null then 'DISPONIBILE' when `res`.`stato_prenotazione` = 'TEMPORANEA' and `res`.`timestamp_scadenza` < current_timestamp() then 'DISPONIBILE' else 'OCCUPATO' end AS `stato_posto` from ((`proiezione` `pr` join `posto` `p` on(`pr`.`num_sala` = `p`.`num_sala`)) left join `prenotazione` `res` on(`pr`.`id_proiezione` = `res`.`id_proiezione` and `p`.`num_sala` = `res`.`num_sala` and `p`.`fila` = `res`.`fila` and `p`.`num_posto` = `res`.`num_posto` and `res`.`stato_prenotazione` in ('TEMPORANEA','CONFERMATA'))) where `pr`.`data_ora_inizio` > current_timestamp() and `pr`.`stato_proiezione` = 'PROGRAMMATA' */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `vista_report_mensile`
--

/*!50001 DROP VIEW IF EXISTS `vista_report_mensile`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_general_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `vista_report_mensile` AS select `pr`.`num_sala` AS `num_sala`,`s`.`nome_sala` AS `nome_sala`,year(`res`.`timestamp_creazione`) AS `anno`,month(`res`.`timestamp_creazione`) AS `mese`,count(case when `res`.`stato_prenotazione` = 'CONFERMATA' then 1 end) AS `prenotazioni_confermate`,count(case when `res`.`stato_prenotazione` = 'ANNULLATA' then 1 end) AS `prenotazioni_annullate`,sum(case when `res`.`stato_prenotazione` = 'CONFERMATA' then `pr`.`prezzo` else 0 end) AS `incasso_totale` from ((`sala` `s` left join `proiezione` `pr` on(`s`.`num_sala` = `pr`.`num_sala`)) left join `prenotazione` `res` on(`pr`.`id_proiezione` = `res`.`id_proiezione`)) where `res`.`timestamp_creazione` is not null group by `pr`.`num_sala`,`s`.`nome_sala`,year(`res`.`timestamp_creazione`),month(`res`.`timestamp_creazione`) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-06-23 22:12:35
