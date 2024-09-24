package ru.tehnobear.essence.share.dto;

import lombok.Builder;
import lombok.Data;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.dao.entries.TReportAsset;

import java.util.List;

@Data
@Builder
public class QueueReport {
    private TQueue queue;
    private List<TReportAsset> reportAssets;
}
