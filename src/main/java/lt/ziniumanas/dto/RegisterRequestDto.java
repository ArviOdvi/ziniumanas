package lt.ziniumanas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequestDto {

    @NotBlank(message = "Vartotojo vardas negali būti tuščias")
    @Size(min = 3, max = 20, message = "Vartotojo vardas turi būti nuo 3 iki 20 simbolių")
    private String username;

    @NotBlank(message = "Slaptažodis negali būti tuščias")
    @Size(min = 6, message = "Slaptažodis turi būti bent 6 simbolių")
    private String password;

    private String role = "USER"; // galima palikti su default

}
