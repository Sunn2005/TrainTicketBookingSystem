import re

with open("../TrainTicketBookingSystemServer/src/main/java/app/SocketServer.java", "r") as f:
    content = f.read()

# Replace Imports
if "import controller.TicketController;" not in content:
    content = content.replace("import controller.StationController;", "import controller.StationController;\nimport controller.TicketController;\nimport com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;\nimport com.fasterxml.jackson.databind.SerializationFeature;")

# Replace fields
if "private final TicketController ticketController =" not in content:
    content = content.replace("private final ObjectMapper objectMapper = new ObjectMapper();",
"""private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final TicketController ticketController = new TicketController();""")

# Add GET_SCHEDULES
get_schedules_code = """
        if (trimmed.toUpperCase().startsWith("GET_SCHEDULES|")) {
            return handleGetSchedules(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("GET_SEATS|")) {
            return handleGetSeats(trimmed);
        }
"""
if "GET_SCHEDULES|" not in content:
    content = content.replace('if ("GET_ALL_STATIONS".equalsIgnoreCase(trimmed)) {', get_schedules_code + '        if ("GET_ALL_STATIONS".equalsIgnoreCase(trimmed)) {')

# Add handler functions
handler_code = """
    private String handleGetSchedules(String command) {
        String[] parts = command.split("\\\\|");
        if (parts.length < 4) return "ERROR|Sai dinh dang. Dung: GET_SCHEDULES|departureStationId|arrivalStationId|yyyy-MM-dd";
        try {
            java.time.LocalDate date = java.time.LocalDate.parse(parts[3]);
            java.util.List<dto.ScheduleInfoResponse> result = ticketController.getSchedulesWithAvailableSeats(parts[1], parts[2], date);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetSeats(String command) {
        String[] parts = command.split("\\\\|");
        if (parts.length < 2) return "ERROR|Sai dinh dang. Dung: GET_SEATS|scheduleId";
        try {
            java.util.List<model.entity.Seat> result = ticketController.getAvailableSeats(parts[1]);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }
"""
if "handleGetSchedules" not in content:
    content = content.replace("    private String handleLogin(String command) {", handler_code + "\n    private String handleLogin(String command) {")

with open("../TrainTicketBookingSystemServer/src/main/java/app/SocketServer.java", "w") as f:
    f.write(content)
