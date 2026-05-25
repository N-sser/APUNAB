package main.ui;

import main.data.DataManager;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class LoginPanel extends JFrame {

    // ── Palette (same as dashboard) ───────────────────────────────────────────
    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_BG = Color.WHITE;
    static final Color C_TEXT = new Color(0x1A, 0x1A, 0x1A);
    static final Color C_MUTED = new Color(0x88, 0x88, 0x88);
    static final Color C_BORDER = new Color(0xDD, 0xDD, 0xDD);
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
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(50, 48, 50, 48));

        // Title
        JLabel lblTitle = centered("APUNAB", new Font("Dialog", Font.BOLD, 32), C_ORANGE);
        JLabel lblSub = centered("Universidad Autonoma de Bucaramanga",
                new Font("Dialog", Font.PLAIN, 11), C_MUTED);

        // Fields
        setupField(fieldCode, "ID code", false);
        setupField(fieldPass, "Contraseña", true);

        // Error label
        lblError.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblError.setForeground(C_ERROR);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // Buttons
        JButton btnLogin = buildButton("Iniciar sesión", true);
        JButton btnRegister = buildButton("Crear cuenta", false);

        btnLogin.addActionListener(e -> attemptLogin());
        btnRegister.addActionListener(e -> openRegister());

        // Allow Enter key to submit
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

    // ── Logic ─────────────────────────────────────────────────────────────────

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
            showError("Codigo o contraseña incorrecta.");
            fieldPass.setText("");
        }
    }

    void openRegister() {
        // TODO: Future RegisterPanel
        JOptionPane.showMessageDialog(this, "Registration coming soon.");
    }

    void showError(String msg) {
        lblError.setText(msg);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    void setupField(JTextField field, String placeholder, boolean isPassword) {
        field.setFont(new Font("Dialog", Font.PLAIN, 13));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        field.setAlignmentX(CENTER_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDER, 1, true),
                new EmptyBorder(10, 14, 10, 14)));

        // Placeholder logic
        if (isPassword) {
            JPasswordField pf = (JPasswordField) field;
            pf.setEchoChar((char) 0); // show placeholder as plain text
            pf.setText(placeholder);
            pf.setForeground(C_MUTED);
            pf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(pf.getPassword()).equals(placeholder)) {
                        pf.setText("");
                        pf.setEchoChar('*'); // hide actual password
                        pf.setForeground(C_TEXT);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (new String(pf.getPassword()).isEmpty()) {
                        pf.setEchoChar((char) 0);
                        pf.setText(placeholder);
                        pf.setForeground(C_MUTED);
                    }
                }
            });
        } else {
            field.setText(placeholder);
            field.setForeground(C_MUTED);
            field.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (field.getText().equals(placeholder)) {
                        field.setText("");
                        field.setForeground(C_TEXT);
                    }
                }

                @Override
                public void focusLost(FocusEvent e) {
                    if (field.getText().isEmpty()) {
                        field.setText(placeholder);
                        field.setForeground(C_MUTED);
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
                Color base = primary ? C_ORANGE : C_BG;
                g2.setColor(getModel().isRollover() ? base.darker() : base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                if (!primary) {
                    g2.setColor(C_BORDER);
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Dialog", Font.BOLD, 13));
        btn.setForeground(primary ? Color.WHITE : C_TEXT);
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