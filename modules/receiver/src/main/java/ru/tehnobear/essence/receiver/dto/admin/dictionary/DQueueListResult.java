package ru.tehnobear.essence.receiver.dto.admin.dictionary;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.springframework.lang.NonNull;
import ru.tehnobear.essence.dao.entries.TDQueue;
import ru.tehnobear.essence.share.dto.Fetch;
import ru.tehnobear.essence.share.dto.Result;

import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
public class DQueueListResult extends Result {
    @NonNull
    private Set<TDQueueList> data;

    @Data
    @EqualsAndHashCode
    @Builder
    public static class TDQueueList {
        @Builder.Default
        private Boolean leaf = true;
        @Builder.Default
        private Boolean expanded = false;
        private String ckId;
        private String cvRunnerUrl;
        private String ckParent;
        private String ckUser;
        @EqualsAndHashCode.Exclude
        private Instant ctChange;
        @EqualsAndHashCode.Exclude
        private Instant ctCreate;
        private Boolean clDeleted;

        public static Set<TDQueueList> fromTDQueue(TDQueue data, Fetch fetch) {
            var res = new HashSet<TDQueueList>();
            if (data.getParent() != null) {
                res.add(
                        TDQueueList
                                .builder()
                                .leaf(false)
                                .ckId(data.getParent().getCkId())
                                .cvRunnerUrl(data.getParent().getCvRunnerUrl())
                                .ckParent(Optional.ofNullable(data.getParent().getParent()).map(TDQueue::getCkId).orElse(null))
                                .ckUser(data.getParent().getCkUser())
                                .ctChange(data.getParent().getCtChange())
                                .ctCreate(data.getParent().getCtCreate())
                                .clDeleted(data.getParent().isClDeleted())
                                .build()
                );
            }
            res.add(
                TDQueueList
                .builder()
                    .leaf(!(data.getChildren() != null && !data.getChildren().isEmpty()))
                    .ckId(data.getCkId())
                    .cvRunnerUrl(data.getCvRunnerUrl())
                    .ckParent(Optional.ofNullable(data.getParent()).map(TDQueue::getCkId).orElse(null))
                    .ckUser(data.getCkUser())
                    .ctChange(data.getCtChange())
                    .ctCreate(data.getCtCreate())
                    .clDeleted(data.isClDeleted())
                .build()
            );
            if (data.getChildren() != null && !data.getChildren().isEmpty()) {
                var clDeleted = Optional.ofNullable(fetch.getData()).map(obj -> obj.get("clDeleted"));
                for(var val : data.getChildren()) {
                    if (clDeleted.isPresent() && !(((Boolean) val.isClDeleted()).equals((Boolean) clDeleted.get()))) {
                        continue;
                    }
                    res.addAll(TDQueueList.fromTDQueue(val, fetch));
                }
            }
            return res;
        }
    }
}
