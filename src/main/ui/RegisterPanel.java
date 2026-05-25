package main.ui;

import main.data.DataManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class RegisterPanel extends JFrame {

    // ── Paleta (misma que el dashboard) ───────────────────────────────────────
    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_BG = Color.WHITE;
    static final Color C_TEXT = new Color(0x1A, 0x1A, 0x1A);
    static final Color C_MUTED = new Color(0x88, 0x88, 0x88);
    static final Color C_BORDER = new Color(0xDD, 0xDD, 0xDD);
    static final Color C_ERROR = new Color(0xE7, 0x4C, 0x3C);

    private final JTextField fieldCode = new JTextField();
    private final JTextField fieldName = new JTextField();
    private final JTextField fieldEmail = new JTextField();
    private final JPasswordField fieldPass = new JPasswordField();
    private final JLabel lblError = new JLabel(" ");

    private LoginPanel previousLoginPanel;

    public RegisterPanel(LoginPanel previousLoginPanel) {
        this.previousLoginPanel = previousLoginPanel;
        setTitle("APUNAB - Register");
        setSize(400, 460);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    void buildUI() {
        JPanel root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBackground(C_BG);
        root.setBorder(new EmptyBorder(50, 48, 50, 48));

        // Titulo
        JLabel lblTitle = centered("APUNAB", new Font("Dialog", Font.BOLD, 32), C_ORANGE);
        JLabel lblSub = centered("Universidad Autonoma de Bucaramanga",
                new Font("Dialog", Font.PLAIN, 11), C_MUTED);

        // Campos
        setupField(fieldName, "Nombre completo", false);
        setupField(fieldEmail, "Email institucional", false);
        setupField(fieldCode, "Codigo de estudiante", false);
        setupField(fieldPass, "Contrasena", true);
        // Etiqueta de error
        lblError.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblError.setForeground(C_ERROR);
        lblError.setAlignmentX(CENTER_ALIGNMENT);

        // Botones
        JButton btnRegister = buildButton("Crear cuenta", true);

        btnRegister.addActionListener(e -> createUser());

        // Enter para enviar el formulario
        KeyAdapter onEnter = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    createUser();
            }
        };
        fieldPass.addKeyListener(onEnter);

        root.add(lblTitle);
        root.add(Box.createVerticalStrut(4));
        root.add(lblSub);
        root.add(Box.createVerticalStrut(36));
        root.add(fieldCode);
        root.add(Box.createVerticalStrut(12));
        root.add(fieldEmail);
        root.add(Box.createVerticalStrut(6));
        root.add(fieldName);
        root.add(Box.createVerticalStrut(6));
        root.add(fieldPass);
        root.add(Box.createVerticalStrut(12));
        root.add(lblError);
        root.add(Box.createVerticalStrut(8));
        root.add(btnRegister);

        setContentPane(root);
    }

    // ── Logica ────────────────────────────────────────────────────────────────
    void createUser() {
        String code = getRealText(fieldCode, "Codigo de estudiante");
        String email = getRealText(fieldEmail, "Email institucional");
        String name = getRealText(fieldName, "Nombre completo");
        String raw = new String(fieldPass.getPassword()).trim();

        if (code.isEmpty() || email.isEmpty() || name.isEmpty() || raw.isEmpty()) {
            showError("Porfavor rellene todos los campos.");
            return;
        }

        boolean validation = DataManager.getInstance().register(code, email, name, raw);

        if (validation) {
            confirmation(true);
            dispose();
        } else {
            confirmation(false);
        }
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

        // Logica de placeholder
        if (isPassword) {
            JPasswordField pf = (JPasswordField) field;
            pf.setEchoChar((char) 0); // mostrar placeholder como texto plano
            pf.setText(placeholder);
            pf.setForeground(C_MUTED);
            pf.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    if (new String(pf.getPassword()).equals(placeholder)) {
                        pf.setText("");
                        pf.setEchoChar('*'); // ocultar contrasena real
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

    void confirmation(boolean t) {
        if (t) {
            JOptionPane.showMessageDialog(
                    this,
                    "Usuario creado exitosamente. Ingrese en la página principal.",
                    "Status",
                    JOptionPane.PLAIN_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Codigo ID ya registrada, intente con otra lol.",
                    "Status",
                    JOptionPane.PLAIN_MESSAGE);
        }
    }

    /** Devuelve el texto real del campo, o vacio si es placeholder. */
    String getRealText(JTextField field, String placeholder) {
        String text = field.getText().trim();
        return text.equals(placeholder) ? "" : text;
    }

    @Override
    public void dispose() {
        if (previousLoginPanel != null) {
            previousLoginPanel.setVisible(true);
        }
        super.dispose();
    }
}