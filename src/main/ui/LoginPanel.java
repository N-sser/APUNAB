package main.ui;

import main.util.ThemeManager;
import main.data.DataManager;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JFrame {

    private static final ThemeManager tm = ThemeManager.getInstance();
    // ── Paleta (misma que el dashboard) ───────────────────────────────────────
    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ERROR = new Color(0xE7, 0x4C, 0x3C);

    private final JTextField fieldCode = new JTextField();
    private final JPasswordField fieldPass = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");

    public LoginPanel() {
        setTitle("APUNAB - Login");
        setSize(400, 460);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    void buildUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(tm.getBg());
        root.setBorder(new EmptyBorder(50, 48, 50, 48));

        // Titulo
        JLabel lblTitle = centered("APUNAB", new Font("Dialog", Font.BOLD, 32), C_ORANGE);
        JLabel lblSub = centered("Universidad Autonoma de Bucaramanga",
                new Font("Dialog", Font.PLAIN, 11), tm.getMuted());

        // Campos
        setupField(fieldCode, "Codigo de estudiante", false);
        setupField(fieldPass, "Contrasena", true);

        // Etiqueta de error
        lblError.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblError.setForeground(C_ERROR);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // Botones
        JButton btnLogin = buildButton("Iniciar sesion", true);
        JButton btnRegister = buildButton("Crear cuenta", false);

        btnLogin.addActionListener(e -> attemptLogin());
        btnRegister.addActionListener(e -> openRegister());

        // Enter para enviar el formulario
        KeyAdapter onEnter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    attemptLogin();
            }
        };
        fieldCode.addKeyListener(onEnter);
        fieldPass.addKeyListener(onEnter);

        root.add(lblTitle);
        root.add(Box.createVerticalStrut(4));
        root.add(lblSub);
        root.add(Box.createVerticalStrut(36));
        root.add(fieldCode);
        root.add(Box.createVerticalStrut(12));
        root.add(fieldPass);
        root.add(Box.createVerticalStrut(6));
        root.add(lblError);
        root.add(Box.createVerticalStrut(12));
        root.add(btnLogin);
        root.add(Box.createVerticalStrut(8));
        root.add(btnRegister);

        setContentPane(root);
    }

    // ── Logica ────────────────────────────────────────────────────────────────

    void attemptLogin() {
        String code = fieldCode.getText().trim();
        String raw = new String(fieldPass.getPassword()).trim();

        if (code.isEmpty() || raw.isEmpty()) {
            showError("Porfavor rellene todos los campos.");
            return;
        }

        Student student = DataManager.getInstance().login(code, raw);

        if (student != null) {
            dispose();
            new MainDashboard(student).setVisible(true);
        } else {
            showError("Codigo o contrasena incorrecta.");
            fieldPass.setText("");
        }
    }

    void openRegister() {
        new RegisterPanel(this).setVisible(true);
        setVisible(false);
    }

    void showError(String msg) {
        lblError.setText(msg);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    void setupField(JTextField field, String placeholder, boolean isPassword) {
        field.setFont(new Font("Dialog", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(CENTER_ALIGNMENT);
        field.setBackground(tm.getSurface());
        field.setCaretColor(tm.getText());
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getBorder(), 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        // Logica de placeholder
        if (isPassword) {
            JPasswordField pf = (JPasswordField) field;
            pf.setEchoChar((char) 0); // mostrar placeholder como texto plano
            pf.setText(placeholder);
            pf.setForeground(tm.getMuted());
            pf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(pf.getPassword()).equals(placeholder)) {
                        pf.setText("");
                        pf.setEchoChar('*'); // ocultar contrasena real
                        pf.setForeground(tm.getText());
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (new String(pf.getPassword()).isEmpty()) {
                        pf.setEchoChar((char) 0);
                        pf.setText(placeholder);
                        pf.setForeground(tm.getMuted());
                    }
                }
            });
        } else {
            field.setText(placeholder);
            field.setForeground(tm.getMuted());
            field.addFocusListener(new FocusAdapter() {

                @Override
                public void focusGained(FocusEvent e) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(tm.getText());
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                        field.setForeground(tm.getMuted());
                    }
                }

            });
        }
    }

    JButton buildButton(String text, boolean primary) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color base = primary ? C_ORANGE : tm.getBg();
                g2.setColor(getModel().isRollover() ? base.darker() : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (!primary) {
                    g2.setColor(tm.getBorder());
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Dialog", Font.BOLD, 13));
        btn.setForeground(primary ? Color.WHITE : tm.getText());
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(12, 0, 12, 0));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    JLabel centered(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        return lbl;
    }
}