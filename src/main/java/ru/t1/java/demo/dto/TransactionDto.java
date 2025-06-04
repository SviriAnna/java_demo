package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@ToString
@JsonTypeName(value = "transactions")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionDto implements Serializable {

    @JsonProperty("transaction_id")
    private UUID id;

    @NotNull(message = "AccountId can not be null")
    @JsonProperty("account_id")
    private UUID accountId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.00", message = "Amount should not be negative")
    @JsonProperty("amount")
    private BigDecimal amount;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime transactionTime;

}
