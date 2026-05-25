package main.model;

import java.util.ArrayList;
import java.util.List;

public class Place {

    // ── Fields ────────────────────────────────────────────────────────────────
    private String id;
    private String name;
    private String description;
    private List<String> enrolledStudentCodes; // students currently registered here

    // ── Constructor ───────────────────────────────────────────────────────────
    public Place(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.enrolledStudentCodes = new ArrayList<>();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getEnrolledStudentCodes() {
        return enrolledStudentCodes;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // ── Enrollment ────────────────────────────────────────────────────────────
    public boolean enroll(String studentCode) {
        if (enrolledStudentCodes.contains(studentCode))
            return false;
        enrolledStudentCodes.add(studentCode);
        return true;
    }

    public boolean leave(String studentCode) {
        return enrolledStudentCodes.remove(studentCode);
    }

    public boolean isEnrolled(String studentCode) {
        return enrolledStudentCodes.contains(studentCode);
    }

    public int getEnrollmentCount() {
        return enrolledStudentCodes.size();
    }

    @Override
    public String toString() {
        return String.format("Lugar[%s | %s | matriculado=%d]", id, name, enrolledStudentCodes.size());
    }
}