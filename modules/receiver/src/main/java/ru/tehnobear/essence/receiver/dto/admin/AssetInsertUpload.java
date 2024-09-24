package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetInsertUpload {
    @NonNull
    private AssetInsert json;
    @NonNull
    private Part upload;
    @Builder
    public AssetInsertUpload(AssetInsert json, Part upload) {
        this.json = json;
        this.upload = upload;
    }

    public static class AssetInsertUploadSchema {
        @NonNull
        public AssetInsert json;
        @NonNull
        public MultipartFile upload;
    }
}
