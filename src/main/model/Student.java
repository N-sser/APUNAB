package main.model;

public class Student {
    private String code;
    private String email;
    private String name;
    private String semester;
    private long apunabBalance;
    private String passwordHash;

    public Student(String code, String email, String name, String semester, long apunabBalance, String passwordHash) {
        this.code = code;
        this.email = email;
        this.name = name;
        this.semester = semester;
        this.apunabBalance = apunabBalance;
        this.passwordHash = passwordHash;
    }

    public String getCode() {
        return code;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSemester() {
        return semester;
    }

    public long getApunabBalance() {
        return apunabBalance;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setApunabBalance(long balance) {
        this.apunabBalance = balance;
    }
}