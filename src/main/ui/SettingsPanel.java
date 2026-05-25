package main.ui;

import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.nio.file.Path;

/**
 * Panel de configuracion.
 * Muestra informacion del sistema y opciones basicas.
 */
public class SettingsPanel extends JPanel {

    private final Student student;

    static final Color C_ORANGE    = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ORANGE_BG = new Color(0xFF, 0x8C, 0x00, 30);
    static final Color C_BG        = new Color(0xF5, 0xF5, 0xF5);
    static final Color C_WHITE     = Color.WHITE;
    static final Color C_TEXT      = new Color(0x1A, 0x1A, 0x1A);
    static final Color C_MUTED     = new Color(0x88, 0x88, 0x88);
    static final Color C_BORDER    = new Color(0xE8, 0xE8, 0xE8);
    static final Color C_RED       = new Color(0xE7, 0x4C, 0x3C);

    public SettingsPanel(Student student) {
        this.student = student;
        setBackground(C_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
    }

    private void buildUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        // Titulo
        JLabel lblTitle = new JLabel("Configuracion");
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        lblTitle.setForeground(C_TEXT);
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(20));

        // Seccion: Informacion de la aplicacion
        content.add(sectionTitle("Aplicacion"));
        content.add(Box.createVerticalStrut(8));
        content.add(infoRow("Version",        "1.0.0"));
        content.add(infoRow("Java",           System.getProperty("java.version")));
        content.add(infoRow("Sistema",         System.getProperty("os.name") + " " + System.getProperty("os.version")));
        content.add(infoRow("Datos guardados en", getDataPath()));
        content.add(Box.createVerticalStrut(20));

        // Seccion: Cuenta
        content.add(sectionTitle("Cuenta"));
        content.add(Box.createVerticalStrut(8));
        content.add(infoRow("Estudiante",     student.getName()));
        content.add(infoRow("Codigo",          student.getCode()));
        content.add(infoRow("Semestre",        student.getSemester()));
        content.add(Box.createVerticalStrut(16));

        // Boton para borrar datos (reset)
        JButton btnReset = styledButton("Borrar todos los datos", new Color(0xEE,0xEE,0xEE), C_RED);
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

    // ── Componentes ─────────────────────────────────────────────────────────

    private JLabel sectionTitle(String text) {
        JLabel lbl = new JLabel(text.toUpperCase());
        lbl.setFont(new Font("Dialog", Font.BOLD, 11));
        lbl.setForeground(C_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }

    private JPanel infoRow(String label, String value) {
        JPanel row = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
            }
        };
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(12, 16, 12, 16));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Dialog", Font.PLAIN, 13));
        lbl.setForeground(C_TEXT);

        JLabel val = new JLabel(value);
        val.setFont(new Font("Dialog", Font.PLAIN, 13));
        val.setForeground(C_MUTED);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);

        // Wrapper con espaciado inferior
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        wrap.setAlignmentX(LEFT_ALIGNMENT);
        wrap.add(row, BorderLayout.CENTER);
        wrap.setBorder(new EmptyBorder(0, 0, 4, 0));
        return wrap;
    }

    // ── Utilidades ──────────────────────────────────────────────────────────

    private String getDataPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            return (appdata != null ? appdata : System.getProperty("user.home")) + "\\APUNAB";
        }
        return System.getProperty("user.home") + "/.local/share/apunab";
    }

    private void deleteDataFiles() {
        Path dir = Path.of(getDataPath());
        try {
            java.nio.file.Files.list(dir).forEach(f -> {
                try { java.nio.file.Files.delete(f); } catch (Exception ignored) {}
            });
        } catch (Exception ignored) {}
    }

    private JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
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
