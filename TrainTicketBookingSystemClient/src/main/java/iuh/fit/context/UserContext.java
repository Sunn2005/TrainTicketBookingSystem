package iuh.fit.context;

public class UserContext {
    private static final UserContext INSTANCE = new UserContext();

    private String userID;
    private String fullName;
    private String role;

    private UserContext() {
        this.userID = "";
        this.fullName = "";
        this.role = "";
    }

    public static UserContext getInstance() {
        return INSTANCE;
    }

    public void setUser(String userID, String fullName, String role) {
        this.userID = userID == null ? "" : userID;
        this.fullName = fullName == null ? "" : fullName;
        this.role = role == null ? "" : role;
    }

    public void clear() {
        this.userID = "";
        this.fullName = "";
        this.role = "";
    }

    public String getUserID() {
        return userID;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }
}
