package ru.tehnobear.essence.report.ctrl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.report.api.ReportApi;
import ru.tehnobear.essence.share.dto.ReportBody;
import ru.tehnobear.essence.report.service.ReportRunnerService;
import ru.tehnobear.essence.share.dto.Result;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReportRunnerCtrl implements ReportApi {
    private final ReportRunnerService reportService;

    @Override
    public Mono<Result> run(ReportBody data) {
        return reportService.run(data);
    }
}
