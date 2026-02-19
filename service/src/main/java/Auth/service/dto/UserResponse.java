package Auth.service.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private String id;
    private String username;
    private String email;

    // always SUPER_ADMIN for signup table
    private String role;

    // tenant / hospital info
    private String tenantId;
    private String hospitalName;
    private String hospitalId;

    // jwt token (login ke time)
    private String token;
}
