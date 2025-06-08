package ru.t1.java.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonTypeName(value = "timeLimitExceedLog")
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeLimitExceedLogDto {

    @JsonProperty("method_signature")
    private String methodSignature;

    @JsonProperty("execution_time")
    private long executionTime;

    @JsonProperty("wanted_time")
    private long wantedTime;

}
