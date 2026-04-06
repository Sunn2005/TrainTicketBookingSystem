package dto;
public class BasePrice {
    private double pricePerDistance;
    private double studentDiscount;
    private double childDiscount;
    private double elderlyDiscount;
    public double getPricePerDistance() { return pricePerDistance; }
    public void setPricePerDistance(double pricePerDistance) { this.pricePerDistance = pricePerDistance; }
    public double getStudentDiscount() { return studentDiscount; }
    public void setStudentDiscount(double studentDiscount) { this.studentDiscount = studentDiscount; }
    public double getChildDiscount() { return childDiscount; }
    public void setChildDiscount(double childDiscount) { this.childDiscount = childDiscount; }
    public double getElderlyDiscount() { return elderlyDiscount; }
    public void setElderlyDiscount(double elderlyDiscount) { this.elderlyDiscount = elderlyDiscount; }
}
