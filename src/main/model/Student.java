package main.model;

public class Student {
    private String code;
    private String email;
    private String name;
    private String semester;
    private long apunabBalance;
    private String passwordHash;
    private String phone;

    public Student(String code, String email, String name, String semester,
            long apunabBalance, String passwordHash, String phone) {
        this.code = code;
        this.email = email;
        this.name = name;
        this.semester = semester;
        this.apunabBalance = apunabBalance;
        this.passwordHash = passwordHash;
        this.phone = phone;
    }

    /** Constructor de compatibilidad hacia atras (sin telefono). */
    public Student(String code, String email, String name, String semester,
            long apunabBalance, String passwordHash) {
        this(code, email, name, semester, apunabBalance, passwordHash, "");
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

    public String getPhone() {
        return phone;
    }

    public void setApunabBalance(long balance) {
        this.apunabBalance = balance;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}