package main.data;

import main.model.Bet;
import main.model.Bet.Result;
import main.model.Place;
import main.model.Student;
import main.util.Security;

import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.util.*;
import java.util.stream.Collectors;

public class DataManager {

    private static DataManager instance;

    private final Map<String, Student> students = new HashMap<>();
    private final List<Bet> bets = new ArrayList<>();
    private final Map<String, Place> places = new LinkedHashMap<>();

    public static final long GRADUATION_GOAL = 100_000L;

    private DataManager() {
        seedData();
    }

    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
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

    public boolean register(String code, String name, String rawPassword) {
        if (students.containsKey(code))
            return false;
        String hash = Security.hashPassword(code, rawPassword);
        students.put(code, new Student(code, name, "1st Semester", 0L, hash));
        return true;
    }

    public Student findByCode(String code) {
        return students.get(code);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // BET CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    public void addBet(Bet bet) {
        bets.add(bet);
        syncBalance(bet.getStudentCode());
    }

    public boolean updateBet(Bet updated) {
        for (int i = 0; i < bets.size(); i++) {
            if (bets.get(i).getId().equals(updated.getId())) {
                bets.set(i, updated);
                syncBalance(updated.getStudentCode());
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
    // APUNAB STATISTICS
    // ═══════════════════════════════════════════════════════════════════════════

    public long getBalance(String studentCode) {
        return getBetsByStudent(studentCode).stream()
                .mapToLong(Bet::getAmount)
                .sum();
    }

    public long getApunabNeeded(String studentCode) {
        return GRADUATION_GOAL - getBalance(studentCode);
    }

    public long getWeeklyTotal(String studentCode) {
        int currentWeek = LocalDate.now().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        int currentYear = LocalDate.now().getYear();
        return getBetsByStudent(studentCode).stream()
                .filter(b -> b.getDate().getYear() == currentYear)
                .filter(b -> b.getDate().get(IsoFields.WEEK_OF_WEEK_BASED_YEAR) == currentWeek)
                .mapToLong(Bet::getAmount)
                .sum();
    }

    public long getMonthlyTotal(String studentCode) {
        LocalDate now = LocalDate.now();
        return getBetsByStudent(studentCode).stream()
                .filter(b -> b.getDate().getYear() == now.getYear())
                .filter(b -> b.getDate().getMonth() == now.getMonth())
                .mapToLong(Bet::getAmount)
                .sum();
    }

    /**
     * Semester 1 = January-June, Semester 2 = July-December.
     * Since students only do one semester this is effectively their full history,
     * but the date filter keeps it correct if seed data spans multiple semesters.
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
    // PLACE CRUD
    // ═══════════════════════════════════════════════════════════════════════════

    public List<Place> getPlaces() {
        return new ArrayList<>(places.values());
    }

    public Place findPlaceById(String id) {
        return places.get(id);
    }

    public void addPlace(Place place) {
        places.put(place.getId(), place);
    }

    public boolean updatePlace(Place updated) {
        if (!places.containsKey(updated.getId()))
            return false;
        places.put(updated.getId(), updated);
        return true;
    }

    public boolean deletePlace(String placeId) {
        return places.remove(placeId) != null;
    }

    public boolean enrollInPlace(String studentCode, String placeId) {
        Place place = places.get(placeId);
        if (place == null)
            return false;
        return place.enroll(studentCode);
    }

    public boolean leavePlace(String studentCode, String placeId) {
        Place place = places.get(placeId);
        if (place == null)
            return false;
        return place.leave(studentCode);
    }

    public List<Place> getEnrolledPlaces(String studentCode) {
        return places.values().stream()
                .filter(p -> p.isEnrolled(studentCode))
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INTERNAL
    // ═══════════════════════════════════════════════════════════════════════════

    /** Recomputes and stores balance from bets. Called after every mutation. */
    private void syncBalance(String studentCode) {
        Student student = students.get(studentCode);
        if (student != null)
            student.setApunabBalance(getBalance(studentCode));
    }

    /** Generates a simple sequential ID: B001, B002, etc. */
    public String nextBetId() {
        return String.format("B%03d", bets.size() + 1);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // SEED
    // One student, five real UNAB places, realistic bets over one semester.
    // Bets net to ~45,230 APUNAB total.
    // ═══════════════════════════════════════════════════════════════════════════

    private void seedData() {
        // Student — password is "1234"
        String code = "U00123456";
        String hash = Security.hashPassword(code, "1234");
        students.put(code, new Student(code, "Juan Perez Gomez", "1st Semester", 0L, hash));

        // The five real UNAB places
        Place p1 = new Place("P001", "Cafeteria L", "Cafeteria del bloque L");
        Place p2 = new Place("P002", "Cafeteria CSU", "Centro Social Universitario");
        Place p3 = new Place("P003", "Cafeteria Bosque", "Cafeteria zona del Bosque");
        Place p4 = new Place("P004", "Cafeteria Casona", "Cafeteria La Casona");
        Place p5 = new Place("P005", "Banu", "Punto Banu campus principal");

        // Juan is enrolled in three of them
        p1.enroll(code);
        p3.enroll(code);
        p5.enroll(code);

        places.put(p1.getId(), p1);
        places.put(p2.getId(), p2);
        places.put(p3.getId(), p3);
        places.put(p4.getId(), p4);
        places.put(p5.getId(), p5);

        // Bets spread across the current semester
        // Positive = won, negative = lost. Total nets to ~45,230.
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

    /** Adds a bet directly to the list without triggering syncBalance mid-seed. */
    private void addSeedBet(String id, String studentCode, String placeId,
            long amount, LocalDate date, Result result) {
        bets.add(new Bet(id, studentCode, placeId, amount, date, result));
    }
}