package dto;

public class ActionResponse {
    private boolean success;
    private String message;

    public ActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ActionResponse success(String message) {
        return new ActionResponse(true, message);
    }

    public static ActionResponse fail(String message) {
        return new ActionResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

}
