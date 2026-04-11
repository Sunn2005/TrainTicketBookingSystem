package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String userID;
    private String fullName;
    private String role;
    private boolean isSuccess;
    private String message;


    public static LoginResponse success(String userID, String fullName, String role) {
        return new LoginResponse(userID, fullName, role, true, "Đăng nhập thành công");
    }

    public static LoginResponse fail(String message) {
        return new LoginResponse(null, null, null, false, message);
    }


}
