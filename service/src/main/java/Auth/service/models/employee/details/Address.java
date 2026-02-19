package Auth.service.models.employee.details;

import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    // --- Present Address ---
    private String presentHouseStreet;
    private String presentCityVillage;
    private String presentDistrict;
    private String presentState;
    private String presentPinCode;

    // --- Permanent Address ---
    private String permanentHouseStreet;
    private String permanentCityVillage;
    private String permanentDistrict;
    private String permanentState;
    private String permanentPinCode;
}