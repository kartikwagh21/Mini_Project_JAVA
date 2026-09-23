package hospital.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Model class representing a Billing Record / Invoice in the Hospital System.
 */
public class BillingRecord {
    private String billId;
    private String patientId;
    private String patientName;
    private String appointmentId;
    private double consultationFee;
    private double labCharges;
    private double medicineCharges;
    private double roomCharges;
    private double taxRatePercent; // e.g. 5%
    private double discountAmount;
    private double totalAmount;
    private String paymentStatus; // "Paid", "Pending", "Cancelled"
    private String paymentMethod; // "Cash", "Credit Card", "Insurance", "UPI", "None"
    private LocalDate billingDate;
    private LocalDate paymentDate;

    public BillingRecord(String billId, String patientId, String patientName, String appointmentId,
                         double consultationFee, double labCharges, double medicineCharges,
                         double roomCharges, double taxRatePercent, double discountAmount,
                         String paymentStatus, String paymentMethod, LocalDate billingDate) {
        this.billId = billId;
        this.patientId = patientId;
        this.patientName = patientName;
        this.appointmentId = appointmentId != null ? appointmentId : "N/A";
        this.consultationFee = consultationFee;
        this.labCharges = labCharges;
        this.medicineCharges = medicineCharges;
        this.roomCharges = roomCharges;
        this.taxRatePercent = taxRatePercent;
        this.discountAmount = discountAmount;
        this.paymentStatus = paymentStatus != null ? paymentStatus : "Pending";
        this.paymentMethod = paymentMethod != null ? paymentMethod : "None";
        this.billingDate = billingDate != null ? billingDate : LocalDate.now();
        recalculateTotal();
        if ("Paid".equalsIgnoreCase(this.paymentStatus)) {
            this.paymentDate = this.billingDate;
        }
    }

    public void recalculateTotal() {
        double subtotal = consultationFee + labCharges + medicineCharges + roomCharges;
        double tax = subtotal * (taxRatePercent / 100.0);
        this.totalAmount = Math.max(0.0, (subtotal + tax) - discountAmount);
    }

    public String getBillId() {
        return billId;
    }

    public void setBillId(String billId) {
        this.billId = billId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public double getConsultationFee() {
        return consultationFee;
    }

    public void setConsultationFee(double consultationFee) {
        this.consultationFee = consultationFee;
        recalculateTotal();
    }

    public double getLabCharges() {
        return labCharges;
    }

    public void setLabCharges(double labCharges) {
        this.labCharges = labCharges;
        recalculateTotal();
    }

    public double getMedicineCharges() {
        return medicineCharges;
    }

    public void setMedicineCharges(double medicineCharges) {
        this.medicineCharges = medicineCharges;
        recalculateTotal();
    }

    public double getRoomCharges() {
        return roomCharges;
    }

    public void setRoomCharges(double roomCharges) {
        this.roomCharges = roomCharges;
        recalculateTotal();
    }

    public double getTaxRatePercent() {
        return taxRatePercent;
    }

    public void setTaxRatePercent(double taxRatePercent) {
        this.taxRatePercent = taxRatePercent;
        recalculateTotal();
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        recalculateTotal();
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
        if ("Paid".equalsIgnoreCase(paymentStatus) && this.paymentDate == null) {
            this.paymentDate = LocalDate.now();
        }
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public LocalDate getBillingDate() {
        return billingDate;
    }

    public void setBillingDate(LocalDate billingDate) {
        this.billingDate = billingDate;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public double getSubtotal() {
        return consultationFee + labCharges + medicineCharges + roomCharges;
    }

    public double getTaxAmount() {
        return getSubtotal() * (taxRatePercent / 100.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BillingRecord that = (BillingRecord) o;
        return Objects.equals(billId, that.billId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(billId);
    }

    @Override
    public String toString() {
        return billId + " - Patient: " + patientName + " Total: ₹" + String.format("%.2f", totalAmount) + " [" + paymentStatus + "]";
    }
}
