import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * APUNAB - Menú Principal / Dashboard
 * Universidad Autónoma de Bucaramanga
 *
 * Esta ventana es la pantalla principal que se muestra después de iniciar
 * sesión.
 * Incluye: sidebar de navegación, tarjetas de estadísticas y accesos directos.
 *
 * Colores UNAB usados:
 * Naranja primario : #FF8C00
 * Texto oscuro : #1A1A1A
 * Fondo : #F5F5F5
 * Tarjetas : #FFFFFF
 */
public class MenuPrincipal extends JFrame {

    // ── Paleta UNAB ──────────────────────────────────────────────────────────
    static final Color C_NARANJA = new Color(0xFF, 0x8C, 0x00);
    static final Color C_NARANJA_BG = new Color(0xFF, 0x8C, 0x00, 30); // tint suave
    static final Color C_SIDEBAR = new Color(0xFF, 0xFF, 0xFF); // sidebar blanco
    static final Color C_SIDEBAR_BRD = new Color(0xE8, 0xE8, 0xE8);
    static final Color C_FONDO = new Color(0xF5, 0xF5, 0xF5);
    static final Color C_TARJETA = Color.WHITE;
    static final Color C_TEXTO = new Color(0x1A, 0x1A, 0x1A);
    static final Color C_TEXTO_CLARO = new Color(0x88, 0x88, 0x88);
    static final Color C_DIVIDER = new Color(0xEE, 0xEE, 0xEE);

    // ── Fuentes ───────────────────────────────────────────────────────────────
    static final Font F_BOLD_LG = new Font("Segoe UI", Font.BOLD, 22);
    static final Font F_BOLD_MD = new Font("Segoe UI", Font.BOLD, 14);
    static final Font F_BOLD_SM = new Font("Segoe UI", Font.BOLD, 12);
    static final Font F_PLAIN_MD = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_PLAIN_SM = new Font("Segoe UI", Font.PLAIN, 11);

    // ── Estado simulado (en la app real vendría del modelo) ───────────────────
    String nombreEstudiante = "Juan Pérez Gómez";
    String codigoEstudiante = "U00123456";
    String semestreActual = "4° Semestre";
    long totalApunab = 45_230L;
    long metaApunab = 100_000L;

    // ── Referencia al panel de contenido central ──────────────────────────────
    JPanel panelContenido;

    // ─────────────────────────────────────────────────────────────────────────
    public MenuPrincipal() {
        setTitle("APUNAB – Universidad Autónoma de Bucaramanga");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 680);
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        setBackground(C_FONDO);
        buildUI();
    }

    // ─── Estructura principal ─────────────────────────────────────────────────
    void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(C_FONDO);

        root.add(buildTopBar(), BorderLayout.NORTH);
        root.add(buildSidebar(), BorderLayout.WEST);
        root.add(buildContent(), BorderLayout.CENTER);

        setContentPane(root);
    }

    // ─── Barra superior ───────────────────────────────────────────────────────
    JPanel buildTopBar() {
        // Panel con borde inferior
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(C_DIVIDER);
                g.fillRect(0, getHeight() - 1, getWidth(), 1);
            }
        };
        bar.setBackground(Color.WHITE);
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setBorder(new EmptyBorder(0, 24, 0, 24));

        // Lado izquierdo: logo texto
        JLabel lblLogo = new JLabel("APUNAB");
        lblLogo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblLogo.setForeground(C_NARANJA);

        JLabel lblUnab = new JLabel("  ·  Universidad Autónoma de Bucaramanga");
        lblUnab.setFont(F_PLAIN_SM);
        lblUnab.setForeground(C_TEXTO_CLARO);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        left.setOpaque(false);
        left.add(lblLogo);
        left.add(lblUnab);

        // Lado derecho: chip de usuario + cerrar sesión
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);

        JLabel lblUser = new JLabel("● " + nombreEstudiante);
        lblUser.setFont(F_PLAIN_SM);
        lblUser.setForeground(C_TEXTO_CLARO);

        JButton btnSalir = roundedButton("Cerrar sesión", C_NARANJA_BG, C_NARANJA, F_PLAIN_SM);
        btnSalir.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "¿Deseas cerrar sesión?", "Confirmar",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (r == JOptionPane.YES_OPTION)
                System.exit(0);
        });

        right.add(lblUser);
        right.add(btnSalir);

        bar.add(left, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    // ─── Sidebar de navegación ────────────────────────────────────────────────
    JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(C_SIDEBAR_BRD);
                g.fillRect(getWidth() - 1, 0, 1, getHeight());
            }
        };
        sidebar.setBackground(C_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(24, 0, 24, 0));

        // ── Avatar + nombre ──
        JPanel avatar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth() / 2, cy = 40, r = 36;
                // fondo del círculo
                g2.setColor(C_NARANJA_BG);
                g2.fillOval(cx - r, cy - r, r * 2, r * 2);
                // icono de persona simplificado
                g2.setColor(C_NARANJA);
                g2.fillOval(cx - 12, cy - 24, 24, 24); // cabeza
                g2.fillArc(cx - 18, cy + 2, 36, 28, 0, 180); // cuerpo
            }
        };
        avatar.setOpaque(false);
        avatar.setPreferredSize(new Dimension(230, 90));
        avatar.setMaximumSize(new Dimension(230, 90));

        JLabel lblNombre = centeredLabel(nombreEstudiante, F_BOLD_MD, C_TEXTO);
        JLabel lblCodigo = centeredLabel(codigoEstudiante, F_PLAIN_SM, C_TEXTO_CLARO);
        JLabel lblSem = centeredLabel(semestreActual, F_PLAIN_SM, C_NARANJA);

        sidebar.add(avatar);
        sidebar.add(Box.createVerticalStrut(6));
        sidebar.add(lblNombre);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(lblCodigo);
        sidebar.add(Box.createVerticalStrut(2));
        sidebar.add(lblSem);
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(divider());
        sidebar.add(Box.createVerticalStrut(12));

        // ── Ítems de navegación ──
        String[][] items = {
                { "🏠", "Inicio" },
                { "🎯", "Mis APUNAB" },
                { "📊", "Estadísticas" },
                { "📍", "Lugares" },
                { "👤", "Mi Perfil" },
                { "⚙", "Configuración" },
        };

        // El primer ítem arranca activo
        for (int i = 0; i < items.length; i++) {
            final boolean activo = (i == 0);
            sidebar.add(navItem(items[i][0], items[i][1], activo));
        }

        sidebar.add(Box.createVerticalGlue());
        sidebar.add(divider());
        sidebar.add(Box.createVerticalStrut(14));

        // ── Chip de APUNAB totales ──
        sidebar.add(apunabChip());
        sidebar.add(Box.createVerticalStrut(10));

        return sidebar;
    }

    JPanel navItem(String icono, String texto, boolean activo) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 0)) {
            boolean hover = false;
            {
                setPreferredSize(new Dimension(230, 42));
                setMaximumSize(new Dimension(230, 42));
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        hover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        hover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (activo) {
                    g.setColor(C_NARANJA_BG);
                    g.fillRect(0, 0, getWidth(), getHeight());
                    g.setColor(C_NARANJA);
                    g.fillRect(0, 0, 4, getHeight());
                } else if (hover) {
                    g.setColor(new Color(0, 0, 0, 8));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        item.setOpaque(false);

        JLabel lbl = new JLabel(icono + "  " + texto);
        lbl.setFont(new Font("Segoe UI Emoji", activo ? Font.BOLD : Font.PLAIN, 13));
        lbl.setForeground(activo ? C_NARANJA : C_TEXTO);
        item.add(lbl);
        return item;
    }

    JPanel apunabChip() {
        JPanel chip = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_NARANJA_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
            }
        };
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));
        chip.setOpaque(false);
        chip.setMaximumSize(new Dimension(198, 70));
        chip.setBorder(new EmptyBorder(12, 16, 12, 16));

        long faltanAp = metaApunab - totalApunab;
        int pct = (int) (totalApunab * 100 / metaApunab);

        JLabel t1 = new JLabel("Mis APUNAB");
        t1.setFont(F_PLAIN_SM);
        t1.setForeground(C_TEXTO_CLARO);
        t1.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t2 = new JLabel(String.format("%,d", totalApunab));
        t2.setFont(new Font("Segoe UI", Font.BOLD, 20));
        t2.setForeground(C_NARANJA);
        t2.setAlignmentX(LEFT_ALIGNMENT);

        JLabel t3 = new JLabel(String.format("Faltan %,d · %d%%", faltanAp, pct));
        t3.setFont(F_PLAIN_SM);
        t3.setForeground(C_TEXTO_CLARO);
        t3.setAlignmentX(LEFT_ALIGNMENT);

        chip.add(t1);
        chip.add(Box.createVerticalStrut(2));
        chip.add(t2);
        chip.add(Box.createVerticalStrut(2));
        chip.add(t3);

        // Wrapper con margen horizontal
        JPanel wrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        wrap.setOpaque(false);
        wrap.setMaximumSize(new Dimension(230, 90));
        wrap.add(chip);
        return wrap;
    }

    // ─── Área de contenido principal ──────────────────────────────────────────
    JPanel buildContent() {
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(C_FONDO);
        panelContenido.setBorder(new EmptyBorder(28, 28, 28, 28));

        // Encabezado
        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setOpaque(false);
        encabezado.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblBienvenida = new JLabel("¡Bienvenido, " + nombreEstudiante.split(" ")[0] + "!");
        lblBienvenida.setFont(F_BOLD_LG);
        lblBienvenida.setForeground(C_TEXTO);

        JLabel lblSub = new JLabel("Aquí está el resumen de tu actividad APUNAB");
        lblSub.setFont(F_PLAIN_MD);
        lblSub.setForeground(C_TEXTO_CLARO);

        JPanel txtWrap = new JPanel();
        txtWrap.setLayout(new BoxLayout(txtWrap, BoxLayout.Y_AXIS));
        txtWrap.setOpaque(false);
        txtWrap.add(lblBienvenida);
        txtWrap.add(Box.createVerticalStrut(4));
        txtWrap.add(lblSub);

        encabezado.add(txtWrap, BorderLayout.WEST);

        // Botón registrar APUNAB rápido
        JButton btnNueva = roundedButton("+ Registrar APUNAB", C_NARANJA, Color.WHITE, F_BOLD_SM);
        encabezado.add(btnNueva, BorderLayout.EAST);

        // Grid de tarjetas 2×3
        JPanel gridTarjetas = new JPanel(new GridLayout(2, 3, 14, 14));
        gridTarjetas.setOpaque(false);

        gridTarjetas.add(card("Total APUNAB", "45,230", "de 100,000 para graduarse", C_NARANJA));
        gridTarjetas.add(card("Esta Semana", "+1,250", "APUNAB ganadas", new Color(0x27, 0xAE, 0x60)));
        gridTarjetas.add(card("Este Mes", "+4,800", "APUNAB ganadas", new Color(0x29, 0x80, 0xB9)));
        gridTarjetas.add(card("Este Semestre", "+22,100", "APUNAB ganadas", new Color(0x8E, 0x44, 0xAD)));
        gridTarjetas.add(card("APUNAB Faltantes", "54,770", "para graduarse (meta 100K)", new Color(0xE7, 0x4C, 0x3C)));
        gridTarjetas.add(card("Lugares Activos", "3", "lugares registrados", new Color(0x16, 0xA0, 0x85)));

        // Barra de accesos rápidos
        JPanel accesos = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        accesos.setOpaque(false);
        accesos.setBorder(new EmptyBorder(18, 0, 0, 0));

        accesos.add(roundedButton("Ver Lugares", C_NARANJA_BG, C_NARANJA, F_PLAIN_SM));
        accesos.add(roundedButton("Historial de APUNAB", new Color(0xEE, 0xEE, 0xEE), C_TEXTO, F_PLAIN_SM));
        accesos.add(roundedButton("Estadísticas", new Color(0xEE, 0xEE, 0xEE), C_TEXTO, F_PLAIN_SM));
        accesos.add(roundedButton("Registrarse en Lugar", new Color(0xEE, 0xEE, 0xEE), C_TEXTO, F_PLAIN_SM));

        JPanel centro = new JPanel(new BorderLayout());
        centro.setOpaque(false);
        centro.add(gridTarjetas, BorderLayout.CENTER);
        centro.add(accesos, BorderLayout.SOUTH);

        panelContenido.add(encabezado, BorderLayout.NORTH);
        panelContenido.add(centro, BorderLayout.CENTER);

        return panelContenido;
    }

    // ─── Componentes reutilizables ─────────────────────────────────────────────

    /** Tarjeta de estadística con barra de color lateral */
    JPanel card(String titulo, String valor, String subtitulo, Color acento) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra suave
                g2.setColor(new Color(0, 0, 0, 12));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 14, 14);
                // Fondo blanco
                g2.setColor(C_TARJETA);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                // Barra lateral de acento
                g2.setColor(acento);
                g2.fillRoundRect(0, 0, 5, getHeight() - 2, 14, 14);
                g2.fillRect(0, 7, 5, getHeight() - 9); // cuadrar el lado derecho de la barra
                g2.dispose();
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(16, 20, 16, 14));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JLabel lTitulo = new JLabel(titulo.toUpperCase());
        lTitulo.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lTitulo.setForeground(C_TEXTO_CLARO);
        lTitulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lValor = new JLabel(valor);
        lValor.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lValor.setForeground(C_TEXTO);
        lValor.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lSub = new JLabel(subtitulo);
        lSub.setFont(F_PLAIN_SM);
        lSub.setForeground(C_TEXTO_CLARO);
        lSub.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lTitulo);
        card.add(Box.createVerticalStrut(6));
        card.add(lValor);
        card.add(Box.createVerticalStrut(4));
        card.add(lSub);
        return card;
    }

    /** Botón con bordes redondeados y sin borde nativo */
    JButton roundedButton(String texto, Color fondo, Color fg, Font fuente) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? fondo.darker() : fondo);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setFont(fuente);
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    JLabel centeredLabel(String texto, Font fuente, Color color) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(fuente);
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

    // ─── Entry point ──────────────────────────────────────────────────────────
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> new MenuPrincipal().setVisible(true));
    }
}