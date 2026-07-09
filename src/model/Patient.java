package model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedList;

public class Patient implements Serializable {
    private static final long serialVersionUID = 1L;

    private int patientId;
    private String name;
    private int age;
    private String gender;
    private String phoneNumber;
    private LocalDate registrationDate;
    private LinkedList<VisitRecord> visitHistory;

    public Patient(int patientId, String name, int age, String gender, String phoneNumber, LocalDate registrationDate) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.phoneNumber = phoneNumber;
        this.registrationDate = registrationDate;
        this.visitHistory = new LinkedList<>();
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public LinkedList<VisitRecord> getVisitHistory() {
        return visitHistory;
    }

    public void setVisitHistory(LinkedList<VisitRecord> visitHistory) {
        this.visitHistory = visitHistory;
    }

    public void addVisitRecord(VisitRecord record) {
        this.visitHistory.add(record);
    }

    @Override
    public String toString() {
        return "Patient ID: " + patientId +
               "\nName: " + name +
               "\nAge: " + age +
               "\nGender: " + gender +
               "\nPhone Number: " + phoneNumber +
               "\nRegistration Date: " + registrationDate +
               "\nTotal Visits: " + (visitHistory != null ? visitHistory.size() : 0);
    }
}
