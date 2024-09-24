package ru.tehnobear.essence.share.plugin;

import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.share.dto.FileStore;

public interface StoragePlugin {
    void saveFile(TQueue queue, FileStore file);
    FileStore getFile(TQueue queue);
    void deleteFile(TQueue... queues);
}
