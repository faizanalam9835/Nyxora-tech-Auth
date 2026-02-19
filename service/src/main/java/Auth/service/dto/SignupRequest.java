package Auth.service.dto;

import lombok.Data;

@Data
public class SignupRequest {

    private String username;
    private String email;
    private String password;

    private String companyId;
    private String companyName;
}

