package main.util;

import java.awt.Color;
import java.io.*;
import java.nio.file.*;

/**
 * Singleton que gestiona el tema visual de la aplicacion.
 * Modo LIGHT, DARK, o AUTO (detecta preferencia del SO).
 * La preferencia se persiste en theme.txt junto a los demas datos.
 */
public class ThemeManager {

    public enum Mode { LIGHT, DARK, AUTO }

    private static ThemeManager instance;
    private Mode mode = Mode.LIGHT;

    // ── Paleta clara ─────────────────────────────────────────────────────────
    public static final Color LIGHT_BG        = new Color(0xF5, 0xF5, 0xF5);
    public static final Color LIGHT_SURFACE   = Color.WHITE;
    public static final Color LIGHT_TEXT      = new Color(0x1A, 0x1A, 0x1A);
    public static final Color LIGHT_MUTED     = new Color(0x88, 0x88, 0x88);
    public static final Color LIGHT_BORDER    = new Color(0xE8, 0xE8, 0xE8);
    public static final Color LIGHT_DIVIDER   = new Color(0xEE, 0xEE, 0xEE);

    // ── Paleta oscura ─────────────────────────────────────────────────────────
    public static final Color DARK_BG         = new Color(0x12, 0x12, 0x1A);
    public static final Color DARK_SURFACE    = new Color(0x1E, 0x1E, 0x2A);
    public static final Color DARK_TEXT       = new Color(0xF0, 0xF0, 0xF0);
    public static final Color DARK_MUTED      = new Color(0x88, 0x88, 0x99);
    public static final Color DARK_BORDER     = new Color(0x2E, 0x2E, 0x3E);
    public static final Color DARK_DIVIDER    = new Color(0x2A, 0x2A, 0x3A);

    // ── Naranja compartido ────────────────────────────────────────────────────
    public static final Color C_ORANGE        = new Color(0xFF, 0x8C, 0x00);
    public static final Color C_ORANGE_BG     = new Color(0xFF, 0x8C, 0x00, 30);

    // ─────────────────────────────────────────────────────────────────────────

    private ThemeManager() {
        load();
    }

    public static ThemeManager getInstance() {
        if (instance == null) instance = new ThemeManager();
        return instance;
    }

    // ── API publica ───────────────────────────────────────────────────────────

    public Mode getMode() { return mode; }

    public void setMode(Mode mode) {
        this.mode = mode;
        save();
    }

    /** True si el tema activo (considerando AUTO) es oscuro. */
    public boolean isDark() {
        return mode == Mode.DARK || (mode == Mode.AUTO && detectOsDark());
    }

    // ── Accesores de color ────────────────────────────────────────────────────

    public Color getBg()      { return isDark() ? DARK_BG      : LIGHT_BG;      }
    public Color getSurface() { return isDark() ? DARK_SURFACE  : LIGHT_SURFACE; }
    public Color getText()    { return isDark() ? DARK_TEXT     : LIGHT_TEXT;    }
    public Color getMuted()   { return isDark() ? DARK_MUTED    : LIGHT_MUTED;   }
    public Color getBorder()  { return isDark() ? DARK_BORDER   : LIGHT_BORDER;  }
    public Color getDivider() { return isDark() ? DARK_DIVIDER  : LIGHT_DIVIDER; }

    // ── Deteccion del SO ──────────────────────────────────────────────────────

    /**
     * Intenta detectar si el SO esta usando tema oscuro.
     * Soporta macOS, Windows 10+ y escritorios Linux con gsettings (GNOME).
     * Retorna false si no puede determinarlo.
     */
    public boolean detectOsDark() {
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("mac")) {
                Process p = Runtime.getRuntime().exec(
                        new String[]{"defaults", "read", "-g", "AppleInterfaceStyle"});
                String out = new String(p.getInputStream().readAllBytes()).trim();
                return out.equalsIgnoreCase("Dark");

            } else if (os.contains("win")) {
                Process p = Runtime.getRuntime().exec(new String[]{
                        "reg", "query",
                        "HKCU\\Software\\Microsoft\\Windows\\CurrentVersion\\Themes\\Personalize",
                        "/v", "AppsUseLightTheme"});
                String out = new String(p.getInputStream().readAllBytes());
                // 0x0 = dark, 0x1 = light
                return out.contains("0x0");

            } else {
                // Linux / GNOME
                Process p = Runtime.getRuntime().exec(new String[]{
                        "gsettings", "get",
                        "org.gnome.desktop.interface", "color-scheme"});
                String out = new String(p.getInputStream().readAllBytes()).trim();
                return out.contains("dark");
            }
        } catch (Exception e) {
            return false; // Si falla, asumir claro
        }
    }

    // ── Persistencia ──────────────────────────────────────────────────────────

    private static Path themeFile() {
        String os = System.getProperty("os.name").toLowerCase();
        Path dir = os.contains("win")
                ? Path.of(System.getenv("APPDATA") != null
                        ? System.getenv("APPDATA") : System.getProperty("user.home"), "APUNAB")
                : Path.of(System.getProperty("user.home"), ".local", "share", "apunab");
        try { Files.createDirectories(dir); } catch (IOException ignored) {}
        return dir.resolve("theme.txt");
    }

    private void load() {
        try {
            if (Files.exists(themeFile())) {
                String saved = Files.readString(themeFile()).trim();
                mode = Mode.valueOf(saved);
            }
        } catch (Exception e) {
            mode = Mode.LIGHT;
        }
    }

    private void save() {
        try { Files.writeString(themeFile(), mode.name()); }
        catch (IOException e) { System.err.println("Error guardando tema: " + e.getMessage()); }
    }
}
