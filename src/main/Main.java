package main;

import javax.swing.*;
import main.ui.LoginPanel;

public class Main {
    // App entry point
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
    }
}
