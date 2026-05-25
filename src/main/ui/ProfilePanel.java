package main.ui;

import main.data.DataManager;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Panel de perfil del estudiante.
 * Muestra informacion personal y academica en tarjetas,
 * siguiendo el diseño de Figma (pagina 2 de diseños.pdf).
 */
public class ProfilePanel extends JPanel {

    private final Student student;
    private final DataManager dm = DataManager.getInstance();

    // Reutilizar paleta del dashboard
    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ORANGE_BG = new Color(0xFF, 0x8C, 0x00, 30);
    static final Color C_BG = new Color(0xF5, 0xF5, 0xF5);
    static final Color C_WHITE = Color.WHITE;
    static final Color C_TEXT = new Color(0x1A, 0x1A, 0x1A);
    static final Color C_MUTED = new Color(0x88, 0x88, 0x88);
    static final Color C_BORDER = new Color(0xE8, 0xE8, 0xE8);

    public ProfilePanel(Student student) {
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

        content.add(buildHeader());
        content.add(Box.createVerticalStrut(24));
        content.add(buildInfoGrid());

        add(content, BorderLayout.NORTH);
    }

    // ── Encabezado con avatar, nombre y badge ─────────────────────────────────

    private JPanel buildHeader() {
        JPanel header = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setLayout(new BorderLayout(20, 0));
        header.setBorder(new EmptyBorder(28, 28, 28, 28));
        header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 180));

        // Avatar grande
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = 50, cy = 50, r = 45;
                g2.setColor(C_ORANGE_BG);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(C_ORANGE);
                g2.fillOval(cx - 15, cy - 30, 30, 30);
                g2.fillArc(cx - 22, cy + 2, 44, 34, 0, 180);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(100, 100));

        // Info del estudiante
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblName = new JLabel(student.getName());
        lblName.setFont(new Font("Dialog", Font.BOLD, 24));
        lblName.setForeground(C_TEXT);

        // Badge "Estudiante Activo"
        JLabel lblBadge = new JLabel("  Estudiante Activo  ") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_ORANGE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblBadge.setFont(new Font("Dialog", Font.BOLD, 11));
        lblBadge.setForeground(C_ORANGE);
        lblBadge.setMaximumSize(new Dimension(140, 24));

        JLabel lblDesc = new JLabel("Bienvenido a tu perfil. Aqui puedes ver tu informacion personal y academica.");
        lblDesc.setFont(new Font("Dialog", Font.PLAIN, 13));
        lblDesc.setForeground(C_MUTED);

        info.add(lblName);
        info.add(Box.createVerticalStrut(6));
        info.add(lblBadge);
        info.add(Box.createVerticalStrut(10));
        info.add(lblDesc);

        // Balance APUNAB a la derecha
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);

        long bal = dm.getBalance(student.getCode());
        int pct = (int) (bal * 100 / DataManager.GRADUATION_GOAL);

        JLabel lblBalTitle = new JLabel("Mis APUNAB");
        lblBalTitle.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblBalTitle.setForeground(C_MUTED);
        lblBalTitle.setAlignmentX(RIGHT_ALIGNMENT);

        JLabel lblBal = new JLabel(String.format("%,d", bal));
        lblBal.setFont(new Font("Dialog", Font.BOLD, 28));
        lblBal.setForeground(C_ORANGE);
        lblBal.setAlignmentX(RIGHT_ALIGNMENT);

        JLabel lblPct = new JLabel(pct + "% de 100,000");
        lblPct.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblPct.setForeground(C_MUTED);
        lblPct.setAlignmentX(RIGHT_ALIGNMENT);

        rightPanel.add(lblBalTitle);
        rightPanel.add(Box.createVerticalStrut(4));
        rightPanel.add(lblBal);
        rightPanel.add(Box.createVerticalStrut(2));
        rightPanel.add(lblPct);

        header.add(avatar, BorderLayout.WEST);
        header.add(info, BorderLayout.CENTER);
        header.add(rightPanel, BorderLayout.EAST);
        return header;
    }

    // ── Grid de tarjetas de informacion ───────────────────────────────────────

    private JPanel buildInfoGrid() {
        JPanel grid = new JPanel(new GridLayout(2, 3, 12, 12));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        grid.add(infoCard("Codigo de estudiante", student.getCode()));
        grid.add(infoCard("Semestre", student.getSemester()));
        grid.add(infoCard("Correo institucional", student.getEmail().toLowerCase() + "@unab.edu.co"));
        grid.add(infoCard("Carrera", "Ingenieria de Sistemas"));
        grid.add(infoCard("Facultad", "Ingenieria"));
        grid.add(infoCard("Lugares activos", dm.getEnrolledPlaces(student.getCode()).size() + " registrados"));

        return grid;
    }

    /** Tarjeta individual de informacion con titulo y valor. */
    private JPanel infoCard(String title, String value) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(C_BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lTitle = new JLabel(title);
        lTitle.setFont(new Font("Dialog", Font.PLAIN, 11));
        lTitle.setForeground(C_MUTED);
        lTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font("Dialog", Font.BOLD, 14));
        lValue.setForeground(C_ORANGE);
        lValue.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lValue);
        return card;
    }
}
