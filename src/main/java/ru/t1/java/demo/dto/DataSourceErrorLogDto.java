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
@JsonTypeName(value = "dataSourceErrorLog")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceErrorLogDto {

    @JsonProperty("method_signature")
    private String methodSignature;

    @JsonProperty("message")
    private String message;

    @JsonProperty("stack_trace")
    private String stackTrace;

}
