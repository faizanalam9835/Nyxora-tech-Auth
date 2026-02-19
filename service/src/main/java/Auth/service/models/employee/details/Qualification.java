package Auth.service.models.employee.details;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Qualification {

    private String qualificationName; // e.g., B.Tech, MBA

    private String course; // e.g., Computer Science

    private String universityBoard;

    private Integer passingYear;

    private String certificateUrl; // Certificate file ka path
}