package ru.tehnobear.essence.service;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import ru.tehnobear.essence.dao.DBProperties;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

@Configuration
@ConditionalOnProperty(name="app.service.enabled", havingValue = "true")
@EnableConfigurationProperties(DBProperties.class)
@RequiredArgsConstructor
public class AsyncConfiguration {
    private static final String TABLE_NAME = "t_shedlock";
    private final DBProperties dbProperties;

    @Value("${app.service.reportSchedulerPool}")
    protected Integer reportSchedulerPool;

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withTableName(dbProperties.getSchema() == null ? "t_shedlock" : String.format("%s.%s", dbProperties.getSchema(),TABLE_NAME))
                .withColumnNames(new JdbcTemplateLockProvider.ColumnNames("cv_name", "ct_lock_until", "ct_locked_at", "cv_locked_by"))
                .withJdbcTemplate(new JdbcTemplate(dataSource))
                .usingDbTime()
                .build()
        );
    }

    @Bean(name = "appThreadPoolTaskExecutor")
    public Executor appThreadPoolTaskExecutor() {
        return new ThreadPoolTaskExecutor();
    }

    @Bean
    public TaskScheduler reportTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setThreadNamePrefix("reportTask");
        scheduler.setPoolSize(reportSchedulerPool);
        return scheduler;
    }
}
