package hospital.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class representing a Patient's Medical Record.
 */
public class MedicalRecord {
    private String recordId;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private LocalDate visitDate;
    private String symptoms;
    private String diagnosis;
    private String prescription;
    private String labTestsRecommended;
    private String notes;

    public MedicalRecord(String recordId, String patientId, String doctorId, String doctorName,
                         LocalDate visitDate, String symptoms, String diagnosis,
                         String prescription, String labTestsRecommended, String notes) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.visitDate = visitDate != null ? visitDate : LocalDate.now();
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.labTestsRecommended = labTestsRecommended != null ? labTestsRecommended : "None";
        this.notes = notes != null ? notes : "";
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public String getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getPrescription() {
        return prescription;
    }

    public void setPrescription(String prescription) {
        this.prescription = prescription;
    }

    public String getLabTestsRecommended() {
        return labTestsRecommended;
    }

    public void setLabTestsRecommended(String labTestsRecommended) {
        this.labTestsRecommended = labTestsRecommended;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MedicalRecord that = (MedicalRecord) o;
        return Objects.equals(recordId, that.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }

    @Override
    public String toString() {
        return "Record " + recordId + " [Patient: " + patientId + ", Date: " + visitDate + ", Diagnosis: " + diagnosis + "]";
    }
}
