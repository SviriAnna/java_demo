package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.t1.java.demo.model.enums.AccountType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
@JsonTypeName(value = "accounts")
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountDto implements Serializable {

    @JsonProperty("account_id")
    private UUID id;

    @NotNull(message = "ClientId can not be null")
    @JsonProperty("client_id")
    private UUID clientId;

    @NotNull(message = "AccountType can not be null")
    @JsonProperty("account_type")
    private AccountType accountType;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance should not be negative")
    private BigDecimal balance;

}
