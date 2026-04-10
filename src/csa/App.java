package csa;

import csa.db.Database;
import csa.model.User;
import csa.ui.*;

import javax.swing.*;
import java.awt.*;

// Ryan - main window, switches between screens using CardLayout
public class App extends JFrame {

    public static final String SCREEN_LOGIN       = "login";
    public static final String SCREEN_APPLY       = "apply";
    public static final String SCREEN_STUDENT     = "student";
    public static final String SCREEN_COORDINATOR = "coordinator";
    public static final String SCREEN_SUPERVISOR  = "supervisor";

    CardLayout layout;
    JPanel container;

    LoginScreen loginScreen;
    ApplyScreen applyScreen;
    StudentScreen studentScreen;
    CoordinatorScreen coordinatorScreen;
    SupervisorScreen supervisorScreen;

    public App() {
        super("Co-op Support Application - TMU");

        try {
            Database.get(); // init db on startup
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Failed to open database: " + e.getMessage());
            System.exit(1);
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(860, 600);
        setLocationRelativeTo(null);

        layout = new CardLayout();
        container = new JPanel(layout);

        loginScreen       = new LoginScreen(this);
        applyScreen       = new ApplyScreen(this);
        studentScreen     = new StudentScreen(this);
        coordinatorScreen = new CoordinatorScreen(this);
        supervisorScreen  = new SupervisorScreen(this);

        container.add(loginScreen,       SCREEN_LOGIN);
        container.add(applyScreen,       SCREEN_APPLY);
        container.add(studentScreen,     SCREEN_STUDENT);
        container.add(coordinatorScreen, SCREEN_COORDINATOR);
        container.add(supervisorScreen,  SCREEN_SUPERVISOR);

        add(container);
        goTo(SCREEN_LOGIN);
        setVisible(true);
    }

    public void goTo(String screen) {
        layout.show(container, screen);
        if (screen.equals(SCREEN_COORDINATOR)) {
            coordinatorScreen.reload();
        }
    }

    // called after a successful login
    public void loginAs(User user) {
        if (user.getRole().equals("student")) {
            studentScreen.setUser(user);
            studentScreen.reload();
            goTo(SCREEN_STUDENT);
        } else if (user.getRole().equals("coordinator")) {
            goTo(SCREEN_COORDINATOR);
        } else if (user.getRole().equals("supervisor")) {
            supervisorScreen.setUser(user);
            goTo(SCREEN_SUPERVISOR);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App());
    }
}
