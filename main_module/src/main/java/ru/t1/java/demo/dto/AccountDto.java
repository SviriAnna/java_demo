package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import ru.t1.java.demo.model.enums.AccountStatus;
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

    @JsonProperty("id")
    private UUID id;

    @JsonProperty(value = "account_id", access = JsonProperty.Access.READ_ONLY)
    private UUID accountId;

    @NotNull(message = "ClientId can not be null")
    @JsonProperty("client_id")
    private UUID clientId;

    @NotNull(message = "Account type can not be null")
    @JsonProperty("account_type")
    private AccountType accountType;

    @NotNull(message = "Account status can not be null")
    @JsonProperty("account_status")
    private AccountStatus accountStatus;

    @NotNull(message = "Balance is required")
    @DecimalMin(value = "0.00", message = "Balance should not be negative")
    private BigDecimal balance;

    @JsonProperty("frozen_amount")
    private BigDecimal frozenAmount;
}
