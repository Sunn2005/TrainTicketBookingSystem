package iuh.fit.util;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import dto.ScheduleInfoResponse;
import iuh.fit.context.TicketContext;
import iuh.fit.context.TicketContext.PassengerInfo;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;

import java.io.File;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class TicketPdfExporter {

    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");
    private static final DateTimeFormatter NOW_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private static final DeviceRgb COLOR_PRIMARY    = new DeviceRgb(21, 28, 53);
    private static final DeviceRgb COLOR_ACCENT     = new DeviceRgb(26, 109, 200);
    private static final DeviceRgb COLOR_LIGHT_BG   = new DeviceRgb(240, 246, 255);
    private static final DeviceRgb COLOR_BORDER     = new DeviceRgb(176, 200, 240);
    private static final DeviceRgb COLOR_SUCCESS    = new DeviceRgb(22, 163, 74);
    private static final DeviceRgb COLOR_TEXT_MUTED = new DeviceRgb(95, 111, 149);

    // Thư mục lưu PDF
    private static final String PDF_DIR =
            System.getProperty("user.dir") + File.separator + "PDF" + File.separator;

    private static final String[] REGULAR_FONT_CANDIDATES = {
            "/System/Library/Fonts/Supplemental/Arial.ttf",
            "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
            "/Library/Fonts/Arial Unicode.ttf",
            "/Library/Fonts/Arial.ttf",
            "C:/Windows/Fonts/arial.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Regular.ttf"
    };

    private static final String[] BOLD_FONT_CANDIDATES = {
            "/System/Library/Fonts/Supplemental/Arial Bold.ttf",
            "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
            "/Library/Fonts/Arial Unicode.ttf",
            "/Library/Fonts/Arial Bold.ttf",
            "C:/Windows/Fonts/arialbd.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
            "/usr/share/fonts/truetype/liberation/LiberationSans-Bold.ttf"
    };

    // ── Tạo font mới mỗi lần xuất để tránh lỗi "indirect object belongs to other PDF" ──
    private static PdfFont createRegularFont() {
                PdfFont font = createFontFromCandidates(REGULAR_FONT_CANDIDATES);
                if (font != null) return font;
                try {
                        return PdfFontFactory.createFont();
                } catch (Exception ex) {
                        throw new RuntimeException("Cannot load font", ex);
                }
    }

    private static PdfFont createBoldFont() {
                PdfFont font = createFontFromCandidates(BOLD_FONT_CANDIDATES);
                return font != null ? font : createRegularFont();
    }

        private static PdfFont createFontFromCandidates(String[] candidates) {
                for (String path : candidates) {
                        if (path == null || path.isBlank()) continue;
                        File f = new File(path);
                        if (!f.exists()) continue;
                        try {
                                return PdfFontFactory.createFont(f.getAbsolutePath(),
                                                PdfEncodings.IDENTITY_H,
                                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                        } catch (Exception ignored) {
                                // try next candidate
                        }
                }
                return null;
        }

    public static String exportAuto(TicketContext ctx, List<String> ticketIds)
            throws Exception {
        File dir = new File(PDF_DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = "ticket_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
                + ".pdf";
        String outputPath = PDF_DIR + fileName;
        export(ctx, ticketIds, outputPath);
        return outputPath;
    }

    public static void export(TicketContext ctx, List<String> ticketIds, String outputPath)
            throws Exception {

        File outFile = new File(outputPath);
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }

        // ── Tạo font MỚI cho mỗi lần xuất ──
        PdfFont fontRegular = createRegularFont();
        PdfFont fontBold    = createBoldFont();

        PdfWriter writer = new PdfWriter(outputPath);
        PdfDocument pdf  = new PdfDocument(writer);
        Document doc     = new Document(pdf, com.itextpdf.kernel.geom.PageSize.A4);
        doc.setMargins(32, 36, 32, 36);
        doc.setFont(fontRegular);

        // ── HEADER ──
        Table header = new Table(UnitValue.createPercentArray(new float[]{1, 2}))
                .useAllAvailableWidth();

        Cell logoCell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(0);
        logoCell.add(p("HỆ THỐNG ĐẶT VÉ TÀU", COLOR_PRIMARY, 14, true, fontBold, fontRegular));
        logoCell.add(p("Train Ticket Booking System", COLOR_TEXT_MUTED, 10, false, fontBold, fontRegular));

        Cell dateCell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(0)
                .setTextAlignment(TextAlignment.RIGHT);
        dateCell.add(p("HÓA ĐƠN VÉ TÀU", COLOR_ACCENT, 14, true, fontBold, fontRegular));
        dateCell.add(p("Ngày xuất: " + LocalDateTime.now().format(NOW_FMT),
                COLOR_TEXT_MUTED, 9, false, fontBold, fontRegular));

        header.addCell(logoCell);
        header.addCell(dateCell);
        doc.add(header);

        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setStrokeColor(COLOR_BORDER).setMarginTop(8).setMarginBottom(12));

        // ── MÃ VÉ ──
        if (ticketIds != null && !ticketIds.isEmpty()) {
            Table ticketIdTable = new Table(1).useAllAvailableWidth()
                    .setBackgroundColor(COLOR_LIGHT_BG)
                    .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(6));
            Cell tc = new Cell()
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(10);
            tc.add(p("Mã vé: " + String.join("  |  ", ticketIds),
                    COLOR_ACCENT, 11, true, fontBold, fontRegular));
            ticketIdTable.addCell(tc);
            doc.add(ticketIdTable);
            doc.add(new Paragraph("").setMarginBottom(10));
        }

        // ── CHIỀU ĐI ──
        if (!ctx.getOutboundSeats().isEmpty() && ctx.getOutboundSchedule() != null) {
            doc.add(buildSegmentSection(
                    "→  CHIỀU ĐI",
                    ctx.getOutboundSchedule(),
                    ctx.getOutboundSeats(),
                    ctx.getPassengers(),
                    ctx.getDistance(),
                    fontBold, fontRegular));
        }

        // ── CHIỀU VỀ ──
        if (!ctx.getReturnSeats().isEmpty() && ctx.getReturnSchedule() != null) {
            doc.add(new Paragraph("").setMarginBottom(8));
            doc.add(buildSegmentSection(
                    "←  CHIỀU VỀ",
                    ctx.getReturnSchedule(),
                    ctx.getReturnSeats(),
                    ctx.getPassengers(),
                    ctx.getDistance(),
                    fontBold, fontRegular));
        }

        doc.add(new Paragraph("").setMarginBottom(10));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setStrokeColor(COLOR_BORDER).setMarginBottom(10));

        // ── TỔNG TIỀN ──
        double total = 0;
        for (int i = 0; i < ctx.getOutboundSeats().size(); i++) {
            CustomerType t = i < ctx.getPassengers().size()
                    ? ctx.getPassengers().get(i).getType() : CustomerType.ADULT;
            total += TicketContext.calcPrice(
                    ctx.getDistance(), ctx.getOutboundSeats().get(i).getSeatType(), t);
        }
        for (int i = 0; i < ctx.getReturnSeats().size(); i++) {
            CustomerType t = i < ctx.getPassengers().size()
                    ? ctx.getPassengers().get(i).getType() : CustomerType.ADULT;
            total += TicketContext.calcPrice(
                    ctx.getDistance(), ctx.getReturnSeats().get(i).getSeatType(), t);
        }

        Table totalTable = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                .useAllAvailableWidth();

        Cell payMethod = new Cell().setBorder(com.itextpdf.layout.borders.Border.NO_BORDER);
        payMethod.add(p("Phương thức thanh toán", COLOR_TEXT_MUTED, 10, false, fontBold, fontRegular));
        payMethod.add(p(ctx.isQrPayment() ? "QR Code (VietQR)" : "Tiền mặt",
                COLOR_PRIMARY, 12, true, fontBold, fontRegular));
        totalTable.addCell(payMethod);

        Cell totalCell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT);
        totalCell.add(p("TỔNG THANH TOÁN", COLOR_TEXT_MUTED, 10, false, fontBold, fontRegular));
        totalCell.add(p(money(total) + " đ", COLOR_ACCENT, 18, true, fontBold, fontRegular));
        totalTable.addCell(totalCell);
        doc.add(totalTable);

        // ── STATUS ──
        doc.add(new Paragraph("").setMarginBottom(10));
        Table statusTable = new Table(1).useAllAvailableWidth()
                .setBackgroundColor(new DeviceRgb(220, 252, 231))
                .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(6));
        Cell sc = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER)
                .setPadding(10).setTextAlignment(TextAlignment.CENTER);
        sc.add(p("✓  THANH TOÁN THÀNH CÔNG", COLOR_SUCCESS, 13, true, fontBold, fontRegular));
        sc.add(p("Vui lòng xuất trình vé khi lên tàu. Cảm ơn quý khách!",
                new DeviceRgb(21, 128, 61), 10, false, fontBold, fontRegular));
        statusTable.addCell(sc);
        doc.add(statusTable);

        // ── FOOTER ──
        doc.add(new Paragraph("").setMarginBottom(20));
        doc.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(0.5f))
                .setStrokeColor(COLOR_BORDER).setMarginBottom(8));
        doc.add(new Paragraph(
                "* Vé có hiệu lực trong ngày khởi hành. "
                        + "Không hoàn trả sau khi tàu khởi hành.")
                .setFont(fontRegular).setFontSize(8)
                .setFontColor(COLOR_TEXT_MUTED)
                .setTextAlignment(TextAlignment.CENTER));

        doc.close();
    }

    private static Table buildSegmentSection(
            String title,
            ScheduleInfoResponse schedule,
            List<Seat> seats,
            List<PassengerInfo> passengers,
            double distance,
            PdfFont fontBold,
            PdfFont fontRegular) {

        Table outer = new Table(1).useAllAvailableWidth()
                .setBorder(new com.itextpdf.layout.borders.SolidBorder(COLOR_BORDER, 0.5f))
                .setBorderRadius(new com.itextpdf.layout.properties.BorderRadius(6));

        Cell titleCell = new Cell()
                .setBackgroundColor(COLOR_PRIMARY)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(8);
        titleCell.add(p(title, ColorConstants.WHITE, 12, true, fontBold, fontRegular));
        outer.addCell(titleCell);

        Cell trainCell = new Cell()
                .setBackgroundColor(COLOR_LIGHT_BG)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(10);
        trainCell.add(p(schedule.getTrainName() + "  (" + schedule.getTrainId() + ")",
                COLOR_PRIMARY, 13, true, fontBold, fontRegular));
        trainCell.add(p(schedule.getDepartureStationName()
                        + "  →  " + schedule.getArrivalStationName(),
                COLOR_TEXT_MUTED, 11, false, fontBold, fontRegular));
        trainCell.add(p(fmt(schedule.getDepartureTime())
                        + "  →  " + fmt(schedule.getArrivalTime()),
                new DeviceRgb(224, 82, 82), 11, true, fontBold, fontRegular));
        outer.addCell(trainCell);

        Table passengerTable = new Table(
                UnitValue.createPercentArray(new float[]{0.5f, 2f, 1.5f, 1.2f, 1.2f}))
                .useAllAvailableWidth();

        String[] headers = {"#", "Họ và tên", "CCCD", "Ghế / Toa", "Giá vé"};
        for (String h : headers) {
            passengerTable.addHeaderCell(new Cell()
                    .setBackgroundColor(COLOR_BORDER)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(6)
                    .add(p(h, COLOR_PRIMARY, 9, true, fontBold, fontRegular)));
        }

        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            PassengerInfo pass = i < passengers.size() ? passengers.get(i) : null;
            CustomerType type  = pass != null ? pass.getType() : CustomerType.ADULT;
            double price = TicketContext.calcPrice(distance, seat.getSeatType(), type);
            String seatTypeName = seat.getSeatType() == SeatType.SOFT_SLEEPER
                    ? "Giường mềm" : "Ghế mềm";

            DeviceRgb rowBg = i % 2 == 0
                    ? new DeviceRgb(255, 255, 255) : new DeviceRgb(248, 250, 255);

            String toaStr = seat.getCarriage() != null
                    ? String.valueOf(seat.getCarriage().getCarriageNumber()) : "?";

            passengerTable.addCell(cellOf(String.valueOf(i + 1), rowBg, 9, fontBold, fontRegular));
            passengerTable.addCell(cellOf(
                    (pass != null ? pass.getName() : "") + "\n" + typeStr(type),
                    rowBg, 9, fontBold, fontRegular));
            passengerTable.addCell(cellOf(
                    pass != null ? pass.getCccd() : "", rowBg, 9, fontBold, fontRegular));
            passengerTable.addCell(cellOf(
                    "Ghế " + seat.getSeatNumber() + " • Toa " + toaStr
                            + "\n" + seatTypeName, rowBg, 9, fontBold, fontRegular));
            passengerTable.addCell(new Cell()
                    .setBackgroundColor(rowBg)
                    .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(6)
                    .add(p(money(price) + " đ", COLOR_ACCENT, 9, true, fontBold, fontRegular)));
        }

        Cell tableCell = new Cell()
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(0);
        tableCell.add(passengerTable);
        outer.addCell(tableCell);

        return outer;
    }

    // ── Helpers ──
    private static Paragraph p(String text, com.itextpdf.kernel.colors.Color color,
                               float size, boolean bold,
                               PdfFont fontBold, PdfFont fontRegular) {
        return new Paragraph(text)
                .setFontSize(size)
                .setFontColor(color)
                .setFont(bold ? fontBold : fontRegular);
    }

    private static Cell cellOf(String text, DeviceRgb bg, float fontSize,
                               PdfFont fontBold, PdfFont fontRegular) {
        return new Cell()
                .setBackgroundColor(bg)
                .setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setPadding(6)
                .add(p(text, COLOR_PRIMARY, fontSize, false, fontBold, fontRegular));
    }

    private static String fmt(java.time.LocalDateTime dt) {
        return dt != null ? dt.format(DT_FMT) : "--";
    }

    private static String money(double v) { return CURRENCY.format((long) v); }

    private static String typeStr(CustomerType t) {
        if (t == null) return "Người lớn";
        return switch (t) {
            case CHILD   -> "Trẻ em";
            case STUDENT -> "Sinh viên";
            case ELDERLY -> "Người cao tuổi";
            default      -> "Người lớn";
        };
    }
}