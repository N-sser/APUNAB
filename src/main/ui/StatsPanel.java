package main.ui;

import main.util.ThemeManager;
import main.data.DataManager;
import main.model.Bet;
import main.model.Place;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Panel de estadisticas APUNAB.
 * Muestra resumen visual: tarjetas, barra de progreso,
 * distribucion por lugar y resumen de victorias/derrotas.
 */
public class StatsPanel extends JPanel {

    private final Student student;
    private final ThemeManager tm = ThemeManager.getInstance();
    private final DataManager dm = DataManager.getInstance();

    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ORANGE_BG = new Color(0xFF, 0x8C, 0x00, 30);
    static final Color C_GREEN = new Color(0x27, 0xAE, 0x60);
    static final Color C_RED = new Color(0xE7, 0x4C, 0x3C);
    static final Color C_BLUE = new Color(0x29, 0x80, 0xB9);
    static final Color C_PURPLE = new Color(0x8E, 0x44, 0xAD);

    public StatsPanel(Student student) {
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

        // Titulo
        JLabel lblTitle = new JLabel("Estadisticas APUNAB");
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        lblTitle.setForeground(tm.getText());
        lblTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblSub = new JLabel("Resumen de tu progreso hacia los 100,000 APUNAB");
        lblSub.setFont(new Font("Dialog", Font.PLAIN, 13));
        lblSub.setForeground(tm.getMuted());
        lblSub.setAlignmentX(LEFT_ALIGNMENT);

        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));
        content.add(lblSub);
        content.add(Box.createVerticalStrut(20));

        // Barra de progreso grande
        content.add(buildProgressSection());
        content.add(Box.createVerticalStrut(16));

        // Tarjetas de periodo
        content.add(buildPeriodCards());
        content.add(Box.createVerticalStrut(16));

        // Resumen por lugar + victorias/derrotas
        content.add(buildBottomRow());

        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(scroll, BorderLayout.CENTER);
    }

    // ── Barra de progreso hacia la meta ─────────────────────────────────────

    private JPanel buildProgressSection() {
        long balance = dm.getBalance(student.getCode());
        long goal = DataManager.GRADUATION_GOAL;
        int pct = (int) (balance * 100 / goal);

        JPanel section = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.getSurface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        section.setOpaque(false);
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBorder(new EmptyBorder(22, 22, 22, 22));
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Texto del progreso
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);

        JLabel lblProgress = new JLabel("Progreso de graduacion");
        lblProgress.setFont(new Font("Dialog", Font.BOLD, 14));
        lblProgress.setForeground(tm.getText());

        JLabel lblPct = new JLabel(String.format("%,d / %,d APUNAB  (%d%%)", balance, goal, pct));
        lblPct.setFont(new Font("Dialog", Font.BOLD, 13));
        lblPct.setForeground(C_ORANGE);

        topRow.add(lblProgress, BorderLayout.WEST);
        topRow.add(lblPct, BorderLayout.EAST);
        topRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        topRow.setAlignmentX(LEFT_ALIGNMENT);

        // Barra visual
        JPanel bar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo gris
                g2.setColor(new Color(0xEE, 0xEE, 0xEE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                // Barra naranja proporcional
                int filled = (int) (getWidth() * Math.min(pct, 100) / 100.0);
                if (filled > 0) {
                    g2.setColor(C_ORANGE);
                    g2.fillRoundRect(0, 0, filled, getHeight(), 10, 10);
                }
                g2.dispose();
            }
        };
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(0, 18));
        bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 18));
        bar.setAlignmentX(LEFT_ALIGNMENT);

        section.add(topRow);
        section.add(Box.createVerticalStrut(12));
        section.add(bar);
        section.setAlignmentX(LEFT_ALIGNMENT);
        return section;
    }

    // ── Tarjetas de totales por periodo ──────────────────────────────────────

    private JPanel buildPeriodCards() {
        String code = student.getCode();
        long weekly = dm.getWeeklyTotal(code);
        long monthly = dm.getMonthlyTotal(code);
        long semester = dm.getSemesterTotal(code);
        long needed = dm.getApunabNeeded(code);

        JPanel grid = new JPanel(new GridLayout(1, 4, 12, 0));
        grid.setOpaque(false);
        grid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 100));
        grid.setAlignmentX(LEFT_ALIGNMENT);

        grid.add(statCard("Esta Semana", fmtSigned(weekly), C_GREEN));
        grid.add(statCard("Este Mes", fmtSigned(monthly), C_BLUE));
        grid.add(statCard("Este Semestre", fmtSigned(semester), C_PURPLE));
        grid.add(statCard("Faltantes", fmt(needed), C_RED));

        return grid;
    }

    private JPanel statCard(String title, String value, Color accent) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.getSurface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 4, getHeight(), 12, 12);
                g2.fillRect(0, 6, 4, getHeight() - 6);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(16, 18, 16, 14));

        JLabel lTitle = new JLabel(title.toUpperCase());
        lTitle.setFont(new Font("Dialog", Font.PLAIN, 10));
        lTitle.setForeground(tm.getMuted());
        lTitle.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lValue = new JLabel(value);
        lValue.setFont(new Font("Dialog", Font.BOLD, 22));
        lValue.setForeground(tm.getText());
        lValue.setAlignmentX(LEFT_ALIGNMENT);

        card.add(lTitle);
        card.add(Box.createVerticalStrut(6));
        card.add(lValue);
        return card;
    }

    // ── Fila inferior: distribucion por lugar + victorias/derrotas ───────────

    private JPanel buildBottomRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 12, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 220));
        row.setAlignmentX(LEFT_ALIGNMENT);

        row.add(buildPlaceBreakdown());
        row.add(buildWinLossCard());
        return row;
    }

    /** Desglose de APUNAB ganadas/perdidas por lugar. */
    private JPanel buildPlaceBreakdown() {
        JPanel card = roundedWhitePanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("APUNAB por lugar");
        title.setFont(new Font("Dialog", Font.BOLD, 14));
        title.setForeground(tm.getText());
        title.setAlignmentX(LEFT_ALIGNMENT);
        card.add(title);
        card.add(Box.createVerticalStrut(12));

        List<Bet> bets = dm.getBetsByStudent(student.getCode());
        // Agrupar por lugar
        Map<String, Long> byPlace = bets.stream()
                .collect(Collectors.groupingBy(Bet::getPlaceId, Collectors.summingLong(Bet::getAmount)));

        long maxAbs = byPlace.values().stream().mapToLong(Math::abs).max().orElse(1);

        for (Map.Entry<String, Long> entry : byPlace.entrySet()) {
            Place p = dm.findPlaceById(entry.getKey());
            String name = (p != null) ? p.getName() : entry.getKey();
            long val = entry.getValue();

            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
            row.setAlignmentX(LEFT_ALIGNMENT);

            JLabel lblName = new JLabel(name);
            lblName.setFont(new Font("Dialog", Font.PLAIN, 12));
            lblName.setForeground(tm.getText());
            lblName.setPreferredSize(new Dimension(120, 20));

            // Mini barra proporcional
            int barWidth = (int) (150.0 * Math.abs(val) / maxAbs);
            Color barColor = val >= 0 ? C_GREEN : C_RED;
            JPanel miniBar = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(barColor);
                    g2.fillRoundRect(0, 4, barWidth, 12, 6, 6);
                    g2.dispose();
                }
            };
            miniBar.setOpaque(false);

            JLabel lblVal = new JLabel(fmtSigned(val));
            lblVal.setFont(new Font("Dialog", Font.BOLD, 12));
            lblVal.setForeground(val >= 0 ? C_GREEN : C_RED);
            lblVal.setPreferredSize(new Dimension(80, 20));
            lblVal.setHorizontalAlignment(SwingConstants.RIGHT);

            row.add(lblName, BorderLayout.WEST);
            row.add(miniBar, BorderLayout.CENTER);
            row.add(lblVal, BorderLayout.EAST);

            card.add(row);
            card.add(Box.createVerticalStrut(4));
        }

        return card;
    }

    /** Tarjeta de resumen victorias vs derrotas. */
    private JPanel buildWinLossCard() {
        List<Bet> bets = dm.getBetsByStudent(student.getCode());
        long won = 0, lost = 0, pend = 0;
        for (Bet b : bets) {
            switch (b.getResult()) {
                case WON -> won++;
                case LOST -> lost++;
                case PENDING -> pend++;
            }
        }
        long total = bets.size();
        int winRate = total > 0 ? (int) (won * 100 / total) : 0;

        JPanel card = roundedWhitePanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(new EmptyBorder(18, 20, 18, 20));

        JLabel title = new JLabel("Resumen de apuestas");
        title.setFont(new Font("Dialog", Font.BOLD, 14));
        title.setForeground(tm.getText());
        title.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lblRate = new JLabel("Tasa de victoria: " + winRate + "%");
        lblRate.setFont(new Font("Dialog", Font.BOLD, 18));
        lblRate.setForeground(C_ORANGE);
        lblRate.setAlignmentX(LEFT_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(14));
        card.add(lblRate);
        card.add(Box.createVerticalStrut(14));
        card.add(summaryRow("Ganadas", String.valueOf(won), C_GREEN));
        card.add(Box.createVerticalStrut(6));
        card.add(summaryRow("Perdidas", String.valueOf(lost), C_RED));
        card.add(Box.createVerticalStrut(6));
        card.add(summaryRow("Pendientes", String.valueOf(pend), C_ORANGE));
        card.add(Box.createVerticalStrut(6));
        card.add(summaryRow("Total", String.valueOf(total), tm.getText()));

        return card;
    }

    private JPanel summaryRow(String label, String value, Color color) {
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        row.setAlignmentX(LEFT_ALIGNMENT);

        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Dialog", Font.PLAIN, 13));
        lbl.setForeground(tm.getMuted());

        JLabel val = new JLabel(value);
        val.setFont(new Font("Dialog", Font.BOLD, 14));
        val.setForeground(color);

        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.EAST);
        return row;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel roundedWhitePanel() {
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(tm.getSurface());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
    }

    static String fmt(long n) {
        return String.format("%,d", n);
    }

    static String fmtSigned(long n) {
        return (n >= 0 ? "+" : "") + fmt(n);
    }
}
