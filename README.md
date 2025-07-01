# 🎬 Sistema di Gestione Sale Cinematografiche

Progetto sviluppato per il corso di **Basi di Dati – Ingegneria Informatica**.  
Include un database relazionale MySQL e un thin client Java per la gestione delle prenotazioni e delle statistiche mensili.

---

## 📦 Requisiti

- Java 17 o superiore  
- MySQL Server o MariaDB  
- MySQL JDBC Driver (`mysql-connector-java.jar`)  
- File SQL: `claudb.sql` (schema + dati iniziali)

---

## 🗃 Installazione del Database

1. **Avvia il server MySQL**  
   *(Esempio su Linux:)*  
   ```bash
   sudo systemctl start mysql
   mysql -u root -p claudb < claudb.sql

---

## 🚀 Avvio del Thin Client Java (CLI)

  git clone " html del repositori"
  javac -cp .:mysql-connector-java.jar src/*.java
