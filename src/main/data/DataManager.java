package main.data;

import main.model.Bet;
import main.model.Bet.Result;
import main.model.Place;
import main.model.Student;
import main.util.Security;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private static DataManager instance;

    private final Map<String, Student> students = new HashMap<>();
    private final List<Bet> bets = new ArrayList<>();
    private final Map<String, Place> places = new LinkedHashMap<>();
    private final Map<String, Set<String>> favorites = new HashMap<>();

    public static final long GRADUATION_GOAL = 100_000L;

    // Delimitador para archivos de datos (pipe para evitar conflictos con comas en
    // texto)
    private static final String D = "|";

    // ═══════════════════════════════════════════════════════════════════════════
    // INICIALIZACION
    // Si existen archivos de datos, los carga. Si no, usa datos de prueba.
    // ═══════════════════════════════════════════════════════════════════════════

    private DataManager() {
        if (dataFilesExist()) {
            loadAll();
        } else {
            seedData();
            saveAll();
        }
    }

    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // RUTA DE DATOS
    // Linux: ~/.local/share/apunab/
    // Windows: %APPDATA%/APUNAB/
    // ═══════════════════════════════════════════════════════════════════════════

    private static Path getDataDir() {
        String os = System.getProperty("os.name").toLowerCase();
        Path dir;
        if (os.contains("win")) {
            String appdata = System.getenv("APPDATA");
            dir = (appdata != null)
                    ? Path.of(appdata, "APUNAB")
                    : Path.of(System.getProperty("user.home"), "APUNAB");
        } else {
            dir = Path.of(System.getProperty("user.home"), ".local", "share", "apunab");
        }
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            System.err.println("No se pudo crear directorio de datos: " + dir);
        }
        return dir;
    }

    private static Path studentsFile() {
        return getDataDir().resolve("students.txt");
    }

    private static Path betsFile() {
        return getDataDir().resolve("bets.txt");
    }

    private static Path placesFile() {
        return getDataDir().resolve("places.txt");
    }

    private static Path favoritesFile() {
        return getDataDir().resolve("favorites.txt");
    }

    private boolean dataFilesExist() {
        return Files.exists(studentsFile()) && Files.exists(betsFile()) && Files.exists(placesFile());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // AUTH
    // ═══════════════════════════════════════════════════════════════════════════

    public Student login(String code, String rawPassword) {
        Student student = students.get(code);
        if (student == null)
            return null;
        String hash = Security.hashPassword(code, rawPassword);
        return hash.equals(student.getPasswordHash()) ? student : null;
    }

    public boolean register(String code, String email, String name, String rawPassword) {
        if (students.containsKey(code))
            return false;
        String hash = Security.hashPassword(code, rawPassword);
        students.put(code, new Student(code, email, name, "1st Semester", 0L, hash, ""));
        saveAll();
        return true;
    }

    public Student findByCode(String code) {
        return students.get(code);
    }

    /** Actualiza email y telefono del estudiante y persiste. */
    public boolean updateStudentContact(String code, String email, String phone) {
        Student s = students.get(code);
        if (s == null)
            return false;
        s.setEmail(email);
        s.setPhone(phone);
        saveAll();
        return true;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // APUESTAS CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    public void addBet(Bet bet) {
        bets.add(bet);
        syncBalance(bet.getStudentCode());
        saveAll();
    }

    public boolean updateBet(Bet updated) {
        for (int i = 0; i < bets.size(); i++) {
            if (bets.get(i).getId().equals(updated.getId())) {
                bets.set(i, updated);
                syncBalance(updated.getStudentCode());
                saveAll();
                return true;
            }
        }
        return false;
    }

    public boolean deleteBet(String betId) {
        Bet target = bets.stream()
                .filter(b -> b.getId().equals(betId))
                .findFirst().orElse(null);
        if (target == null)
            return false;
        bets.remove(target);
        syncBalance(target.getStudentCode());
        saveAll();
        return true;
    }

    public List<Bet> getBetsByStudent(String studentCode) {
        return bets.stream()
                .filter(b -> b.getStudentCode().equals(studentCode))
                .collect(Collectors.toList());
    }

    public Bet findBetById(String id) {
        return bets.stream()
                .filter(b -> b.getId().equals(id))
                .findFirst().orElse(null);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // ESTADISTICAS APUNAB
    // ═══════════════════════════════════════════════════════════════════════════

    public long getBalance(String studentCode) {
        return getBetsByStudent(studentCode).stream()
                .mapToLong(Bet::getAmount)
                .sum();
    }

    public long getApunabNeeded(String studentCode) {
        return GRADUATION_GOAL - getBalance(studentCode);
    }

    /** Total de APUNAB en la semana calendario actual. */
    public long getWeeklyTotal(String studentCode) {
        int currentWeek = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int currentYear = LocalDate.now().getYear();
        return getBetsByStudent(studentCode).stream()
                .filter(b -> b.getDate().getYear() == currentYear)
                .filter(b -> b.getDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == currentWeek)
                .mapToLong(Bet::getAmount)
                .sum();
    }

    /** Total de APUNAB en el mes calendario actual. */
    public long getMonthlyTotal(String studentCode) {
        LocalDate now = LocalDate.now();
        return getBetsByStudent(studentCode).stream()
                .filter(b -> b.getDate().getYear() == now.getYear())
                .filter(b -> b.getDate().getMonth() == now.getMonth())
                .mapToLong(Bet::getAmount)
                .sum();
    }

    /**
     * Total de APUNAB en el semestre actual.
     * Semestre 1 = enero-junio, Semestre 2 = julio-diciembre.
     */
    public long getSemesterTotal(String studentCode) {
        LocalDate now = LocalDate.now();
        int month = now.getMonthValue();
        int semStart = (month <= 6) ? 1 : 7;
        int semEnd = (month <= 6) ? 6 : 12;
        return getBetsByStudent(studentCode).stream()
                .filter(b -> b.getDate().getYear() == now.getYear())
                .filter(b -> {
                    int m = b.getDate().getMonthValue();
                    return m >= semStart && m <= semEnd;
                })
                .mapToLong(Bet::getAmount)
                .sum();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // LUGARES CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    public List<Place> getPlaces() {
        return new ArrayList<>(places.values());
    }

    public Place findPlaceById(String id) {
        return places.get(id);
    }

    public void addPlace(Place place) {
        places.put(place.getId(), place);
        saveAll();
    }

    public boolean updatePlace(Place updated) {
        if (!places.containsKey(updated.getId()))
            return false;
        places.put(updated.getId(), updated);
        saveAll();
        return true;
    }

    public boolean deletePlace(String placeId) {
        if (places.remove(placeId) == null)
            return false;
        saveAll();
        return true;
    }

    public boolean enrollInPlace(String studentCode, String placeId) {
        Place place = places.get(placeId);
        if (place == null)
            return false;
        boolean ok = place.enroll(studentCode);
        if (ok)
            saveAll();
        return ok;
    }

    public boolean leavePlace(String studentCode, String placeId) {
        Place place = places.get(placeId);
        if (place == null)
            return false;
        boolean ok = place.leave(studentCode);
        if (ok)
            saveAll();
        return ok;
    }

    public List<Place> getEnrolledPlaces(String studentCode) {
        return places.values().stream()
                .filter(p -> p.isEnrolled(studentCode))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // FAVORITOS
    // ═══════════════════════════════════════════════════════════════════════════

    public boolean isFavorite(String studentCode, String placeId) {
        return favorites.getOrDefault(studentCode, Collections.emptySet()).contains(placeId);
    }

    public void toggleFavorite(String studentCode, String placeId) {
        Set<String> fav = favorites.computeIfAbsent(studentCode, k -> new HashSet<>());
        if (!fav.remove(placeId))
            fav.add(placeId);
        saveFavorites();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTERNO
    // ═══════════════════════════════════════════════════════════════════════════

    /** Recalcula y guarda el balance del estudiante a partir de sus apuestas. */
    private void syncBalance(String studentCode) {
        Student student = students.get(studentCode);
        if (student != null)
            student.setApunabBalance(getBalance(studentCode));
    }

    /** Genera un ID secuencial simple: B001, B002, etc. */
    public String nextBetId() {
        return String.format("B%03d", bets.size() + 1);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSISTENCIA — GUARDAR
    // Formato: texto plano delimitado por pipe (|), un registro por linea.
    //
    // students.txt: codigo|email|nombre|semestre|balance|hashContrasena
    // bets.txt: id|codigoEstudiante|idLugar|monto|fecha|resultado
    // places.txt: id|nombre|descripcion|inscritos (separados por coma)
    // ═══════════════════════════════════════════════════════════════════════════

    private void saveAll() {
        saveStudents();
        saveBets();
        savePlaces();
        saveFavorites();
    }

    private void saveStudents() {
        try (BufferedWriter w = Files.newBufferedWriter(studentsFile())) {
            for (Student s : students.values()) {
                w.write(String.join(D,
                        s.getCode(),
                        s.getEmail(),
                        s.getName(),
                        s.getSemester(),
                        Long.toString(s.getApunabBalance()),
                        s.getPasswordHash(),
                        s.getPhone()));
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error guardando estudiantes: " + e.getMessage());
        }
    }

    private void saveBets() {
        try (BufferedWriter w = Files.newBufferedWriter(betsFile())) {
            for (Bet b : bets) {
                w.write(String.join(D,
                        b.getId(),
                        b.getStudentCode(),
                        b.getPlaceId(),
                        Long.toString(b.getAmount()),
                        b.getDate().toString(), // formato ISO: 2026-05-25
                        b.getResult().name() // WON, LOST, PENDING
                ));
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error guardando apuestas: " + e.getMessage());
        }
    }

    private void savePlaces() {
        try (BufferedWriter w = Files.newBufferedWriter(placesFile())) {
            for (Place p : places.values()) {
                String enrolled = String.join(",", p.getEnrolledStudentCodes());
                w.write(String.join(D,
                        p.getId(),
                        p.getName(),
                        p.getDescription(),
                        p.getCategory(),
                        enrolled));
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error guardando lugares: " + e.getMessage());
        }
    }

    private void saveFavorites() {
        try (BufferedWriter w = Files.newBufferedWriter(favoritesFile())) {
            for (Map.Entry<String, Set<String>> e : favorites.entrySet()) {
                if (e.getValue().isEmpty())
                    continue;
                w.write(e.getKey() + D + String.join(",", e.getValue()));
                w.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error guardando favoritos: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PERSISTENCIA — CARGAR
    // ═══════════════════════════════════════════════════════════════════════════

    private void loadAll() {
        students.clear();
        bets.clear();
        places.clear();
        favorites.clear();

        loadStudents();
        loadBets();
        loadPlaces();
        loadFavorites();

        for (String code : students.keySet()) {
            syncBalance(code);
        }
    }

    private void loadStudents() {
        try {
            for (String line : Files.readAllLines(studentsFile())) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 5)
                    continue;

                String code = parts[0];
                String email = parts[1];
                String name = parts[2];
                String semester = parts[3];
                long balance = Long.parseLong(parts[4]);
                String hash = parts.length > 5 ? parts[5] : "";
                String phone = parts.length > 6 ? parts[6] : "";

                students.put(code, new Student(code, email, name, semester, balance, hash, phone));
            }
        } catch (IOException e) {
            System.err.println("Error cargando estudiantes: " + e.getMessage());
        }
    }

    private void loadBets() {
        try {
            for (String line : Files.readAllLines(betsFile())) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 6)
                    continue;

                String id = parts[0];
                String studentCode = parts[1];
                String placeId = parts[2];
                long amount = Long.parseLong(parts[3]);
                LocalDate date = LocalDate.parse(parts[4]);
                Result result = Result.valueOf(parts[5]);

                bets.add(new Bet(id, studentCode, placeId, amount, date, result));
            }
        } catch (IOException e) {
            System.err.println("Error cargando apuestas: " + e.getMessage());
        }
    }

    private void loadPlaces() {
        try {
            for (String line : Files.readAllLines(placesFile())) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 4)
                    continue;

                String id = parts[0];
                String name = parts[1];
                String description = parts[2];
                // Backward compat: old format has 4 fields (no category)
                String category;
                String enrolledRaw;
                if (parts.length >= 5) {
                    category = parts[3];
                    enrolledRaw = parts[4];
                } else {
                    category = "Otro";
                    enrolledRaw = parts[3];
                }

                Place place = new Place(id, name, description, category);

                if (!enrolledRaw.isEmpty()) {
                    for (String code : enrolledRaw.split(",", -1)) {
                        code = code.trim();
                        if (!code.isEmpty())
                            place.enroll(code);
                    }
                }

                places.put(id, place);
            }
        } catch (IOException e) {
            System.err.println("Error cargando lugares: " + e.getMessage());
        }
    }

    private void loadFavorites() {
        if (!Files.exists(favoritesFile()))
            return;
        try {
            for (String line : Files.readAllLines(favoritesFile())) {
                line = line.trim();
                if (line.isEmpty())
                    continue;
                String[] parts = line.split("\\|", -1);
                if (parts.length < 2)
                    continue;
                Set<String> fav = new HashSet<>();
                for (String id : parts[1].split(",", -1)) {
                    id = id.trim();
                    if (!id.isEmpty())
                        fav.add(id);
                }
                favorites.put(parts[0], fav);
            }
        } catch (IOException e) {
            System.err.println("Error cargando favoritos: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DATOS DE PRUEBA
    // Solo se ejecuta en la primera ejecucion (cuando no existen archivos).
    // Crea un estudiante demo, 5 lugares UNAB y 17 apuestas de ejemplo.
    // ID del estudiante de prueba: "U00123456"
    // Contrasena del estudiante de prueba: "1234"
    // ═══════════════════════════════════════════════════════════════════════════

    private void seedData() {
        String code = "U00123456";
        String hash = Security.hashPassword(code, "1234");
        students.put(code,
                new Student(code, "jperez123@unab.edu.co", "Juan Perez Gomez", "1st Semester", 0L, hash, ""));

        Place p1 = new Place("P001", "Cafeteria L", "Cafeteria del bloque L", "Cafeteria");
        Place p2 = new Place("P002", "Cafeteria CSU", "Centro Social Universitario", "Cafeteria");
        Place p3 = new Place("P003", "Cafeteria Bosque", "Cafeteria zona del Bosque", "Cafeteria");
        Place p4 = new Place("P004", "Cafeteria Casona", "Cafeteria La Casona", "Cafeteria");
        Place p5 = new Place("P005", "Banu", "Punto Banu campus principal", "Actividades");

        p1.enroll(code);
        p3.enroll(code);
        p5.enroll(code);

        places.put(p1.getId(), p1);
        places.put(p2.getId(), p2);
        places.put(p3.getId(), p3);
        places.put(p4.getId(), p4);
        places.put(p5.getId(), p5);

        // Apuestas distribuidas en el semestre actual
        LocalDate base = LocalDate.now().withDayOfMonth(1);

        addSeedBet("B001", code, "P001", 5_000L, base.minusMonths(4).withDayOfMonth(5), Result.WON);
        addSeedBet("B002", code, "P002", -1_500L, base.minusMonths(4).withDayOfMonth(12), Result.LOST);
        addSeedBet("B003", code, "P003", 3_200L, base.minusMonths(4).withDayOfMonth(20), Result.WON);
        addSeedBet("B004", code, "P001", 4_500L, base.minusMonths(3).withDayOfMonth(3), Result.WON);
        addSeedBet("B005", code, "P005", -2_000L, base.minusMonths(3).withDayOfMonth(10), Result.LOST);
        addSeedBet("B006", code, "P003", 6_000L, base.minusMonths(3).withDayOfMonth(18), Result.WON);
        addSeedBet("B007", code, "P002", 2_800L, base.minusMonths(3).withDayOfMonth(25), Result.WON);
        addSeedBet("B008", code, "P004", -1_200L, base.minusMonths(2).withDayOfMonth(7), Result.LOST);
        addSeedBet("B009", code, "P001", 7_500L, base.minusMonths(2).withDayOfMonth(14), Result.WON);
        addSeedBet("B010", code, "P005", 3_000L, base.minusMonths(2).withDayOfMonth(21), Result.WON);
        addSeedBet("B011", code, "P003", -3_500L, base.minusMonths(1).withDayOfMonth(4), Result.LOST);
        addSeedBet("B012", code, "P001", 8_000L, base.minusMonths(1).withDayOfMonth(11), Result.WON);
        addSeedBet("B013", code, "P002", 4_200L, base.minusMonths(1).withDayOfMonth(19), Result.WON);
        addSeedBet("B014", code, "P005", -2_800L, base.withDayOfMonth(5), Result.LOST);
        addSeedBet("B015", code, "P003", 5_530L, base.withDayOfMonth(12), Result.WON);
        addSeedBet("B016", code, "P001", 2_730L, base.withDayOfMonth(18), Result.WON);
        addSeedBet("B017", code, "P005", 2_500L, LocalDate.now(), Result.PENDING);

        syncBalance(code);
    }

    /** Agrega una apuesta directamente sin disparar syncBalance ni saveAll. */
    private void addSeedBet(String id, String studentCode, String placeId,
            long amount, LocalDate date, Result result) {
        bets.add(new Bet(id, studentCode, placeId, amount, date, result));
    }
}