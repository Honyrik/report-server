package ru.tehnobear.essence.receiver.dto.admin;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetUpdateUpload {
    @NonNull
    private AssetUpdate json;
    @NonNull
    private Part upload;
    @Builder
    public AssetUpdateUpload(AssetUpdate json, Part upload) {
        this.json = json;
        this.upload = upload;
    }

    public static class AssetUpdateUploadSchema {
        @NonNull
        public AssetUpdate json;
        @NonNull
        public MultipartFile upload;
    }
}
