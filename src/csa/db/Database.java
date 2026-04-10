package csa.db;

import java.sql.*;

// handles all database operations
// Kian
public class Database {

    private static Database instance;
    private Connection conn;

    private Database() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:csa.db");
        createTables();
        seedCoordinator();
    }

    public static Database get() throws SQLException {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    private void createTables() throws SQLException {
        Statement st = conn.createStatement();

        st.execute("CREATE TABLE IF NOT EXISTS users (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name TEXT NOT NULL," +
            "student_id TEXT," +
            "email TEXT NOT NULL UNIQUE," +
            "password TEXT NOT NULL," +
            "role TEXT NOT NULL" +
        ")");

        st.execute("CREATE TABLE IF NOT EXISTS applications (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "student_id TEXT NOT NULL," +
            "name TEXT NOT NULL," +
            "email TEXT NOT NULL," +
            "status TEXT DEFAULT 'Pending Provisional Review'," +
            "notes TEXT," +
            "applied_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
        ")");

        st.execute("CREATE TABLE IF NOT EXISTS reports (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "student_id TEXT NOT NULL," +
            "filepath TEXT NOT NULL," +
            "work_term TEXT," +
            "deadline TEXT," +
            "submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
        ")");

        st.execute("CREATE TABLE IF NOT EXISTS evaluations (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "student_id TEXT NOT NULL," +
            "supervisor_email TEXT," +
            "work_term TEXT," +
            "type TEXT," +
            "filepath TEXT," +
            "comments TEXT," +
            "rating INTEGER," +
            "submitted_at DATETIME DEFAULT CURRENT_TIMESTAMP" +
        ")");

        st.close();
    }

    // add a default coordinator account so we can test the app
    private void seedCoordinator() throws SQLException {
        PreparedStatement check = conn.prepareStatement(
            "SELECT id FROM users WHERE email = ?");
        check.setString(1, "coordinator@tmu.ca");
        if (!check.executeQuery().next()) {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, ?)");
            ps.setString(1, "Admin Coordinator");
            ps.setString(2, "coordinator@tmu.ca");
            ps.setString(3, "admin123");
            ps.setString(4, "coordinator");
            ps.executeUpdate();
        }
    }

    public ResultSet login(String email, String password) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM users WHERE email = ? AND password = ?");
        ps.setString(1, email);
        ps.setString(2, password);
        return ps.executeQuery();
    }

    public boolean emailExists(String email) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT id FROM users WHERE email = ?");
        ps.setString(1, email);
        return ps.executeQuery().next();
    }

    public boolean studentIdExists(String sid) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT id FROM users WHERE student_id = ?");
        ps.setString(1, sid);
        return ps.executeQuery().next();
    }

    public void addStudent(String name, String sid, String email, String password) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO users (name, student_id, email, password, role) VALUES (?, ?, ?, ?, 'student')");
        ps.setString(1, name);
        ps.setString(2, sid);
        ps.setString(3, email);
        ps.setString(4, password);
        ps.executeUpdate();
    }

    public void addSupervisor(String name, String email, String password) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO users (name, email, password, role) VALUES (?, ?, ?, 'supervisor')");
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, password);
        ps.executeUpdate();
    }

    public void submitApplication(String sid, String name, String email) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO applications (student_id, name, email) VALUES (?, ?, ?)");
        ps.setString(1, sid);
        ps.setString(2, name);
        ps.setString(3, email);
        ps.executeUpdate();
    }

    public ResultSet getApplication(String sid) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT * FROM applications WHERE student_id = ?");
        ps.setString(1, sid);
        return ps.executeQuery();
    }

    public ResultSet getAllApplications() throws SQLException {
        return conn.createStatement().executeQuery(
            "SELECT * FROM applications ORDER BY applied_at DESC");
    }

    public void updateStatus(String sid, String status, String notes) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "UPDATE applications SET status = ?, notes = ? WHERE student_id = ?");
        ps.setString(1, status);
        ps.setString(2, notes);
        ps.setString(3, sid);
        ps.executeUpdate();
    }

    public String getStatus(String sid) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT status FROM applications WHERE student_id = ?");
        ps.setString(1, sid);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return rs.getString("status");
        return null;
    }

    public boolean hasReport(String sid) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "SELECT id FROM reports WHERE student_id = ?");
        ps.setString(1, sid);
        return ps.executeQuery().next();
    }

    public void saveReport(String sid, String path, String term, String deadline) throws SQLException {
        // delete old one first if resubmitting
        conn.createStatement().execute(
            "DELETE FROM reports WHERE student_id = '" + sid + "'");
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO reports (student_id, filepath, work_term, deadline) VALUES (?, ?, ?, ?)");
        ps.setString(1, sid);
        ps.setString(2, path);
        ps.setString(3, term);
        ps.setString(4, deadline);
        ps.executeUpdate();
    }

    public void saveEvalForm(String sid, String supEmail, String term,
                             String comments, int rating) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO evaluations (student_id, supervisor_email, work_term, type, comments, rating)" +
            " VALUES (?, ?, ?, 'form', ?, ?)");
        ps.setString(1, sid);
        ps.setString(2, supEmail);
        ps.setString(3, term);
        ps.setString(4, comments);
        ps.setInt(5, rating);
        ps.executeUpdate();
    }

    public void saveEvalPdf(String sid, String supEmail, String term, String path) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO evaluations (student_id, supervisor_email, work_term, type, filepath)" +
            " VALUES (?, ?, ?, 'pdf', ?)");
        ps.setString(1, sid);
        ps.setString(2, supEmail);
        ps.setString(3, term);
        ps.setString(4, path);
        ps.executeUpdate();
    }

    // used for the missing submissions tab
    public ResultSet getFinalStudentsWithStatus() throws SQLException {
        String query =
            "SELECT a.student_id, a.name, a.email, " +
            "CASE WHEN r.id IS NOT NULL THEN 'Yes' ELSE 'No' END as has_report, " +
            "CASE WHEN e.id IS NOT NULL THEN 'Yes' ELSE 'No' END as has_eval " +
            "FROM applications a " +
            "LEFT JOIN reports r ON a.student_id = r.student_id " +
            "LEFT JOIN evaluations e ON a.student_id = e.student_id " +
            "WHERE a.status = 'Final Accepted'";
        return conn.createStatement().executeQuery(query);
    }
}
