# CinemaDBProject-Java-MySQL
Applicazione Java console che utilizza JDBC per collegarsi a un database MySQL e gestire un semplice sistema di prenotazione cinema.

Il progetto permette di visualizzare film e spettacoli, gestire clienti e prenotare posti con controllo della disponibilità.

Progetto didattico sviluppato per consolidare competenze Java Backend e Database Relazionali.

🚀 Funzionalità

Visualizzazione elenco film

Visualizzazione elenco spettacoli con data, ora e prezzo

Inserimento nuovi clienti

Visualizzazione clienti registrati

Prenotazione posto per uno spettacolo

Controllo posti occupati e disponibili

Conferma prenotazione

🛠 Tecnologie utilizzate

Java

JDBC

MySQL (MariaDB tramite XAMPP)

IntelliJ IDEA

Git & GitHub

📂 Struttura del progetto
CinemaDBProject
│
├── src
│   └── Main.java
│
├── lib
│   └── mysql-connector-j-9.6.0.jar
│
└── README.md

⚙️ Requisiti

Java JDK 17 o superiore

XAMPP (con MySQL attivo)

MySQL Connector/J

IntelliJ IDEA

▶️ Come eseguire il progetto

Avviare XAMPP

Avviare Apache e MySQL

Creare il database cinema_db in phpMyAdmin

Importare le tabelle SQL

Aprire il progetto con IntelliJ

Avviare la classe Main.java

🗄️ Struttura database (tabelle)

film

sala

spettacolo

cliente

prenotazione

posto_prenotato

💡 Esempio Menu
1) Visualizza FILM
2) Visualizza SPETTACOLI
3) Inserisci CLIENTE
4) Visualizza CLIENTI
5) Prenota POSTO
6) Visualizza POSTI OCCUPATI
0) Esci

📌 Stato del progetto

Versione funzionante da console.
Possibili miglioramenti futuri:

Suddivisione in classi e DAO

Interfaccia grafica

Gestione utenti

Storico prenotazioni
