package iuh.fit.context;

import dto.ScheduleInfoResponse;
import model.entity.BasePrice;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;
import iuh.fit.service.PriceClientService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TicketContext {
    public enum BookingStep {
        OUTBOUND_SEARCH, OUTBOUND_SEAT, RETURN_SEARCH, RETURN_SEAT, PASSENGER_INFO, CONFIRM, PAYMENT
    }

    private static final TicketContext INSTANCE = new TicketContext();

    private BasePrice basePrice;
    private BookingStep currentStep = BookingStep.OUTBOUND_SEARCH;

    private String departureStationId;
    private String departureStationName;
    private String arrivalStationId;
    private String arrivalStationName;
    private LocalDate departureDate;
    private LocalDate returnDate;
    private boolean roundTrip;
    private boolean qrPayment;

    private ScheduleInfoResponse outboundSchedule;
    private ScheduleInfoResponse returnSchedule;

    private final List<Seat> outboundSeats = new ArrayList<>();
    private final List<Seat> returnSeats = new ArrayList<>();
    private final List<PassengerInfo> passengers = new ArrayList<>();

    private double currentDistance = 1.0;

    private TicketContext() {
        try {
            PriceClientService pcs = new PriceClientService();
            this.basePrice = pcs.getBasePrice();
        } catch (Exception e) {
            this.basePrice = new BasePrice(1000.0, 300000.0, 500000.0, 0.9, 0.85, 0.75);
        }
    }

    public static TicketContext getInstance() {
        return INSTANCE;
    }

    public void reset() {
        departureStationId = null;
        departureStationName = null;
        arrivalStationId = null;
        arrivalStationName = null;
        departureDate = null;
        returnDate = null;
        roundTrip = false;
        qrPayment = false;
        outboundSchedule = null;
        returnSchedule = null;
        outboundSeats.clear();
        returnSeats.clear();
        passengers.clear();
        currentStep = BookingStep.OUTBOUND_SEARCH;
    }

    public double getDistance() {
        // Return current distance from selected schedule
        return currentDistance;
    }

    public static double calcPrice(double distance, SeatType seatType, CustomerType customerType) {
        return getInstance().calculatePrice(distance, seatType, customerType);
    }

    private double calculatePrice(double distance, SeatType seatType, CustomerType customerType) {
        if (basePrice == null) {
            return 0.0;
        }
        double seatFee = seatType == SeatType.SOFT_SLEEPER
                ? basePrice.getSoftSleeperFee() : basePrice.getSoftSeatFee();
        // Giữ đúng công thức: distance * pricePerDistance + seatFee,
        // Nhưng nếu frontend không lấy được distance, ta tính Base tạm:
        double base = (distance > 0 ? distance : 1) * basePrice.getPricePerDistance() + seatFee;

        // "áp dụng ưu đãi đối với customerType"
        // Lúc chọn ghế thì TicketContext.calcPrice được gọi truyền vào `null` vì chưa biết customerType.
        return base * discount(customerType);
    }

    private double discount(CustomerType customerType) {
        if (customerType == null || basePrice == null) {
            return 1.0;
        }
        return switch (customerType) {
            case STUDENT -> basePrice.getStudentDiscount();
            case CHILD -> basePrice.getChildDiscount();
            case ELDERLY -> basePrice.getElderlyDiscount();
            default -> 1.0;
        };
    }

    public double getDiscountRate(CustomerType customerType) {
        return discount(customerType);
    }

    public String getDepartureStationId() {
        return departureStationId;
    }

    public void setDepartureStationId(String departureStationId) {
        this.departureStationId = departureStationId;
    }

    public String getDepartureStationName() {
        return departureStationName;
    }

    public void setDepartureStationName(String departureStationName) {
        this.departureStationName = departureStationName;
    }

    public String getArrivalStationId() {
        return arrivalStationId;
    }

    public void setArrivalStationId(String arrivalStationId) {
        this.arrivalStationId = arrivalStationId;
    }

    public String getArrivalStationName() {
        return arrivalStationName;
    }

    public void setArrivalStationName(String arrivalStationName) {
        this.arrivalStationName = arrivalStationName;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public boolean isRoundTrip() {
        return roundTrip;
    }

    public void setRoundTrip(boolean roundTrip) {
        this.roundTrip = roundTrip;
    }

    public boolean isQrPayment() {
        return qrPayment;
    }

    public void setQrPayment(boolean qrPayment) {
        this.qrPayment = qrPayment;
    }

    public ScheduleInfoResponse getOutboundSchedule() {
        return outboundSchedule;
    }

    public void setOutboundSchedule(ScheduleInfoResponse outboundSchedule) {
        this.outboundSchedule = outboundSchedule;
    }

    public ScheduleInfoResponse getReturnSchedule() {
        return returnSchedule;
    }

    public void setReturnSchedule(ScheduleInfoResponse returnSchedule) {
        this.returnSchedule = returnSchedule;
    }

    public List<Seat> getOutboundSeats() {
        return outboundSeats;
    }

    public List<Seat> getReturnSeats() {
        return returnSeats;
    }

    public List<PassengerInfo> getPassengers() {
        return passengers;
    }

    public void setCurrentDistance(double currentDistance) {
        this.currentDistance = currentDistance;
    }

    public BookingStep getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(BookingStep currentStep) {
        this.currentStep = currentStep;
    }

    public static final class PassengerInfo {
        private String name = "";
        private String cccd = "";
        private CustomerType type = CustomerType.ADULT;
        private Seat outboundSeat;
        private Seat returnSeat;

        public PassengerInfo(Seat outboundSeat) {
            this.outboundSeat = outboundSeat;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCccd() {
            return cccd;
        }

        public void setCccd(String cccd) {
            this.cccd = cccd;
        }

        public CustomerType getType() {
            return type;
        }

        public void setType(CustomerType type) {
            this.type = type;
        }

        public Seat getOutboundSeat() {
            return outboundSeat;
        }

        public void setOutboundSeat(Seat outboundSeat) {
            this.outboundSeat = outboundSeat;
        }

        public Seat getReturnSeat() {
            return returnSeat;
        }

        public void setReturnSeat(Seat returnSeat) {
            this.returnSeat = returnSeat;
        }

        public double calcOutboundPrice(double distance) {
            if (outboundSeat == null) {
                return 0.0;
            }
            return calcPrice(distance, outboundSeat.getSeatType(), type);
        }

        public double calcReturnPrice(double distance) {
            if (returnSeat == null) {
                return 0.0;
            }
            return calcPrice(distance, returnSeat.getSeatType(), type);
        }

        public double calcTotalPrice(double distance) {
            return calcOutboundPrice(distance) + calcReturnPrice(distance);
        }
    }
    // ── Exchange mode ─────────────────────────────────────────────────────────
    private boolean exchangeMode     = false;
    private String  exchangeTicketId = "";

    public boolean isExchangeMode()              { return exchangeMode; }
    public void    setExchangeMode(boolean v)    { this.exchangeMode = v; }
    public String  getExchangeTicketId()         { return exchangeTicketId; }
    public void    setExchangeTicketId(String v) { this.exchangeTicketId = v; }
}

