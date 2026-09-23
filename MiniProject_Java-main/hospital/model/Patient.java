package hospital.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class representing a Patient entity in the Hospital System.
 */
public class Patient implements Comparable<Patient> {
    private String id;
    private String name;
    private int age;
    private String gender;
    private String contactNumber;
    private String bloodGroup;
    private String address;
    private LocalDate registrationDate;
    private String emergencyContact;
    private String bloodPressure;
    private String allergies;

    public Patient(String id, String name, int age, String gender, String contactNumber,
                   String bloodGroup, String address, LocalDate registrationDate,
                   String emergencyContact, String bloodPressure, String allergies) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.bloodGroup = bloodGroup;
        this.address = address;
        this.registrationDate = registrationDate != null ? registrationDate : LocalDate.now();
        this.emergencyContact = emergencyContact;
        this.bloodPressure = bloodPressure != null ? bloodPressure : "120/80";
        this.allergies = allergies != null ? allergies : "None";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getAllergies() {
        return allergies;
    }

    public void setAllergies(String allergies) {
        this.allergies = allergies;
    }

    @Override
    public int compareTo(Patient other) {
        if (other == null) return 1;
        // Default comparison by name, then by ID
        int cmp = this.name.compareToIgnoreCase(other.name);
        if (cmp != 0) return cmp;
        return this.id.compareTo(other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(id, patient.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return name + " (" + id + ", Age: " + age + ", Blood: " + bloodGroup + ")";
    }
}
