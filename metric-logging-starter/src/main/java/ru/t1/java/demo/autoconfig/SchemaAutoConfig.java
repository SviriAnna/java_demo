package ru.t1.java.demo.autoconfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.*;

@AutoConfiguration
@ConditionalOnProperty(prefix = "schema", name = "enabled", havingValue = "true")
public class SchemaAutoConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchemaAutoConfig.class);

    @Bean
    @ConditionalOnBean(DataSource.class)
    public ApplicationRunner checkSchema(DataSource dataSource) {
        return args -> {
            logger.info("Starting schema validation and creation...");

            try (Connection conn = dataSource.getConnection();
                 Statement stmt = conn.createStatement()) {

                // Проверка и создание data_source_error_logs
                try (ResultSet rs = conn.getMetaData().getTables(null, null, "data_source_error_logs", null)) {
                    if (!rs.next()) {
                        logger.info("Table 'data_source_error_logs' not found, creating...");
                        stmt.execute("""
                            CREATE TABLE data_source_error_logs (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                message VARCHAR(1024),
                                stacktrace TEXT,
                                method_signature VARCHAR(512)
                            )
                        """);
                        logger.info("Table 'data_source_error_logs' created successfully.");
                    } else {
                        logger.info("Table 'data_source_error_logs' already exists.");
                    }
                }

                // Проверка и создание time_limit_exceed_log
                try (ResultSet rs = conn.getMetaData().getTables(null, null, "time_limit_exceed_log", null)) {
                    if (!rs.next()) {
                        logger.info("Table 'time_limit_exceed_log' not found, creating...");
                        stmt.execute("""
                            CREATE TABLE time_limit_exceed_log (
                                id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                method_signature VARCHAR(255),
                                execution_time BIGINT,
                                wanted_time BIGINT
                            )
                        """);
                        logger.info("Table 'time_limit_exceed_log' created successfully.");
                    } else {
                        logger.info("Table 'time_limit_exceed_log' already exists.");
                    }
                }

                logger.info("Schema validation and creation completed.");
            } catch (SQLException e) {
                logger.error("Failed to validate or create schema", e);
                throw new RuntimeException("Failed to validate or create schema", e);
            }
        };
    }
}
