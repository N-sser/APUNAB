package main.ui;

import main.util.ThemeManager;
import main.data.DataManager;
import main.model.Bet;
import main.model.Bet.Result;
import main.model.Place;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Panel de gestion de apuestas APUNAB.
 * CRUD completo: listar, agregar, editar y eliminar apuestas.
 * Usa JTable para la lista y JOptionPane para formularios.
 */
public class BetsPanel extends JPanel {

    private static final ThemeManager tm = ThemeManager.getInstance();
    private final Student student;
    private final DataManager dm = DataManager.getInstance();
    private DefaultTableModel tableModel;
    private JTable table;

    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_GREEN = new Color(0x27, 0xAE, 0x60);
    static final Color C_RED = new Color(0xE7, 0x4C, 0x3C);

    // Columnas de la tabla
    private static final String[] COLUMNS = {
            "ID", "Lugar", "Monto", "Fecha", "Resultado"
    };

    public BetsPanel(Student student) {
        this.student = student;
        setBackground(tm.getBg());
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
    }

    private void buildUI() {
        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setOpaque(false);

        JLabel lblTitle = new JLabel("Mis APUNAB");
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        lblTitle.setForeground(tm.getText());

        long balance = dm.getBalance(student.getCode());
        JLabel lblBalance = new JLabel("Balance actual: " + String.format("%,d", balance) + " APUNAB");
        lblBalance.setFont(new Font("Dialog", Font.PLAIN, 13));
        lblBalance.setForeground(tm.getMuted());

        titleWrap.add(lblTitle);
        titleWrap.add(Box.createVerticalStrut(4));
        titleWrap.add(lblBalance);

        JButton btnAdd = styledButton("+ Registrar APUNAB", C_ORANGE, Color.WHITE);
        btnAdd.addActionListener(e -> showAddDialog());

        header.add(titleWrap, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);

        // Tabla
        tableModel = new DefaultTableModel(COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        styleTable();
        refreshTable();

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xE8, 0xE8, 0xE8)));
        scroll.getViewport().setBackground(tm.getSurface());

        // Botones de accion bajo la tabla
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        actions.setOpaque(false);
        actions.setBorder(new EmptyBorder(12, 0, 0, 0));

        JButton btnEdit = styledButton("Editar", new Color(0xEE, 0xEE, 0xEE), tm.getText());
        btnEdit.addActionListener(e -> editSelected());

        JButton btnDel = styledButton("Eliminar", new Color(0xEE, 0xEE, 0xEE), C_RED);
        btnDel.addActionListener(e -> deleteSelected());

        JButton btnExport = styledButton("Exportar CSV", new Color(0xEE, 0xEE, 0xEE), new Color(0x27, 0xAE, 0x60));
        btnExport.addActionListener(e -> exportCsv());

        actions.add(btnEdit);
        actions.add(btnDel);
        actions.add(btnExport);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);
    }

    // ── Estilo de la tabla ──────────────────────────────────────────────────

    private void styleTable() {
        table.setFont(new Font("Dialog", Font.PLAIN, 13));
        table.setRowHeight(36);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0xFF, 0x8C, 0x00, 30));
        table.setSelectionForeground(tm.getText());
        table.getTableHeader().setFont(new Font("Dialog", Font.BOLD, 12));
        table.getTableHeader().setBackground(tm.getSurface());
        table.getTableHeader().setForeground(tm.getMuted());
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xE8, 0xE8, 0xE8)));
        table.setFillsViewportHeight(true);

        // Renderer personalizado para colorear el resultado
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String result = val.toString();
                if (result.equals("WON"))
                    setForeground(C_GREEN);
                else if (result.equals("LOST"))
                    setForeground(C_RED);
                else
                    setForeground(C_ORANGE);
                setFont(new Font("Dialog", Font.BOLD, 12));
                return this;
            }
        });

        // Renderer para montos (color segun signo)
        table.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object val, boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, focus, row, col);
                String text = val.toString();
                if (text.startsWith("+"))
                    setForeground(C_GREEN);
                else if (text.startsWith("-"))
                    setForeground(C_RED);
                else
                    setForeground(tm.getText());
                setFont(new Font("Dialog", Font.BOLD, 13));
                return this;
            }
        });
    }

    // ── Recarga de datos en la tabla ─────────────────────────────────────────

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Bet> bets = dm.getBetsByStudent(student.getCode());

        // Mas recientes primero
        for (int i = bets.size() - 1; i >= 0; i--) {
            Bet b = bets.get(i);
            Place place = dm.findPlaceById(b.getPlaceId());
            String placeName = (place != null) ? place.getName() : b.getPlaceId();
            long amt = b.getAmount();
            String amtStr = (amt >= 0 ? "+" : "") + String.format("%,d", amt);

            tableModel.addRow(new Object[] {
                    b.getId(),
                    placeName,
                    amtStr,
                    b.getDate().toString(),
                    b.getResult().name()
            });
        }
    }

    // ── Dialogo para agregar apuesta ────────────────────────────────────────

    private void showAddDialog() {
        List<Place> places = dm.getPlaces();
        if (places.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay lugares registrados.");
            return;
        }

        String[] placeNames = places.stream().map(Place::getName).toArray(String[]::new);
        JComboBox<String> comboPlace = new JComboBox<>(placeNames);
        JTextField fieldAmount = new JTextField();
        JComboBox<String> comboResult = new JComboBox<>(new String[] { "WON", "LOST", "PENDING" });
        JTextField fieldDate = new JTextField(LocalDate.now().toString());

        Object[] form = {
                "Lugar:", comboPlace,
                "Monto:", fieldAmount,
                "Resultado:", comboResult,
                "Fecha (YYYY-MM-DD):", fieldDate
        };

        int r = JOptionPane.showConfirmDialog(this, form,
                "Registrar APUNAB", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (r != JOptionPane.OK_OPTION)
            return;

        try {
            Place selected = places.get(comboPlace.getSelectedIndex());
            long amount = Long.parseLong(fieldAmount.getText().trim());
            Result result = Result.valueOf((String) comboResult.getSelectedItem());
            LocalDate date = LocalDate.parse(fieldDate.getText().trim());

            // Si perdio, monto negativo
            if (result == Result.LOST && amount > 0)
                amount = -amount;

            dm.addBet(new Bet(dm.nextBetId(), student.getCode(), selected.getId(),
                    amount, date, result));
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un numero.");
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Formato de fecha invalido. Use YYYY-MM-DD.");
        }
    }

    // ── Editar apuesta seleccionada ─────────────────────────────────────────

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una apuesta para editar.");
            return;
        }

        String betId = (String) tableModel.getValueAt(row, 0);
        Bet bet = dm.findBetById(betId);
        if (bet == null)
            return;

        JTextField fieldAmount = new JTextField(String.valueOf(Math.abs(bet.getAmount())));
        JComboBox<String> comboResult = new JComboBox<>(new String[] { "WON", "LOST", "PENDING" });
        comboResult.setSelectedItem(bet.getResult().name());

        Object[] form = {
                "Monto:", fieldAmount,
                "Resultado:", comboResult
        };

        int r = JOptionPane.showConfirmDialog(this, form,
                "Editar apuesta " + betId, JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (r != JOptionPane.OK_OPTION)
            return;

        try {
            long amount = Long.parseLong(fieldAmount.getText().trim());
            Result result = Result.valueOf((String) comboResult.getSelectedItem());
            if (result == Result.LOST && amount > 0)
                amount = -amount;

            bet.setAmount(amount);
            bet.setResult(result);
            dm.updateBet(bet);
            refreshTable();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El monto debe ser un numero.");
        }
    }

    // ── Eliminar apuesta seleccionada ───────────────────────────────────────

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Selecciona una apuesta para eliminar.");
            return;
        }

        String betId = (String) tableModel.getValueAt(row, 0);
        int r = JOptionPane.showConfirmDialog(this,
                "Eliminar apuesta " + betId + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);

        if (r == JOptionPane.YES_OPTION) {
            dm.deleteBet(betId);
            refreshTable();
        }
    }

    // ── Exportar CSV ────────────────────────────────────────────────────────

    private void exportCsv() {
        List<Bet> bets = dm.getBetsByStudent(student.getCode());
        if (bets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay apuestas para exportar.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Guardar reporte CSV");
        chooser.setSelectedFile(new java.io.File("apunab_reporte_" + student.getCode() + ".csv"));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;

        java.io.File file = chooser.getSelectedFile();
        // Agregar extension si el usuario no la puso
        if (!file.getName().toLowerCase().endsWith(".csv"))
            file = new java.io.File(file.getAbsolutePath() + ".csv");

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(
                        new java.io.FileOutputStream(file), java.nio.charset.StandardCharsets.UTF_8))) {

            // Cabecera
            pw.println("ID,Lugar,Monto,Fecha,Resultado");

            for (int i = bets.size() - 1; i >= 0; i--) {
                Bet b = bets.get(i);
                Place place = dm.findPlaceById(b.getPlaceId());
                String placeName = (place != null) ? place.getName() : b.getPlaceId();
                // Escapar comas en el nombre del lugar
                if (placeName.contains(","))
                    placeName = "\"" + placeName + "\"";

                pw.printf("%s,%s,%d,%s,%s%n",
                        b.getId(),
                        placeName,
                        b.getAmount(),
                        b.getDate().toString(),
                        b.getResult().name());
            }

            JOptionPane.showMessageDialog(this,
                    "Reporte exportado:\n" + file.getAbsolutePath(),
                    "Exportacion exitosa", JOptionPane.INFORMATION_MESSAGE);

        } catch (java.io.IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al guardar el archivo:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Boton reutilizable ──────────────────────────────────────────────────

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
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}