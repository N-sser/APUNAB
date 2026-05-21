package main.data;

import main.model.Student;
import main.util.Security;

import java.util.HashMap;
import java.util.Map;

public class DataManager {

    private static DataManager instance;

    private final Map<String, Student> students = new HashMap<>();

    private DataManager() {
        seedData();
    }

    public static DataManager getInstance() {
        if (instance == null)
            instance = new DataManager();
        return instance;
    }

    // ── Lookup ────────────────────────────────────────────────────────────────

    /** Returns the Student if credentials match, null otherwise. */
    public Student login(String code, String rawPassword) {
        Student student = students.get(code);
        if (student == null)
            return null;
        String hash = Security.hashPassword(code, rawPassword);
        return hash.equals(student.getPasswordHash()) ? student : null;
    }

    public Student findByCode(String code) {
        return students.get(code);
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /** Returns false if code is already taken. */
    public boolean register(String code, String name, String semester, String rawPassword) {
        if (students.containsKey(code))
            return false;
        String hash = Security.hashPassword(code, rawPassword);
        students.put(code, new Student(code, name, semester, 0L, hash));
        return true;
    }

    // ── Seed ─────────────────────────────────────────────────────────────────

    /** One demo student */
    private void seedData() {
        String code = "U00123456";
        String hash = Security.hashPassword(code, "1234");
        students.put(code, new Student(code, "Juan Perez Gomez", "1th Semester", 45_230L, hash));
    }
}