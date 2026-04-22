//package iuh.fit.gui.ticket;
//
//import dto.ScheduleInfoResponse;
//import model.entity.Seat;
//import model.entity.enums.CustomerType;
//import model.entity.enums.SeatType;
//
//import java.time.LocalDate;
//import java.util.ArrayList;
//import java.util.List;
//
//public final class TicketContext {
//    private static final TicketContext INSTANCE = new TicketContext();
//
//    private static final double PRICE_PER_DISTANCE = 1000.0;
//    private static final double SOFT_SEAT_FEE = 300000.0;
//    private static final double SOFT_SLEEPER_FEE = 500000.0;
//
//    private String departureStationId;
//    private String departureStationName;
//    private String arrivalStationId;
//    private String arrivalStationName;
//    private LocalDate departureDate;
//    private LocalDate returnDate;
//    private boolean roundTrip;
//    private boolean qrPayment;
//
//    private ScheduleInfoResponse outboundSchedule;
//    private ScheduleInfoResponse returnSchedule;
//
//    private final List<Seat> outboundSeats = new ArrayList<>();
//    private final List<Seat> returnSeats = new ArrayList<>();
//    private final List<PassengerInfo> passengers = new ArrayList<>();
//
//    private TicketContext() {
//    }
//
//    public static TicketContext getInstance() {
//        return INSTANCE;
//    }
//
//    public void reset() {
//        departureStationId = null;
//        departureStationName = null;
//        arrivalStationId = null;
//        arrivalStationName = null;
//        departureDate = null;
//        returnDate = null;
//        roundTrip = false;
//        qrPayment = false;
//        outboundSchedule = null;
//        returnSchedule = null;
//        outboundSeats.clear();
//        returnSeats.clear();
//        passengers.clear();
//    }
//
//    public double getDistance() {
//        // Keep a safe default so UI price calculation still works without route distance data.
//        return 1.0;
//    }
//
//    public static double calcPrice(double distance, SeatType seatType, CustomerType customerType) {
//        double seatFee = seatType == SeatType.SOFT_SLEEPER ? SOFT_SLEEPER_FEE : SOFT_SEAT_FEE;
//        double base = distance * PRICE_PER_DISTANCE + seatFee;
//        return base * discount(customerType);
//    }
//
//    private static double discount(CustomerType customerType) {
//        if (customerType == null) {
//            return 1.0;
//        }
//        return switch (customerType) {
//            case STUDENT -> 0.9;
//            case CHILD -> 0.75;
//            case ELDERLY -> 0.85;
//            default -> 1.0;
//        };
//    }
//
//    public String getDepartureStationId() {
//        return departureStationId;
//    }
//
//    public void setDepartureStationId(String departureStationId) {
//        this.departureStationId = departureStationId;
//    }
//
//    public String getDepartureStationName() {
//        return departureStationName;
//    }
//
//    public void setDepartureStationName(String departureStationName) {
//        this.departureStationName = departureStationName;
//    }
//
//    public String getArrivalStationId() {
//        return arrivalStationId;
//    }
//
//    public void setArrivalStationId(String arrivalStationId) {
//        this.arrivalStationId = arrivalStationId;
//    }
//
//    public String getArrivalStationName() {
//        return arrivalStationName;
//    }
//
//    public void setArrivalStationName(String arrivalStationName) {
//        this.arrivalStationName = arrivalStationName;
//    }
//
//    public LocalDate getDepartureDate() {
//        return departureDate;
//    }
//
//    public void setDepartureDate(LocalDate departureDate) {
//        this.departureDate = departureDate;
//    }
//
//    public LocalDate getReturnDate() {
//        return returnDate;
//    }
//
//    public void setReturnDate(LocalDate returnDate) {
//        this.returnDate = returnDate;
//    }
//
//    public boolean isRoundTrip() {
//        return roundTrip;
//    }
//
//    public void setRoundTrip(boolean roundTrip) {
//        this.roundTrip = roundTrip;
//    }
//
//    public boolean isQrPayment() {
//        return qrPayment;
//    }
//
//    public void setQrPayment(boolean qrPayment) {
//        this.qrPayment = qrPayment;
//    }
//
//    public ScheduleInfoResponse getOutboundSchedule() {
//        return outboundSchedule;
//    }
//
//    public void setOutboundSchedule(ScheduleInfoResponse outboundSchedule) {
//        this.outboundSchedule = outboundSchedule;
//    }
//
//    public ScheduleInfoResponse getReturnSchedule() {
//        return returnSchedule;
//    }
//
//    public void setReturnSchedule(ScheduleInfoResponse returnSchedule) {
//        this.returnSchedule = returnSchedule;
//    }
//
//    public List<Seat> getOutboundSeats() {
//        return outboundSeats;
//    }
//
//    public List<Seat> getReturnSeats() {
//        return returnSeats;
//    }
//
//    public List<PassengerInfo> getPassengers() {
//        return passengers;
//    }
//
//    public static final class PassengerInfo {
//        private String name = "";
//        private String cccd = "";
//        private CustomerType type = CustomerType.ADULT;
//        private Seat outboundSeat;
//        private Seat returnSeat;
//
//        public PassengerInfo(Seat outboundSeat) {
//            this.outboundSeat = outboundSeat;
//        }
//
//        public String getName() {
//            return name;
//        }
//
//        public void setName(String name) {
//            this.name = name;
//        }
//
//        public String getCccd() {
//            return cccd;
//        }
//
//        public void setCccd(String cccd) {
//            this.cccd = cccd;
//        }
//
//        public CustomerType getType() {
//            return type;
//        }
//
//        public void setType(CustomerType type) {
//            this.type = type;
//        }
//
//        public Seat getOutboundSeat() {
//            return outboundSeat;
//        }
//
//        public void setOutboundSeat(Seat outboundSeat) {
//            this.outboundSeat = outboundSeat;
//        }
//
//        public Seat getReturnSeat() {
//            return returnSeat;
//        }
//
//        public void setReturnSeat(Seat returnSeat) {
//            this.returnSeat = returnSeat;
//        }
//
//        public double calcOutboundPrice(double distance) {
//            if (outboundSeat == null) {
//                return 0.0;
//            }
//            return calcPrice(distance, outboundSeat.getSeatType(), type);
//        }
//
//        public double calcReturnPrice(double distance) {
//            if (returnSeat == null) {
//                return 0.0;
//            }
//            return calcPrice(distance, returnSeat.getSeatType(), type);
//        }
//
//        public double calcTotalPrice(double distance) {
//            return calcOutboundPrice(distance) + calcReturnPrice(distance);
//        }
//    }
//}
//
