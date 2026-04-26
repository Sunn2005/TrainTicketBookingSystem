package iuh.fit.gui.ticket.confirm;

import dto.SellRoundTripRequest;
import dto.SellTicketRequest;
import iuh.fit.context.UserContext;
import iuh.fit.context.TicketContext;
import iuh.fit.context.TicketContext.PassengerInfo;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class PaymentFlowSupport {
    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(Locale.of("vi", "VN"));

    private PaymentFlowSupport() {
    }

    static double calculateTotal(TicketContext ctx) {
        double total = 0;
        double dist = ctx.getDistance();
        List<PassengerInfo> passengers = ctx.getPassengers();

        for (int i = 0; i < ctx.getOutboundSeats().size(); i++) {
            Seat seat = ctx.getOutboundSeats().get(i);
            CustomerType type = i < passengers.size() ? passengers.get(i).getType() : CustomerType.ADULT;
            total += TicketContext.calcPrice(dist, seat.getSeatType(), type);
        }

        for (int i = 0; i < ctx.getReturnSeats().size(); i++) {
            Seat seat = ctx.getReturnSeats().get(i);
            CustomerType type = i < passengers.size() ? passengers.get(i).getType() : CustomerType.ADULT;
            total += TicketContext.calcPrice(dist, seat.getSeatType(), type);
        }

        return total;
    }

    static SellTicketRequest buildRequest(TicketContext ctx, boolean qrPayment) {
        String sellerId = UserContext.getInstance().getUserID();
        List<SellTicketRequest.TicketDetail> details = new ArrayList<>();

        for (int i = 0; i < ctx.getOutboundSeats().size(); i++) {
            PassengerInfo p = i < ctx.getPassengers().size() ? ctx.getPassengers().get(i) : null;
            details.add(new SellTicketRequest.TicketDetail(
                    ctx.getOutboundSchedule().getScheduleId(),
                    ctx.getOutboundSeats().get(i).getSeatID(),
                    p != null ? safe(p.getName()) : "",
                    p != null ? safe(p.getCccd()) : "",
                    p != null ? p.getType() : CustomerType.ADULT
            ));
        }

        if (ctx.getReturnSchedule() != null) {
            for (int i = 0; i < ctx.getReturnSeats().size(); i++) {
                PassengerInfo p = i < ctx.getPassengers().size() ? ctx.getPassengers().get(i) : null;
                details.add(new SellTicketRequest.TicketDetail(
                        ctx.getReturnSchedule().getScheduleId(),
                        ctx.getReturnSeats().get(i).getSeatID(),
                        p != null ? safe(p.getName()) : "",
                        p != null ? safe(p.getCccd()) : "",
                        p != null ? p.getType() : CustomerType.ADULT
                ));
            }
        }
        SellTicketRequest req =  new SellTicketRequest(sellerId != null ? sellerId : "", qrPayment, details);
        System.out.println("ticket sell request: " + req.getTickets());
        return req;
    }

    static SellRoundTripRequest buildRoundTripRequest(TicketContext ctx, boolean qrPayment) {
        String sellerId = UserContext.getInstance().getUserID();
        List<SellRoundTripRequest.TicketDetail> outbound = new ArrayList<>();
        List<SellRoundTripRequest.TicketDetail> inbound = new ArrayList<>();

        for (int i = 0; i < ctx.getOutboundSeats().size(); i++) {
            PassengerInfo p = i < ctx.getPassengers().size() ? ctx.getPassengers().get(i) : null;
            outbound.add(new SellRoundTripRequest.TicketDetail(
                    ctx.getOutboundSchedule().getScheduleId(),
                    ctx.getOutboundSeats().get(i).getSeatID(),
                    p != null ? safe(p.getName()) : "",
                    p != null ? safe(p.getCccd()) : "",
                    p != null ? p.getType() : CustomerType.ADULT
            ));
        }
        if (ctx.getReturnSchedule() != null) {
            for (int i = 0; i < ctx.getReturnSeats().size(); i++) {
                PassengerInfo p = i < ctx.getPassengers().size() ? ctx.getPassengers().get(i) : null;
                inbound.add(new SellRoundTripRequest.TicketDetail(
                        ctx.getReturnSchedule().getScheduleId(),
                        ctx.getReturnSeats().get(i).getSeatID(),
                        p != null ? safe(p.getName()) : "",
                        p != null ? safe(p.getCccd()) : "",
                        p != null ? p.getType() : CustomerType.ADULT
                ));
            }
        }

        SellRoundTripRequest req = new SellRoundTripRequest(
                sellerId != null ? sellerId : "",
                qrPayment,
                outbound,
                inbound
        );
        System.out.println("ticket sell round-trip request: " + req.getOutboundTickets().size() + " outbound, " + req.getReturnTickets().size() + " return");
        return req;
    }

    static String money(double value) {
        return CURRENCY.format((long) value) + " đ";
    }

    static String seatTypeName(SeatType type) {
        return type == SeatType.SOFT_SLEEPER ? "Giường mềm" : "Ghế mềm";
    }

    static String customerTypeName(CustomerType type) {
        if (type == null) return "Người lớn";
        return switch (type) {
            case CHILD -> "Trẻ em";
            case STUDENT -> "Sinh viên";
            case ELDERLY -> "Người cao tuổi";
            default -> "Người lớn";
        };
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}

