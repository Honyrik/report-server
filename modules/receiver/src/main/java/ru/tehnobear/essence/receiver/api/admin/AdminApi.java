package ru.tehnobear.essence.receiver.api.admin;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tehnobear.essence.share.web.ReportExceptionHandler;

@Tag(name = "Admin", description = "Управление")
@RequestMapping("/admin")
@ReportExceptionHandler
public interface AdminApi {
}
