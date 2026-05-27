package main.ui;

import main.util.ThemeManager;
import main.data.DataManager;
import main.model.Place;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

/**
 * Panel de gestion de lugares APUNAB.
 * Muestra todos los lugares en tarjetas con opcion de inscribirse o darse de
 * baja.
 * Incluye CRUD basico: agregar, editar, eliminar lugares.
 */
public class PlacesPanel extends JPanel {

    private static final ThemeManager tm = ThemeManager.getInstance();
    private final Student student;
    private final DataManager dm = DataManager.getInstance();
    private JPanel cardsContainer;

    // ── Filtros ──────────────────────────────────────────────────────────────
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private boolean showFavOnly = false;

    static final String[] CATEGORIES = { "Todas", "Cafeteria", "Deportes", "Actividades", "Biblioteca", "Otro" };

    static final Color C_ORANGE = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ORANGE_BG = new Color(0xFF, 0x8C, 0x00, 30);
    static final Color C_GREEN = new Color(0x27, 0xAE, 0x60);
    static final Color C_GREEN_BG = new Color(0x27, 0xAE, 0x60, 30);
    static final Color C_RED = new Color(0xE7, 0x4C, 0x3C);

    public PlacesPanel(Student student) {
        this.student = student;
        setBackground(tm.getBg());
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
    }

    private void buildUI() {
        // ── Fila 1: titulo + boton agregar ───────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 12, 0));

        JLabel lblTitle = new JLabel("Lugares APUNAB");
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        lblTitle.setForeground(tm.getText());

        JLabel lblSub = new JLabel("Lugares donde puedes ganar o apostar APUNAB");
        lblSub.setFont(new Font("Dialog", Font.PLAIN, 13));
        lblSub.setForeground(tm.getMuted());

        JPanel titleWrap = new JPanel();
        titleWrap.setLayout(new BoxLayout(titleWrap, BoxLayout.Y_AXIS));
        titleWrap.setOpaque(false);
        titleWrap.add(lblTitle);
        titleWrap.add(Box.createVerticalStrut(4));
        titleWrap.add(lblSub);

        JButton btnAdd = styledButton("+ Agregar lugar", C_ORANGE, Color.WHITE);
        btnAdd.addActionListener(e -> showAddDialog());

        header.add(titleWrap, BorderLayout.WEST);
        header.add(btnAdd, BorderLayout.EAST);

        // ── Fila 2: busqueda + categoria + favoritos ─────────────────────────
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filterRow.setOpaque(false);
        filterRow.setBorder(new EmptyBorder(0, 0, 14, 0));

        searchField = new JTextField(18);
        searchField.setFont(new Font("Dialog", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(tm.getBorder(), 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        searchField.setToolTipText("Buscar por nombre");
        // Placeholder
        searchField.setText("Buscar lugar...");
        searchField.setForeground(tm.getMuted());
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                if (searchField.getText().equals("Buscar lugar...")) {
                    searchField.setText("");
                    searchField.setForeground(tm.getText());
                }
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Buscar lugar...");
                    searchField.setForeground(tm.getMuted());
                }
            }
        });
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                refreshCards();
            }

            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                refreshCards();
            }

            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                refreshCards();
            }
        });

        categoryFilter = new JComboBox<>(CATEGORIES);
        categoryFilter.setFont(new Font("Dialog", Font.PLAIN, 12));
        categoryFilter.addActionListener(e -> refreshCards());

        JButton btnFav = styledButton("Favoritos", new Color(0xEE, 0xEE, 0xEE), tm.getMuted());
        btnFav.addActionListener(e -> {
            showFavOnly = !showFavOnly;
            if (showFavOnly) {
                btnFav.setForeground(C_ORANGE);
            } else {
                btnFav.setForeground(tm.getMuted());
            }
            refreshCards();
        });

        filterRow.add(searchField);
        filterRow.add(categoryFilter);
        filterRow.add(btnFav);

        // ── Encabezado compuesto ──────────────────────────────────────────────
        JPanel topSection = new JPanel();
        topSection.setLayout(new BoxLayout(topSection, BoxLayout.Y_AXIS));
        topSection.setOpaque(false);
        topSection.add(header);
        topSection.add(filterRow);

        // ── Contenedor de tarjetas con scroll ────────────────────────────────
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);

        refreshCards();

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(topSection, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /** Reconstruye la lista de tarjetas de lugares aplicando filtros activos. */
    private void refreshCards() {
        cardsContainer.removeAll();

        // Leer filtros actuales
        String query = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        if (query.equals("buscar lugar..."))
            query = "";
        String catSel = categoryFilter != null ? (String) categoryFilter.getSelectedItem() : "Todas";

        List<Place> allPlaces = dm.getPlaces();
        final String finalQuery = query;

        for (Place place : allPlaces) {
            // Filtro de texto
            if (!finalQuery.isEmpty() && !place.getName().toLowerCase().contains(finalQuery))
                continue;
            // Filtro de categoria
            if (!"Todas".equals(catSel) && !catSel.equals(place.getCategory()))
                continue;
            // Filtro de favoritos
            if (showFavOnly && !dm.isFavorite(student.getCode(), place.getId()))
                continue;

            cardsContainer.add(buildPlaceCard(place));
            cardsContainer.add(Box.createVerticalStrut(10));
        }

        cardsContainer.revalidate();
        cardsContainer.repaint();
    }

    // ── Tarjeta individual de lugar ──────────────────────────────────────────

    private JPanel buildPlaceCard(Place place) {
        boolean enrolled = place.isEnrolled(student.getCode());

        JPanel card = new JPanel(new BorderLayout(16, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 14, 14);
                // Fondo
                g2.setColor(tm.getSurface());
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 14, 14);
                // Barra lateral segun estado
                Color accent = enrolled ? C_GREEN : C_ORANGE;
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 5, getHeight() - 2, 14, 14);
                g2.fillRect(0, 7, 5, getHeight() - 9);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(18, 22, 18, 18));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Info del lugar
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.setOpaque(false);

        JLabel lblName = new JLabel(place.getName());
        lblName.setFont(new Font("Dialog", Font.BOLD, 15));
        lblName.setForeground(tm.getText());

        JLabel lblDesc = new JLabel(place.getDescription());
        lblDesc.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblDesc.setForeground(tm.getMuted());

        JLabel lblCount = new JLabel(place.getEnrollmentCount() + " estudiantes inscritos");
        lblCount.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblCount.setForeground(tm.getMuted());

        JLabel lblCategory = new JLabel(place.getCategory());
        lblCategory.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblCategory.setForeground(C_ORANGE);

        info.add(lblName);
        info.add(Box.createVerticalStrut(4));
        info.add(lblDesc);
        info.add(Box.createVerticalStrut(3));
        info.add(lblCount);
        info.add(Box.createVerticalStrut(2));
        info.add(lblCategory);

        // Botones de accion
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        // Estrella de favorito
        boolean isFav = dm.isFavorite(student.getCode(), place.getId());
        JButton btnStar = styledButton(isFav ? "★" : "☆", new Color(0xEE, 0xEE, 0xEE),
                isFav ? new Color(0xFF, 0xB3, 0x00) : tm.getMuted());
        btnStar.addActionListener(e -> {
            dm.toggleFavorite(student.getCode(), place.getId());
            refreshCards();
        });

        if (enrolled) {
            JButton btnLeave = styledButton("Darse de baja", new Color(0xEE, 0xEE, 0xEE), C_RED);
            btnLeave.addActionListener(e -> {
                dm.leavePlace(student.getCode(), place.getId());
                refreshCards();
            });
            JLabel lblStatus = new JLabel("Inscrito ");
            lblStatus.setFont(new Font("Dialog", Font.BOLD, 11));
            lblStatus.setForeground(C_GREEN);
            actions.add(lblStatus);
            actions.add(btnLeave);
        } else {
            JButton btnJoin = styledButton("Inscribirse", C_ORANGE, Color.WHITE);
            btnJoin.addActionListener(e -> {
                dm.enrollInPlace(student.getCode(), place.getId());
                refreshCards();
            });
            actions.add(btnJoin);
        }

        JButton btnDel = styledButton("Eliminar", new Color(0xEE, 0xEE, 0xEE), C_RED);
        btnDel.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "Eliminar " + place.getName() + "?", "Confirmar",
                    JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                dm.deletePlace(place.getId());
                refreshCards();
            }
        });
        actions.add(btnStar);
        actions.add(btnDel);

        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    // ── Dialogo para agregar lugar ──────────────────────────────────────────

    private void showAddDialog() {
        JTextField fieldName = new JTextField();
        JTextField fieldDesc = new JTextField();
        String[] catOptions = Arrays.copyOfRange(CATEGORIES, 1, CATEGORIES.length); // sin "Todas"
        JComboBox<String> comboCategory = new JComboBox<>(catOptions);

        Object[] fields = {
                "Nombre del lugar:", fieldName,
                "Descripcion:", fieldDesc,
                "Categoria:", comboCategory
        };

        int result = JOptionPane.showConfirmDialog(this, fields,
                "Agregar lugar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = fieldName.getText().trim();
            String desc = fieldDesc.getText().trim();
            String cat = (String) comboCategory.getSelectedItem();
            if (!name.isEmpty()) {
                String id = "P" + String.format("%03d", dm.getPlaces().size() + 1);
                dm.addPlace(new Place(id, name, desc.isEmpty() ? "Sin descripcion" : desc, cat));
                refreshCards();
            }
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
        btn.setFont(new Font("Dialog", Font.BOLD, 11));
        btn.setForeground(fg);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 14, 8, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
