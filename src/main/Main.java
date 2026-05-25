package main;

import javax.swing.*;
import main.ui.LoginPanel;

public class Main {
    // Punto de entrada de la aplicacion
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPanel().setVisible(true));
    }
}
