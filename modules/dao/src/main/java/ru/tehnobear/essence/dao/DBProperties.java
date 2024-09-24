package ru.tehnobear.essence.dao;

import com.zaxxer.hikari.HikariConfig;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.Properties;

@ConfigurationProperties(prefix = "app.db")
@Getter
@Setter
public class DBProperties extends HikariConfig {
    private final static Duration DEFAULT_TX_TIMEOUT = Duration.ofMinutes(1);
    private Properties jpa;
    private Duration defaultTxTimeout = DEFAULT_TX_TIMEOUT;
}
