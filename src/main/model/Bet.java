package main.model;

import java.time.LocalDate;

public class Bet {

    // ── Fields ────────────────────────────────────────────────────────────────
    private String id;
    private String studentCode; // Quién hizo la apuesta
    private String placeId; // Donde va suceder
    private long amount; // APUNAB apostado (positivo = ganado, negativo = perdido)
    private LocalDate date;
    private Result result;

    // ── Result enum ───────────────────────────────────────────────────────────
    public enum Result {
        WON, LOST, PENDING
    }

    // ── Constructor ───────────────────────────────────────────────────────────
    public Bet(String id, String studentCode, String placeId, long amount, LocalDate date, Result result) {
        this.id = id;
        this.studentCode = studentCode;
        this.placeId = placeId;
        this.amount = amount;
        this.date = date;
        this.result = result;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public String getPlaceId() {
        return placeId;
    }

    public long getAmount() {
        return amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public Result getResult() {
        return result;
    }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setResult(Result result) {
        this.result = result;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return String.format("Bet[%s | student=%s | place=%s | amount=%d | %s | %s]",
                id, studentCode, placeId, amount, date, result);
    }
}