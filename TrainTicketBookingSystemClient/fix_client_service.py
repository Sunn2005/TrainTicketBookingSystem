with open("src/main/java/iuh/fit/service/TicketClientService.java", "r") as f:
    content = f.read()

# Make ObjectMapper configure JavaTimeModule
if "JavaTimeModule" not in content:
    content = content.replace("import com.fasterxml.jackson.databind.ObjectMapper;", "import com.fasterxml.jackson.databind.ObjectMapper;\nimport com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;\nimport com.fasterxml.jackson.databind.SerializationFeature;")
    content = content.replace("new ObjectMapper();", "new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);")

# Update getSchedulesWithAvailableSeats
new_get_schedules = """    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats(String departureStation, String destinationStation, LocalDate date) {
        try {
            String message = "GET_SCHEDULES|" + departureStation + "|" + destinationStation + "|" + date.toString();
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
                System.err.println("Lỗi lấy lịch trình: " + response);
                return List.of();
            }
            return objectMapper.readValue(response, new TypeReference<List<ScheduleInfoResponse>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }"""
content = __import__("re").sub(
    r"    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats[^{]+{\s*return delegate\.[^}]+}\s*}",
    new_get_schedules,
    content,
    flags=__import__("re").MULTILINE
)

# Update getAvailableSeats
new_get_seats = """    public List<Seat> getAvailableSeats(String scheduleId) {
        try {
            String message = "GET_SEATS|" + scheduleId;
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
                System.err.println("Lỗi lấy ghế: " + response);
                return List.of();
            }
            return objectMapper.readValue(response, new TypeReference<List<Seat>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }"""
content = __import__("re").sub(
    r"    public List<Seat> getAvailableSeats[^{]+{\s*return delegate\.[^}]+}\s*}",
    new_get_seats,
    content,
    flags=__import__("re").MULTILINE
)

with open("src/main/java/iuh/fit/service/TicketClientService.java", "w") as f:
    f.write(content)
