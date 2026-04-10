package csa.ui;

import javax.swing.*;
import java.awt.*;

// small helper methods used across the panels
// Devesh
public class Helpers {

    public static final Color NAVY = new Color(0x1A5276);
    public static final Color LIGHT = new Color(0xF0F0F0);

    public static boolean isValidEmail(String email) {
        if (email == null || email.isEmpty()) return false;
        return email.contains("@") && email.contains(".");
    }

    public static boolean isValidStudentId(String sid) {
        if (sid == null) return false;
        return sid.matches("\\d{9}");
    }

    public static void error(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void success(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}
