package ru.tehnobear.essence.share.store;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.share.dto.FileStore;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.StoragePlugin;
import ru.tehnobear.essence.share.util.Util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;

@Slf4j
public class DirStorage implements StoragePlugin {
    private static final String VAR_NAME = "name";
    private static final String VAR_CONTENT_TYPE = "contentType";
    private DirStorageProperties prop;
    public DirStorage(Map<String, Object> param) {
        this.prop = Util.objectMapper.convertValue(param, DirStorageProperties.class);
    }
    @Override
    public void saveFile(TQueue queue, FileStore file) {
        var fileData = Path.of(prop.path, String.format("%s.data", queue.getCkId())).toFile();
        var fileMetaDate = Path.of(prop.path, String.format("%s.metadata.json", queue.getCkId())).toFile();
        try(var writer = new FileOutputStream(fileData)) {
            writer.write(file.getFile());
            writer.flush();
        } catch (IOException e) {
            throw ReportException.fromFormat("Error store file for queue {}", queue.getCkId(), e);
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
            throw ReportException.fromFormat("Error store metaData file for queue {}", queue.getCkId(), e);
        }
    }

    @Override
    public FileStore getFile(TQueue queue) {
        var fileData = Path.of(prop.path, String.format("%s.data", queue.getCkId()));
        var fileMetaDate = Path.of(prop.path, String.format("%s.metadata.json", queue.getCkId())).toFile();
        if (!fileMetaDate.exists()) {
            throw ReportException.fromFormat("Not found metaData file for queue {}", queue.getCkId());
        }
        if (!fileData.toFile().exists()) {
            throw ReportException.fromFormat("Not found metaData file for queue {}", queue.getCkId());
        }
        try {
            var setting = Util.objectMapper.readValue(fileMetaDate, Map.class);
            return FileStore.builder()
                    .file(Files.readAllBytes(fileData))
                    .name((String) setting.get(VAR_NAME))
                    .contentType((String) setting.get(VAR_CONTENT_TYPE))
                    .build();
        } catch (IOException e) {
            throw ReportException.fromFormat("Error parse or read file for queue {}", queue.getCkId());
        }
    }

    @Override
    public void deleteFile(TQueue... queues) {
        Arrays
            .stream(queues)
            .forEach(queue -> {
                var fileData = Path.of(prop.path, String.format("%s.data", queue.getCkId())).toFile();
                var fileMetaDate = Path.of(prop.path, String.format("%s.metadata.json", queue.getCkId())).toFile();
                try {
                    if (fileData.exists()) {
                        fileData.delete();
                    }
                    if (fileMetaDate.exists()) {
                        fileMetaDate.delete();
                    }
                } catch (Exception e) {
                    log.error("Error delete for queue {}", queue.getCkId(), e);
                }
            });
    }

    @Data
    public class DirStorageProperties {
        private String path = System.getProperty("java.io.tmpdir");
    }
}
