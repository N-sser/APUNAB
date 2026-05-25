package main.ui;

import main.data.DataManager;
import main.model.Place;
import main.model.Student;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Panel de gestion de lugares APUNAB.
 * Muestra todos los lugares en tarjetas con opcion de inscribirse o darse de baja.
 * Incluye CRUD basico: agregar, editar, eliminar lugares.
 */
public class PlacesPanel extends JPanel {

    private final Student student;
    private final DataManager dm = DataManager.getInstance();
    private JPanel cardsContainer;

    static final Color C_ORANGE    = new Color(0xFF, 0x8C, 0x00);
    static final Color C_ORANGE_BG = new Color(0xFF, 0x8C, 0x00, 30);
    static final Color C_BG        = new Color(0xF5, 0xF5, 0xF5);
    static final Color C_WHITE     = Color.WHITE;
    static final Color C_TEXT      = new Color(0x1A, 0x1A, 0x1A);
    static final Color C_MUTED     = new Color(0x88, 0x88, 0x88);
    static final Color C_BORDER    = new Color(0xE8, 0xE8, 0xE8);
    static final Color C_GREEN     = new Color(0x27, 0xAE, 0x60);
    static final Color C_GREEN_BG  = new Color(0x27, 0xAE, 0x60, 30);
    static final Color C_RED       = new Color(0xE7, 0x4C, 0x3C);

    public PlacesPanel(Student student) {
        this.student = student;
        setBackground(C_BG);
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(28, 28, 28, 28));
        buildUI();
    }

    private void buildUI() {
        // Encabezado
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel lblTitle = new JLabel("Lugares APUNAB");
        lblTitle.setFont(new Font("Dialog", Font.BOLD, 22));
        lblTitle.setForeground(C_TEXT);

        JLabel lblSub = new JLabel("Lugares donde puedes ganar o apostar APUNAB");
        lblSub.setFont(new Font("Dialog", Font.PLAIN, 13));
        lblSub.setForeground(C_MUTED);

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

        // Contenedor de tarjetas con scroll
        cardsContainer = new JPanel();
        cardsContainer.setLayout(new BoxLayout(cardsContainer, BoxLayout.Y_AXIS));
        cardsContainer.setOpaque(false);

        refreshCards();

        JScrollPane scroll = new JScrollPane(cardsContainer);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    /** Reconstruye la lista de tarjetas de lugares. */
    private void refreshCards() {
        cardsContainer.removeAll();
        List<Place> allPlaces = dm.getPlaces();

        for (Place place : allPlaces) {
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
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Sombra
                g2.setColor(new Color(0, 0, 0, 10));
                g2.fillRoundRect(2, 3, getWidth() - 2, getHeight() - 2, 14, 14);
                // Fondo
                g2.setColor(C_WHITE);
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
        lblName.setForeground(C_TEXT);

        JLabel lblDesc = new JLabel(place.getDescription());
        lblDesc.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblDesc.setForeground(C_MUTED);

        JLabel lblCount = new JLabel(place.getEnrollmentCount() + " estudiantes inscritos");
        lblCount.setFont(new Font("Dialog", Font.PLAIN, 11));
        lblCount.setForeground(C_MUTED);

        info.add(lblName);
        info.add(Box.createVerticalStrut(4));
        info.add(lblDesc);
        info.add(Box.createVerticalStrut(3));
        info.add(lblCount);

        // Botones de accion
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);

        if (enrolled) {
            JButton btnLeave = styledButton("Darse de baja", new Color(0xEE,0xEE,0xEE), C_RED);
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

        JButton btnDel = styledButton("Eliminar", new Color(0xEE,0xEE,0xEE), C_RED);
        btnDel.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                "Eliminar " + place.getName() + "?", "Confirmar",
                JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                dm.deletePlace(place.getId());
                refreshCards();
            }
        });
        actions.add(btnDel);

        card.add(info, BorderLayout.CENTER);
        card.add(actions, BorderLayout.EAST);
        return card;
    }

    // ── Dialogo para agregar lugar ──────────────────────────────────────────

    private void showAddDialog() {
        JTextField fieldName = new JTextField();
        JTextField fieldDesc = new JTextField();

        Object[] fields = {
            "Nombre del lugar:", fieldName,
            "Descripcion:", fieldDesc
        };

        int result = JOptionPane.showConfirmDialog(this, fields,
            "Agregar lugar", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String name = fieldName.getText().trim();
            String desc = fieldDesc.getText().trim();
            if (!name.isEmpty()) {
                String id = "P" + String.format("%03d", dm.getPlaces().size() + 1);
                dm.addPlace(new Place(id, name, desc.isEmpty() ? "Sin descripcion" : desc));
                refreshCards();
            }
        }
    }

    // ── Boton reutilizable ──────────────────────────────────────────────────

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
