package ru.tehnobear.essence.receiver.api.admin;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tehnobear.essence.share.web.ReportExceptionHandler;

@Tag(name = "Dictionary Admin", description = "Справочники")
@RequestMapping("/admin/dictionary")
@ReportExceptionHandler
public interface DictionaryApi {
}
