package ru.tehnobear.essence.receiver.store;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tehnobear.essence.share.dto.FileStore;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class DirPatchStorage {
    private static final String VAR_NAME = "name";
    private static final String VAR_CONTENT_TYPE = "contentType";
    private String path;
    @Autowired
    public void setPath(
            @Value("${app.receiver.patchPath:TMP_LOCAL}")
            String path
    ) {
        if (path.equalsIgnoreCase("TMP_LOCAL")) {
            path = System.getProperty("java.io.tmpdir");
        }
        this.path = path;
    }

    public void saveFile(String id, FileStore file) {
        var fileData = Path.of(path, String.format("%s.data", id)).toFile();
        var fileMetaDate = Path.of(path, String.format("%s.metadata.json", id)).toFile();
        try(var writer = new FileOutputStream(fileData)) {
            writer.write(file.getFile());
            writer.flush();
        } catch (IOException e) {
            throw ReportException.fromFormat("Error store file for {}", id, e);
        }
        try(var writer = new FileOutputStream(fileMetaDate)) {
            writer.write(Util.objectMapper.writeValueAsBytes(
                Map.of(
                    VAR_CONTENT_TYPE, file.getContentType(),
                    VAR_NAME, file.getName()
                )
            ));
            writer.flush();
        } catch (IOException e) {
            throw ReportException.fromFormat("Error store metaData file for {}", id, e);
        }
    }

    public FileStore getFile(String id) {
        var fileData = Path.of(path, String.format("%s.data", id));
        var fileMetaDate = Path.of(path, String.format("%s.metadata.json", id)).toFile();
        if (!fileMetaDate.exists()) {
            throw ReportException.fromFormat("Not found metaData file for {}", id);
        }
        if (!fileData.toFile().exists()) {
            throw ReportException.fromFormat("Not found metaData file for {}", id);
        }
        try {
            var setting = Util.objectMapper.readValue(fileMetaDate, Map.class);
            return FileStore.builder()
                    .file(Files.readAllBytes(fileData))
                    .name((String) setting.get(VAR_NAME))
                    .contentType((String) setting.get(VAR_CONTENT_TYPE))
                    .build();
        } catch (IOException e) {
            throw ReportException.fromFormat("Error parse or read file for {}", id);
        }
    }

    public void deleteFile(String... ids) {
        Arrays
            .stream(ids)
            .forEach(id -> {
                var fileData = Path.of(path, String.format("%s.data", id)).toFile();
                var fileMetaDate = Path.of(path, String.format("%s.metadata.json", id)).toFile();
                try {
                    if (fileData.exists()) {
                        fileData.delete();
                    }
                    if (fileMetaDate.exists()) {
                        fileMetaDate.delete();
                    }
                } catch (Exception e) {
                    log.error("Error delete for {}", id, e);
                }
            });
    }
}
