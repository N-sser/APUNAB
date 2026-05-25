package main.ui;

import main.data.DataManager;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MainDashboard extends JFrame {

    // ── Paleta de colores ─────────────────────────────────────────────────────
    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ORANGE_BG = new Color(0xFF, 0x8C, 0x00, 30);
    static final Color C_BG = new Color(0xF5, 0xF5, 0xF5);
    static final Color C_WHITE = Color.WHITE;
    static final Color C_TEXT = new Color(0x1A, 0x1A, 0x1A);
    static final Color C_MUTED = new Color(0x88, 0x88, 0x88);
    static final Color C_DIVIDER = new Color(0xEE, 0xEE, 0xEE);

    // ── Fuentes ───────────────────────────────────────────────────────────────
    static final Font F_BOLD_LG = new Font("Dialog", Font.BOLD, 22);
    static final Font F_BOLD_MD = new Font("Dialog", Font.BOLD, 14);
    static final Font F_BOLD_SM = new Font("Dialog", Font.BOLD, 12);
    static final Font F_PLAIN_MD = new Font("Dialog", Font.PLAIN, 13);
    static final Font F_PLAIN_SM = new Font("Dialog", Font.PLAIN, 11);

    // ── Estado ────────────────────────────────────────────────────────────────
    private final Student student;
    private final DataManager dm = DataManager.getInstance();

    // CardLayout para cambiar paneles de contenido
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardHolder = new JPanel(cardLayout);

    public MainDashboard(Student student) {
        this.student = student;
        setTitle("APUNAB - " + student.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        buildUI();
    }

    void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_BG);
        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildCardHolder(), BorderLayout.CENTER);
        setContentPane(root);
    }

    // ── Barra superior ────────────────────────────────────────────────────────

    JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(C_DIVIDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        bar.setBackground(C_WHITE);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(new EmptyBorder(0, 24, 0, 24));

        JLabel lblLogo = new JLabel("APUNAB");
        lblLogo.setFont(new Font("Dialog", Font.BOLD, 20));
        lblLogo.setForeground(C_ORANGE);

        JLabel lblUnab = new JLabel("  -  Universidad Autonoma de Bucaramanga");
        lblUnab.setFont(F_PLAIN_SM);
        lblUnab.setForeground(C_MUTED);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(lblLogo);
        left.add(lblUnab);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel lblUser = new JLabel("  " + student.getName());
        lblUser.setFont(F_PLAIN_SM);
        lblUser.setForeground(C_MUTED);

        JButton btnLogout = roundedButton("Cerrar sesion", C_ORANGE_BG, C_ORANGE, F_PLAIN_SM);
        btnLogout.addActionListener(e -> logout());

        right.add(lblUser);
        right.add(btnLogout);
        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ── Sidebar ───────────────────────────────────────────────────────────────

    JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(C_DIVIDER);
                g.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        sidebar.setBackground(C_WHITE);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(24, 0, 24, 0));

        sidebar.add(buildAvatar());
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(centeredLabel(student.getName(), F_BOLD_MD, C_TEXT));
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(centeredLabel(student.getCode(), F_PLAIN_SM, C_MUTED));
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(centeredLabel(student.getSemester(), F_PLAIN_SM, C_ORANGE));
        sidebar.add(Box.createVerticalStrut(20));
        sidebar.add(divider());
        sidebar.add(Box.createVerticalStrut(12));

        // Elementos de navegacion: texto visible -> nombre de la tarjeta en CardLayout
        String[][] navItems = {
                { "Inicio", "home" },
                { "Mis APUNAB", "apunab" },
                { "Estadisticas", "statistics" },
                { "Lugares", "places" },
                { "Perfil", "profile" },
                { "Configuracion", "settings" },
        };

        // Controlar cual esta activo para repintar al cambiar
        JPanel[] navPanels = new JPanel[navItems.length];
        for (int i = 0; i < navItems.length; i++) {
            final String card = navItems[i][1];
            final int index = i;
            navPanels[i] = navItem(navItems[i][0], i == 0, () -> {
                cardLayout.show(cardHolder, card);
                for (int j = 0; j < navPanels.length; j++)
                    navPanels[j].putClientProperty("active", j == index);
                for (JPanel p : navPanels)
                    p.repaint();
            });
            sidebar.add(navPanels[i]);
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(divider());
        sidebar.add(Box.createVerticalStrut(14));
        sidebar.add(buildApunabChip());
        sidebar.add(Box.createVerticalStrut(10));
        return sidebar;
    }

    JPanel navItem(String label, boolean initiallyActive, Runnable onClick) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                if (active) {
                    g.setColor(C_ORANGE_BG);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(C_ORANGE);
                    g.fillRect(0, 0, 4, getHeight());
                }
            }
        };
        item.putClientProperty("active", initiallyActive);
        item.setOpaque(false);
        item.setMaximumSize(new Dimension(230, 42));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lbl = new JLabel(label);
        lbl.setFont(F_PLAIN_MD);
        lbl.setForeground(C_TEXT);
        item.add(lbl);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                onClick.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!Boolean.TRUE.equals(item.getClientProperty("active"))) {
                    item.setBackground(new Color(0, 0, 0, 8));
                    item.setOpaque(true);
                    item.repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setOpaque(false);
                item.repaint();
            }
        });
        return item;
    }

    // ── Contenedor de paneles (CardLayout) ────────────────────────────────────

    JPanel buildCardHolder() {
        cardHolder.setBackground(C_BG);

        // Cada panel recibe el estudiante para mostrar datos reales.
        // Los placeholder se reemplazan con paneles reales conforme se construyan.
        cardHolder.add(buildHomePanel(), "home");
        cardHolder.add(placeholder("Mis APUNAB"), "apunab");
        cardHolder.add(placeholder("Estadisticas"), "statistics");
        cardHolder.add(placeholder("Lugares"), "places");
        cardHolder.add(placeholder("Perfil"), "profile");
        cardHolder.add(placeholder("Configuracion"), "settings");

        cardLayout.show(cardHolder, "home");
        return cardHolder;
    }

    // ── Panel de inicio (dashboard con datos reales de DataManager) ───────────

    JPanel buildHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_BG);
        panel.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JPanel headerText = new JPanel();
        headerText.setLayout(new BoxLayout(headerText, BoxLayout.Y_AXIS));
        headerText.setOpaque(false);

        JLabel lblHi = new JLabel("Bienvenido, " + student.getName().split(" ")[0] + "!");
        lblHi.setFont(F_BOLD_LG);
        lblHi.setForeground(C_TEXT);

        JLabel lblSub = new JLabel("Resumen de tu actividad APUNAB");
        lblSub.setFont(F_PLAIN_MD);
        lblSub.setForeground(C_MUTED);

        headerText.add(lblHi);
        headerText.add(Box.createVerticalStrut(4));
        headerText.add(lblSub);

        JButton btnRegister = roundedButton("+ Registrar APUNAB", C_ORANGE, Color.WHITE, F_BOLD_SM);
        btnRegister.addActionListener(e -> cardLayout.show(cardHolder, "apunab"));

        header.add(headerText, BorderLayout.WEST);
        header.add(btnRegister, BorderLayout.EAST);

        // Datos reales desde DataManager
        String code = student.getCode();
        long balance = dm.getBalance(code);
        long needed = dm.getApunabNeeded(code);
        long weekly = dm.getWeeklyTotal(code);
        long monthly = dm.getMonthlyTotal(code);
        long semester = dm.getSemesterTotal(code);
        int places = dm.getEnrolledPlaces(code).size();

        // Formato con signo + para valores positivos
        JPanel grid = new JPanel(new GridLayout(2, 3, 14, 14));
        grid.setOpaque(false);
        grid.add(card("Total APUNAB", fmt(balance), "de 100,000 para graduarse", C_ORANGE));
        grid.add(card("Esta Semana", fmtSigned(weekly), "APUNAB ganadas", new Color(0x27, 0xAE, 0x60)));
        grid.add(card("Este Mes", fmtSigned(monthly), "APUNAB ganadas", new Color(0x29, 0x80, 0xB9)));
        grid.add(card("Este Semestre", fmtSigned(semester), "APUNAB ganadas", new Color(0x8E, 0x44, 0xAD)));
        grid.add(card("APUNAB Faltantes", fmt(needed), "para graduarse (meta 100K)", new Color(0xE7, 0x4C, 0x3C)));
        grid.add(card("Lugares Activos", String.valueOf(places), "lugares registrados", new Color(0x16, 0xA0, 0x85)));

        // Accesos rapidos
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(18, 0, 0, 0));
        actions.add(roundedButton("Ver Lugares", C_ORANGE_BG, C_ORANGE, F_PLAIN_SM));
        actions.add(roundedButton("Historial de APUNAB", new Color(0xEE, 0xEE, 0xEE), C_TEXT, F_PLAIN_SM));
        actions.add(roundedButton("Estadisticas", new Color(0xEE, 0xEE, 0xEE), C_TEXT, F_PLAIN_SM));
        actions.add(roundedButton("Registrarse en Lugar", new Color(0xEE, 0xEE, 0xEE), C_TEXT, F_PLAIN_SM));

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(grid, BorderLayout.CENTER);
        center.add(actions, BorderLayout.SOUTH);

        panel.add(header, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    /** Panel temporal para secciones que aun no estan construidas. */
    JPanel placeholder(String name) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(C_BG);
        JLabel lbl = new JLabel(name + " — proximamente");
        lbl.setFont(F_BOLD_MD);
        lbl.setForeground(C_MUTED);
        p.add(lbl);
        return p;
    }

    // ── Componentes reutilizables ─────────────────────────────────────────────

    JPanel buildAvatar() {
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = 40, r = 36;
                g2.setColor(C_ORANGE_BG);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                g2.setColor(C_ORANGE);
                g2.fillOval(cx - 12, cy - 24, 24, 24);
                g2.fillArc(cx - 18, cy + 2, 36, 28, 0, 180);
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(230, 90));
        avatar.setMaximumSize(new Dimension(230, 90));
        return avatar;
    }

    /** Chip en el sidebar que muestra el balance total y progreso. */
    JPanel buildApunabChip() {
        JPanel chip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_ORANGE_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setOpaque(false);
        chip.setMaximumSize(new Dimension(198, 70));
        chip.setBorder(new EmptyBorder(12, 16, 12, 16));

        long bal = dm.getBalance(student.getCode());
        long needed = dm.getApunabNeeded(student.getCode());
        int pct = (int) (bal * 100 / DataManager.GRADUATION_GOAL);

        JLabel t1 = new JLabel("Mis APUNAB");
        t1.setFont(F_PLAIN_SM);
        t1.setForeground(C_MUTED);
        t1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t2 = new JLabel(fmt(bal));
        t2.setFont(new Font("Dialog", Font.BOLD, 20));
        t2.setForeground(C_ORANGE);
        t2.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t3 = new JLabel(String.format("Faltan %s  %d%%", fmt(needed), pct));
        t3.setFont(F_PLAIN_SM);
        t3.setForeground(C_MUTED);
        t3.setAlignmentX(LEFT_ALIGNMENT);

        chip.add(t1);
        chip.add(Box.createVerticalStrut(2));
        chip.add(t2);
        chip.add(Box.createVerticalStrut(2));
        chip.add(t3);

        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(230, 90));
        wrap.add(chip);
        return wrap;
    }

    /** Tarjeta de estadistica con barra lateral de color. */
    JPanel card(String title, String value, String subtitle, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.setColor(C_WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight() - 2, 14, 14);
                g2.fillRect(0, 7, 5, getHeight() - 9);
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 14));

        JLabel lTitle = new JLabel(title.toUpperCase());
        lTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
        lTitle.setForeground(C_MUTED);
        lTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font("Dialog", Font.BOLD, 26));
        lValue.setForeground(C_TEXT);
        lValue.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSub = new JLabel(subtitle);
        lSub.setFont(F_PLAIN_SM);
        lSub.setForeground(C_MUTED);
        lSub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lValue);
        card.add(Box.createVerticalStrut(4));
        card.add(lSub);
        return card;
    }

    /** Boton con bordes redondeados sin borde nativo de Swing. */
    JButton roundedButton(String text, Color bg, Color fg, Font font) {
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
        btn.setFont(font);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    JLabel centeredLabel(String text, Font font, Color color) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setFont(font);
        lbl.setForeground(color);
        lbl.setAlignmentX(CENTER_ALIGNMENT);
        lbl.setMaximumSize(new Dimension(230, 20));
        return lbl;
    }

    JPanel divider() {
        JPanel d = new JPanel();
        d.setBackground(C_DIVIDER);
        d.setMaximumSize(new Dimension(190, 1));
        d.setPreferredSize(new Dimension(190, 1));
        return d;
    }

    // ── Utilidades de formato ─────────────────────────────────────────────────

    /** Formato con separador de miles: 45230 -> "45,230" */
    static String fmt(long n) {
        return String.format("%,d", n);
    }

    /** Formato con signo: 1250 -> "+1,250", -500 -> "-500" */
    static String fmtSigned(long n) {
        return (n >= 0 ? "+" : "") + fmt(n);
    }

    // ── Cerrar sesion ─────────────────────────────────────────────────────────

    void logout() {
        int r = JOptionPane.showConfirmDialog(this,
                "Cerrar sesion?", "Confirmar", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            dispose();
            new LoginPanel().setVisible(true);
        }
    }
}