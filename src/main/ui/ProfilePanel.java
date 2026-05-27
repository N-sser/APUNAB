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

    // Campos editables — referenciados desde el boton Actualizar
    private JTextField fieldEmail;
    private JTextField fieldPhone;

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
        content.add(Box.createVerticalStrut(14));
        content.add(buildActualizarButton());

        add(content, BorderLayout.NORTH);
    }

    private void rebuild() {
        removeAll();
        setBackground(C_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
        revalidate();
        repaint();
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

        fieldEmail = new JTextField(student.getEmail());
        fieldPhone = new JTextField(student.getPhone().isEmpty() ? "" : student.getPhone());

        grid.add(infoCard("Codigo de estudiante", student.getCode()));
        grid.add(infoCard("Semestre", student.getSemester()));
        grid.add(editableInfoCard("Correo institucional", fieldEmail));
        grid.add(editableInfoCard("Telefono", fieldPhone));
        grid.add(infoCard("Carrera", "Ingenieria de Sistemas"));
        grid.add(infoCard("Lugares activos", dm.getEnrolledPlaces(student.getCode()).size() + " registrados"));

        return grid;
    }

    /** Tarjeta de solo lectura. */
    private JPanel infoCard(String title, String value) {
        JPanel card = roundedCard();
        card.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lTitle = mutedLabel(title);
        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font("Dialog", Font.BOLD, 14));
        lValue.setForeground(C_ORANGE);
        lValue.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lValue);
        return card;
    }

    /** Tarjeta editable: el valor es un JTextField que parece un label. */
    private JPanel editableInfoCard(String title, JTextField field) {
        JPanel card = roundedCard();
        card.setBorder(new EmptyBorder(16, 18, 10, 18));

        JLabel lTitle = mutedLabel(title);

        // Estilo del campo para que luzca como el label naranja
        field.setFont(new Font("Dialog", Font.BOLD, 14));
        field.setForeground(C_ORANGE);
        field.setBackground(C_WHITE);
        field.setCaretColor(C_ORANGE);
        field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        field.setAlignmentX(LEFT_ALIGNMENT);

        // Al enfocar, subrayado naranja; al perder foco, vuelve a gris
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, C_ORANGE));
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                field.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, C_BORDER));
            }
        });

        card.add(lTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(field);
        return card;
    }

    /** Boton Actualizar alineado a la derecha, bajo el grid. */
    private JPanel buildActualizarButton() {
        JButton btn = new JButton("Actualizar") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? C_ORANGE.darker() : C_ORANGE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Dialog", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 24, 10, 24));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addActionListener(e -> {
            String email = fieldEmail.getText().trim();
            String phone = fieldPhone.getText().trim();
            if (email.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El correo no puede estar vacio.", "Error",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            dm.updateStudentContact(student.getCode(), email, phone);
            rebuild();
        });

        JPanel row = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        row.add(btn);
        return row;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel roundedCard() {
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
        return card;
    }

    private JLabel mutedLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Dialog", Font.PLAIN, 11));
        lbl.setForeground(C_MUTED);
        lbl.setAlignmentX(LEFT_ALIGNMENT);
        return lbl;
    }
}