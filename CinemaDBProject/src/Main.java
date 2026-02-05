import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    // === CONNESSIONE DB ===
    static final String URL = "jdbc:mysql://localhost:3306/cinema_db";
    static final String USER = "root";
    static final String PASSWORD = ""; // se non hai password su XAMPP lascia vuoto

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        int scelta;

        do {
            System.out.println("\n=== CINEMA DB PROJECT ===");
            System.out.println("1) Visualizza FILM");
            System.out.println("2) Visualizza SPETTACOLI");
            System.out.println("3) Inserisci CLIENTE");
            System.out.println("4) Visualizza CLIENTI");
            System.out.println("5) Prenota POSTO (con conferma)");
            System.out.println("6) Visualizza POSTI OCCUPATI per spettacolo");
            System.out.println("0) Esci");
            System.out.print("Scelta: ");

            scelta = leggiIntero(sc);

            switch (scelta) {
                case 1 -> stampaFilm();
                case 2 -> stampaSpettacoli();
                case 3 -> inserisciCliente(sc);
                case 4 -> stampaClienti();
                case 5 -> prenotaPosto(sc);
                case 6 -> visualizzaPostiOccupati(sc);
                case 0 -> System.out.println("Uscita dal programma. Ciao!");
                default -> System.out.println("Scelta non valida.");
            }

        } while (scelta != 0);

        sc.close();
    }

    // =========================
    // 1) VISUALIZZA FILM
    // =========================
    static void stampaFilm() {
        String sql = "SELECT id_film, titolo, durata_min FROM film ORDER BY id_film";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- ELENCO FILM ---");
            while (rs.next()) {
                int id = rs.getInt("id_film");
                String titolo = rs.getString("titolo");
                int durata = rs.getInt("durata_min");
                System.out.println(id + " - " + titolo + " (" + durata + " min)");
            }

        } catch (SQLException e) {
            System.out.println("Errore stampaFilm: " + e.getMessage());
        }
    }

    // =========================
    // 2) VISUALIZZA SPETTACOLI
    // =========================
    static void stampaSpettacoli() {
        String sql =
                "SELECT s.id_spettacolo, f.titolo, s.data_ora, s.prezzo " +
                        "FROM spettacolo s " +
                        "JOIN film f ON s.id_film = f.id_film " +
                        "ORDER BY s.data_ora";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- ELENCO SPETTACOLI ---");
            while (rs.next()) {
                int idSp = rs.getInt("id_spettacolo");
                String titolo = rs.getString("titolo");
                String dataOra = rs.getString("data_ora");
                double prezzo = rs.getDouble("prezzo");
                System.out.println(idSp + ") " + titolo + " | " + dataOra + " | €" + prezzo);
            }

        } catch (SQLException e) {
            System.out.println("Errore stampaSpettacoli: " + e.getMessage());
        }
    }

    // =========================
    // 3) INSERISCI CLIENTE
    // =========================
    static void inserisciCliente(Scanner sc) {

        System.out.println("\n--- INSERIMENTO CLIENTE ---");

        System.out.print("Nome: ");
        String nome = sc.nextLine().trim();

        System.out.print("Cognome: ");
        String cognome = sc.nextLine().trim();

        System.out.print("Email (facoltativa): ");
        String email = sc.nextLine().trim();
        if (email.isEmpty()) email = null;

        if (nome.isEmpty() || cognome.isEmpty()) {
            System.out.println("❌ Nome e cognome obbligatori!");
            return;
        }

        String sql = "INSERT INTO cliente (nome, cognome, email) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nome);
            ps.setString(2, cognome);
            if (email == null) ps.setNull(3, Types.VARCHAR);
            else ps.setString(3, email);

            int rows = ps.executeUpdate();
            System.out.println(rows == 1 ? "✅ Cliente inserito!" : "❌ Inserimento non riuscito.");

        } catch (SQLException e) {
            System.out.println("Errore inserisciCliente: " + e.getMessage());
        }
    }

    // =========================
    // 4) VISUALIZZA CLIENTI
    // =========================
    static void stampaClienti() {
        String sql = "SELECT id_cliente, nome, cognome, email FROM cliente ORDER BY id_cliente";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            System.out.println("\n--- ELENCO CLIENTI ---");
            while (rs.next()) {
                int id = rs.getInt("id_cliente");
                String nome = rs.getString("nome");
                String cognome = rs.getString("cognome");
                String email = rs.getString("email");
                System.out.println(id + ") " + nome + " " + cognome + " | " + (email == null ? "-" : email));
            }

        } catch (SQLException e) {
            System.out.println("Errore stampaClienti: " + e.getMessage());
        }
    }

    // =========================
    // 5) PRENOTA POSTO
    // =========================
    static void prenotaPosto(Scanner sc) {

        System.out.println("\n--- PRENOTA POSTO ---");

        // Mostra spettacoli
        stampaSpettacoli();

        System.out.print("\nInserisci ID spettacolo: ");
        int idSpettacolo = leggiIntero(sc);

        SpettacoloDettaglio det = getDettaglioSpettacolo(idSpettacolo);
        if (det == null) {
            System.out.println("❌ Spettacolo non trovato.");
            return;
        }

        int occupati = contaPostiOccupati(idSpettacolo);
        int disponibili = det.postiTotali - occupati;

        System.out.println("\nHai scelto:");
        System.out.println("🎬 " + det.titolo + " | " + det.dataOra + " | €" + det.prezzo);
        System.out.println("📌 Posti: " + occupati + "/" + det.postiTotali + " occupati | Disponibili: " + disponibili);

        if (disponibili <= 0) {
            System.out.println("❌ Spettacolo SOLD OUT. Nessun posto disponibile.");
            return;
        }

        System.out.print("Confermi questo spettacolo? (s/n): ");
        String conferma = sc.nextLine().trim().toLowerCase();
        if (!conferma.equals("s")) {
            System.out.println("Operazione annullata.");
            return;
        }

        // Mostra clienti
        stampaClienti();

        System.out.print("\nInserisci ID cliente: ");
        int idCliente = leggiIntero(sc);

        ClienteDettaglio cliente = getDettaglioCliente(idCliente);
        if (cliente == null) {
            System.out.println("❌ Cliente non trovato. Inseriscilo prima (opzione 3).");
            return;
        }

        // (Opzionale) mostra posti occupati prima di scegliere
        List<Integer> occupatiLista = listaPostiOccupati(idSpettacolo);
        if (!occupatiLista.isEmpty()) {
            System.out.println("Posti già occupati: " + occupatiLista);
        } else {
            System.out.println("Nessun posto occupato (per ora).");
        }

        System.out.print("Inserisci numero posto (1 - " + det.postiTotali + "): ");
        int numeroPosto = leggiIntero(sc);

        if (numeroPosto < 1 || numeroPosto > det.postiTotali) {
            System.out.println("❌ Numero posto non valido.");
            return;
        }

        if (postoGiaPrenotato(idSpettacolo, numeroPosto)) {
            System.out.println("❌ Posto " + numeroPosto + " già prenotato per questo spettacolo.");
            return;
        }

        boolean ok = salvaPrenotazioneConPosto(idCliente, idSpettacolo, numeroPosto);

        if (ok) {
            System.out.println("\n✅ PRENOTAZIONE CONFERMATA");
            System.out.println("Cliente: " + cliente.nome + " " + cliente.cognome);
            System.out.println("Film: " + det.titolo);
            System.out.println("Data/Ora: " + det.dataOra);
            System.out.println("Posto: " + numeroPosto);
            System.out.println("Prezzo: €" + det.prezzo);
        } else {
            System.out.println("❌ Prenotazione non riuscita.");
        }
    }

    // =========================
    // 6) VISUALIZZA POSTI OCCUPATI
    // =========================
    static void visualizzaPostiOccupati(Scanner sc) {

        System.out.println("\n--- POSTI OCCUPATI PER SPETTACOLO ---");
        stampaSpettacoli();

        System.out.print("\nInserisci ID spettacolo: ");
        int idSpettacolo = leggiIntero(sc);

        SpettacoloDettaglio det = getDettaglioSpettacolo(idSpettacolo);
        if (det == null) {
            System.out.println("❌ Spettacolo non trovato.");
            return;
        }

        List<Integer> posti = listaPostiOccupati(idSpettacolo);
        int occupati = posti.size();
        int disponibili = det.postiTotali - occupati;

        System.out.println("\n🎬 " + det.titolo + " | " + det.dataOra + " | €" + det.prezzo);
        System.out.println("📌 Occupati: " + occupati + "/" + det.postiTotali + " | Disponibili: " + disponibili);

        if (posti.isEmpty()) {
            System.out.println("Nessun posto occupato.");
        } else {
            System.out.println("Posti occupati: " + posti);
        }
    }

    // =========================
    // METODI SUPPORTO DB
    // =========================

    private static SpettacoloDettaglio getDettaglioSpettacolo(int idSpettacolo) {
        // Non usiamo nome_sala per evitare errori di colonna: prendiamo solo posti_totali
        String sql =
                "SELECT f.titolo, s.data_ora, s.prezzo, sa.posti_totali " +
                        "FROM spettacolo s " +
                        "JOIN film f ON s.id_film = f.id_film " +
                        "JOIN sala sa ON s.id_sala = sa.id_sala " +
                        "WHERE s.id_spettacolo = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSpettacolo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    SpettacoloDettaglio det = new SpettacoloDettaglio();
                    det.titolo = rs.getString("titolo");
                    det.dataOra = rs.getString("data_ora");
                    det.prezzo = rs.getDouble("prezzo");
                    det.postiTotali = rs.getInt("posti_totali");
                    return det;
                }
            }

        } catch (SQLException e) {
            System.out.println("Errore getDettaglioSpettacolo: " + e.getMessage());
        }

        return null;
    }

    private static ClienteDettaglio getDettaglioCliente(int idCliente) {
        String sql = "SELECT nome, cognome FROM cliente WHERE id_cliente = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idCliente);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    ClienteDettaglio c = new ClienteDettaglio();
                    c.nome = rs.getString("nome");
                    c.cognome = rs.getString("cognome");
                    return c;
                }
            }

        } catch (SQLException e) {
            System.out.println("Errore getDettaglioCliente: " + e.getMessage());
        }

        return null;
    }

    private static List<Integer> listaPostiOccupati(int idSpettacolo) {
        String sql =
                "SELECT pp.numero_posto " +
                        "FROM posto_prenotato pp " +
                        "JOIN prenotazione p ON pp.id_prenotazione = p.id_prenotazione " +
                        "WHERE p.id_spettacolo = ? " +
                        "ORDER BY pp.numero_posto";

        List<Integer> out = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSpettacolo);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getInt("numero_posto"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Errore listaPostiOccupati: " + e.getMessage());
        }

        return out;
    }

    private static int contaPostiOccupati(int idSpettacolo) {
        String sql =
                "SELECT COUNT(*) AS tot " +
                        "FROM posto_prenotato pp " +
                        "JOIN prenotazione p ON pp.id_prenotazione = p.id_prenotazione " +
                        "WHERE p.id_spettacolo = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSpettacolo);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("tot");
            }

        } catch (SQLException e) {
            System.out.println("Errore contaPostiOccupati: " + e.getMessage());
        }

        return 0;
    }

    private static boolean postoGiaPrenotato(int idSpettacolo, int numeroPosto) {
        String sql =
                "SELECT 1 " +
                        "FROM posto_prenotato pp " +
                        "JOIN prenotazione p ON pp.id_prenotazione = p.id_prenotazione " +
                        "WHERE p.id_spettacolo = ? AND pp.numero_posto = ?";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idSpettacolo);
            ps.setInt(2, numeroPosto);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            System.out.println("Errore postoGiaPrenotato: " + e.getMessage());
            return true; // per sicurezza
        }
    }

    private static boolean salvaPrenotazioneConPosto(int idCliente, int idSpettacolo, int numeroPosto) {

        String sqlPrenotazione = "INSERT INTO prenotazione (id_cliente, id_spettacolo) VALUES (?, ?)";
        String sqlPosto = "INSERT INTO posto_prenotato (id_prenotazione, numero_posto) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            conn.setAutoCommit(false);

            int idPrenotazione;

            try (PreparedStatement ps = conn.prepareStatement(sqlPrenotazione, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCliente);
                ps.setInt(2, idSpettacolo);
                ps.executeUpdate();

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        conn.rollback();
                        return false;
                    }
                    idPrenotazione = keys.getInt(1);
                }
            }

            try (PreparedStatement ps2 = conn.prepareStatement(sqlPosto)) {
                ps2.setInt(1, idPrenotazione);
                ps2.setInt(2, numeroPosto);
                ps2.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            System.out.println("Errore salvaPrenotazioneConPosto: " + e.getMessage());
            return false;
        }
    }

    // =========================
    // UTILITY INPUT
    // =========================
    private static int leggiIntero(Scanner sc) {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("Inserisci un numero valido: ");
            }
        }
    }

    // =========================
    // CLASSI DI SUPPORTO
    // =========================
    private static class SpettacoloDettaglio {
        String titolo;
        String dataOra;
        double prezzo;
        int postiTotali;
    }

    private static class ClienteDettaglio {
        String nome;
        String cognome;
    }
}
