package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.t1.java.demo.model.enums.ClientStatus;

import java.io.Serializable;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
@JsonTypeName(value = "clients")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientDto implements Serializable {

    private UUID id;

    @JsonProperty(value = "client_id", access = JsonProperty.Access.READ_ONLY)
    private UUID clientId;

    @NotBlank(message = "First name must not be blank")
    @JsonProperty("first_name")
    private String firstName;

    @NotBlank(message = "Last name must not be blank")
    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("middle_name")
    private String middleName;

    @JsonProperty("client_status")
    private ClientStatus clientStatus;

}
