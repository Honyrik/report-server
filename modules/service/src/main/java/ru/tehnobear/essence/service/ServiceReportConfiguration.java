package ru.tehnobear.essence.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import ru.tehnobear.essence.service.service.ReportJobService;

@Configuration
@ConditionalOnProperty(name="app.service.enabled", havingValue = "true")
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ServiceReportConfiguration {
    private final ReportJobService reportService;

    @Scheduled(cron = "${app.service.runReportCron}")
    public void runReport() {
        log.debug("Check run report");
        reportService.runReport();
    }

    @Scheduled(cron = "${app.service.clearReportCron}")
    public void clearReport() {
        log.debug("Clear report");
        reportService.clearReport();
    }

    @Scheduled(cron = "${app.service.reRunReportCron}")
    public void reRunReport() {
        log.debug("Check Re report");
        reportService.reRunReport();
    }

    @Scheduled(cron = "${app.service.registrationNewSchedulerCron}")
    public void registrationNewScheduler() {
        log.debug("Check registration New Scheduler");
        reportService.registrationNewScheduler();
    }
}
