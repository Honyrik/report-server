package ru.tehnobear.essence.share;

import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class ShareReportConfiguration {
    private String name;

    @Bean
    public CronParser reportCronParser() {
        return new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
    }

    @Value("${app.report.name:localhost}")
    public void setName(String name) {
        if(name.equalsIgnoreCase("localhost")) {
            try {
                this.name = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                this.name = UUID.randomUUID().toString();
            }
        } else {
            this.name = name;
        }
    }

    @Bean
    public String appReportName() {
        return name;
    }
}
