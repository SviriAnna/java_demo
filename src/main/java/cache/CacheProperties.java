package cache;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "cache")
@Validated
public class CacheProperties {

    @NotNull
    @Min(1)
    private long recordLifetime;

}
