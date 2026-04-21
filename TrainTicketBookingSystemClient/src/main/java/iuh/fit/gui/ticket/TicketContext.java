package iuh.fit.gui.ticket;

import dto.ScheduleInfoResponse;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TicketContext {

    private static final TicketContext INSTANCE = new TicketContext();
    public static TicketContext getInstance() { return INSTANCE; }
    private TicketContext() {}

    // ── Giá ─────────────────────────────────────────────────────────
    private static final double PRICE_PER_KM     = 1000;
    private static final double SOFT_SEAT_FEE    = 300_000;
    private static final double SOFT_SLEEPER_FEE = 500_000;
    private static final double CHILD_DISCOUNT   = 0.75;
    private static final double STUDENT_DISCOUNT = 0.9;
    private static final double ELDERLY_DISCOUNT = 0.85;

    public static double calcPrice(double distance, SeatType seatType, CustomerType type) {
        double seatFee = switch (seatType) {
            case SOFT_SEAT    -> SOFT_SEAT_FEE;
            case SOFT_SLEEPER -> SOFT_SLEEPER_FEE;
        };
        double base = distance * PRICE_PER_KM + seatFee;
        if (type == null) return base;
        return switch (type) {
            case CHILD   -> base * CHILD_DISCOUNT;
            case STUDENT -> base * STUDENT_DISCOUNT;
            case ELDERLY -> base * ELDERLY_DISCOUNT;
            default      -> base;
        };
    }

    // ── Tìm kiếm ────────────────────────────────────────────────────
    private String departureStationId;
    private String departureStationName;
    private String arrivalStationId;
    private String arrivalStationName;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private boolean roundTrip;

    // ── Chuyến đi ───────────────────────────────────────────────────
    private ScheduleInfoResponse outboundSchedule;
    private final List<Seat> outboundSeats = new ArrayList<>();

    // ── Chuyến về ───────────────────────────────────────────────────
    private ScheduleInfoResponse returnSchedule;
    private final List<Seat> returnSeats = new ArrayList<>();

    // ── Hành khách ──────────────────────────────────────────────────
    private final List<PassengerInfo> passengers = new ArrayList<>();

    // ── Thanh toán ───────────────────────────────────────────────────
    private boolean qrPayment = false;

    public void reset() {
        departureStationId = departureStationName = null;
        arrivalStationId   = arrivalStationName   = null;
        departureDate = returnDate = null;
        roundTrip = false;
        outboundSchedule = null;
        returnSchedule   = null;
        outboundSeats.clear();
        returnSeats.clear();
        passengers.clear();
        qrPayment = false;
    }

    // ── Getters / Setters ────────────────────────────────────────────
    public String getDepartureStationId()                    { return departureStationId; }
    public void   setDepartureStationId(String v)            { departureStationId = v; }
    public String getDepartureStationName()                  { return departureStationName; }
    public void   setDepartureStationName(String v)          { departureStationName = v; }
    public String getArrivalStationId()                      { return arrivalStationId; }
    public void   setArrivalStationId(String v)              { arrivalStationId = v; }
    public String getArrivalStationName()                    { return arrivalStationName; }
    public void   setArrivalStationName(String v)            { arrivalStationName = v; }
    public LocalDate getDepartureDate()                      { return departureDate; }
    public void      setDepartureDate(LocalDate v)           { departureDate = v; }
    public LocalDate getReturnDate()                         { return returnDate; }
    public void      setReturnDate(LocalDate v)              { returnDate = v; }
    public boolean   isRoundTrip()                           { return roundTrip; }
    public void      setRoundTrip(boolean v)                 { roundTrip = v; }
    public ScheduleInfoResponse getOutboundSchedule()        { return outboundSchedule; }
    public void setOutboundSchedule(ScheduleInfoResponse v)  { outboundSchedule = v; }
    public List<Seat> getOutboundSeats()                     { return outboundSeats; }
    public ScheduleInfoResponse getReturnSchedule()          { return returnSchedule; }
    public void setReturnSchedule(ScheduleInfoResponse v)    { returnSchedule = v; }
    public List<Seat> getReturnSeats()                       { return returnSeats; }
    public List<PassengerInfo> getPassengers()               { return passengers; }
    public boolean isQrPayment()                             { return qrPayment; }
    public void    setQrPayment(boolean v)                   { qrPayment = v; }

    // ── Helper: lấy distance ─────────────────────────────────────────
    public double getDistance() {
        // ScheduleInfoResponse chưa có distance → dùng route từ schedule
        // Tạm dùng 500km, sau khi server thêm distance sẽ update
        return 500.0;
    }

    // ── PassengerInfo ────────────────────────────────────────────────
    public static class PassengerInfo {
        private String name = "";
        private String cccd = "";
        private CustomerType type = CustomerType.ADULT;
        private Seat outboundSeat;
        private Seat returnSeat;

        public PassengerInfo(Seat outboundSeat) { this.outboundSeat = outboundSeat; }

        public String getName()             { return name; }
        public void   setName(String v)     { name = v; }
        public String getCccd()             { return cccd; }
        public void   setCccd(String v)     { cccd = v; }
        public CustomerType getType()       { return type; }
        public void setType(CustomerType v) { type = v; }
        public Seat getOutboundSeat()       { return outboundSeat; }
        public void setOutboundSeat(Seat v) { outboundSeat = v; }
        public Seat getReturnSeat()         { return returnSeat; }
        public void setReturnSeat(Seat v)   { returnSeat = v; }

        public double calcOutboundPrice(double dist) {
            return calcPrice(dist, outboundSeat.getSeatType(), type);
        }
        public double calcReturnPrice(double dist) {
            return returnSeat != null ? calcPrice(dist, returnSeat.getSeatType(), type) : 0;
        }
        public double calcTotalPrice(double dist) {
            return calcOutboundPrice(dist) + calcReturnPrice(dist);
        }
    }
}