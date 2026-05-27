package main.ui;

import main.model.Student;
import main.util.ThemeManager;
import main.util.ThemeManager.Mode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel de configuracion.
 * Incluye gestion de tema (claro/oscuro/auto-SO) y seccion de seguridad.
 */
public class SettingsPanel extends JPanel {

    private final ThemeManager tm = ThemeManager.getInstance();
    private final Student student;

    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ORANGE_BG = new Color(0xFF, 0x8C, 0x00, 30);
    static final Color C_RED = new Color(0xE7, 0x4C, 0x3C);
    static final Color C_GREEN = new Color(0x27, 0xAE, 0x60);

    public SettingsPanel(Student student) {
        this.student = student;
        setBackground(tm.getBg());
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JLabel lblTitle = new JLabel("Configuracion");
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        lblTitle.setForeground(tm.getText());
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(24));

        // ── Tema ──────────────────────────────────────────────────────────────
        content.add(sectionTitle("Tema de la aplicacion"));
        content.add(Box.createVerticalStrut(8));
        content.add(buildThemeSection());
        content.add(Box.createVerticalStrut(24));

        // ── Seguridad ─────────────────────────────────────────────────────────
        content.add(sectionTitle("Seguridad"));
        content.add(Box.createVerticalStrut(8));
        content.add(infoRow("Cifrado de contrasena", "SHA-256 con sal por usuario"));
        content.add(infoRow("Almacenamiento", "Hash irreversible — contrasena nunca en texto plano"));
        content.add(Box.createVerticalStrut(24));

        // ── Aplicacion ────────────────────────────────────────────────────────
        content.add(sectionTitle("Aplicacion"));
        content.add(Box.createVerticalStrut(8));
        content.add(infoRow("Version", "1.0.0"));
        content.add(infoRow("Java", System.getProperty("java.version")));
        content.add(infoRow("Sistema", System.getProperty("os.name") + " " + System.getProperty("os.version")));
        content.add(infoRow("Datos guardados en", getDataPath()));
        content.add(Box.createVerticalStrut(24));

        // ── Cuenta ────────────────────────────────────────────────────────────
        content.add(sectionTitle("Cuenta"));
        content.add(Box.createVerticalStrut(8));
        content.add(infoRow("Estudiante", student.getName()));
        content.add(infoRow("Codigo", student.getCode()));
        content.add(infoRow("Semestre", student.getSemester()));
        content.add(Box.createVerticalStrut(16));

        JButton btnReset = styledButton("Borrar todos los datos", new Color(0xEE, 0xEE, 0xEE), C_RED);
        btnReset.setAlignmentX(LEFT_ALIGNMENT);
        btnReset.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "Esto eliminara todos los archivos de datos y cerrara la aplicacion.\nContinuar?",
                    "Confirmar borrado", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (r == JOptionPane.YES_OPTION) {
                deleteDataFiles();
                JOptionPane.showMessageDialog(this, "Datos eliminados. La aplicacion se cerrara.");
                System.exit(0);
            }
        });
        content.add(btnReset);

        add(content, BorderLayout.NORTH);
    }

    // ── Seccion de tema ───────────────────────────────────────────────────────

    private JPanel buildThemeSection() {
        JPanel card = roundedCard();
        card.setBorder(new EmptyBorder(18, 20, 18, 20));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        // Radio buttons
        JRadioButton rbLight = styledRadio("Claro", Mode.LIGHT);
        JRadioButton rbDark = styledRadio("Oscuro", Mode.DARK);
        JRadioButton rbAuto = styledRadio("Automatico (segun el SO)", Mode.AUTO);

        ButtonGroup group = new ButtonGroup();
        group.add(rbLight);
        group.add(rbDark);
        group.add(rbAuto);

        // Seleccionar el actual
        switch (tm.getMode()) {
            case DARK -> rbDark.setSelected(true);
            case AUTO -> rbAuto.setSelected(true);
            default -> rbLight.setSelected(true);
        }

        // Preview del estado AUTO
        boolean osDark = tm.detectOsDark();
        JLabel lblAutoHint = new JLabel(
                "  SO detectado como: " + (osDark ? "Oscuro" : "Claro"));
        lblAutoHint.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblAutoHint.setForeground(tm.getMuted());
        lblAutoHint.setAlignmentX(LEFT_ALIGNMENT);
        lblAutoHint.setVisible(tm.getMode() == Mode.AUTO);

        // Listeners
        rbLight.addActionListener(e -> {
            tm.setMode(Mode.LIGHT);
            lblAutoHint.setVisible(false);
        });
        rbDark.addActionListener(e -> {
            tm.setMode(Mode.DARK);
            lblAutoHint.setVisible(false);
        });
        rbAuto.addActionListener(e -> {
            tm.setMode(Mode.AUTO);
            lblAutoHint.setVisible(true);
        });

        // Nota informativa
        JLabel lblNote = new JLabel("El tema se aplica al reiniciar la aplicacion.");
        lblNote.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblNote.setForeground(tm.getMuted());
        lblNote.setAlignmentX(LEFT_ALIGNMENT);

        card.add(rbLight);
        card.add(Box.createVerticalStrut(8));
        card.add(rbDark);
        card.add(Box.createVerticalStrut(8));
        card.add(rbAuto);
        card.add(lblAutoHint);
        card.add(Box.createVerticalStrut(12));
        card.add(lblNote);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));
        wrap.setAlignmentX(LEFT_ALIGNMENT);
        wrap.add(card);
        wrap.setBorder(new EmptyBorder(0, 0, 4, 0));
        return wrap;
    }

    private JRadioButton styledRadio(String text, Mode mode) {
        JRadioButton rb = new JRadioButton(text);
        rb.setFont(new Font("Dialog", Font.PLAIN, 13));
        rb.setForeground(tm.getText());
        rb.setOpaque(false);
        rb.setAlignmentX(LEFT_ALIGNMENT);
        rb.setFocusPainted(false);
        return rb;
    }

    // ── Componentes reutilizables ─────────────────────────────────────────────

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Dialog", Font.BOLD, 11));
        lbl.setForeground(tm.getMuted());
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = roundedCard();
        row.setLayout(new BorderLayout());
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Dialog", Font.PLAIN, 13));
        lbl.setForeground(tm.getText());

        JLabel val = new JLabel(value);
        val.setFont(new Font("Dialog", Font.PLAIN, 13));
        val.setForeground(tm.getMuted());

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        wrap.setAlignmentX(LEFT_ALIGNMENT);
        wrap.add(row, BorderLayout.CENTER);
        wrap.setBorder(new EmptyBorder(0, 0, 4, 0));
        return wrap;
    }

    private JPanel roundedCard() {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.getSurface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(LEFT_ALIGNMENT);
        return card;
    }

    // ── Utilidades ───────────────────────────────────────────────────────────

    private String getDataPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            return (appdata != null ? appdata : System.getProperty("user.home")) + "\\APUNAB";
        }
        return System.getProperty("user.home") + "/.local/share/apunab";
    }

    private void deleteDataFiles() {
        java.nio.file.Path dir = java.nio.file.Path.of(getDataPath());
        try {
            java.nio.file.Files.list(dir).forEach(f -> {
                try {
                    java.nio.file.Files.delete(f);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? bg.darker() : bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 18, 10, 18));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}