package ru.tehnobear.essence.share.store;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.SignerFactory;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.internal.S3Signer;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import ru.tehnobear.essence.dao.entries.TQueue;
import ru.tehnobear.essence.share.dto.FileStore;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.plugin.StoragePlugin;
import ru.tehnobear.essence.share.util.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class AwsStorage implements StoragePlugin {
    private static final String FILE_NAME_METADATA = "filename";
    private final AmazonS3 csClient;
    private final AwsStoragePrperties prop;
    private static final String SIGNER_TYPE = "riak-cs";
    static {
        SignerFactory.registerSigner(SIGNER_TYPE, S3Signer.class);
    }

    public AwsStorage(Map<String, Object> param) {
        this.prop = Util.objectMapper.convertValue(param, AwsStoragePrperties.class);
        var csClient = AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(prop.endpoint, null))
                .withChunkedEncodingDisabled(true)
                .withPathStyleAccessEnabled(true)
                .withCredentials(
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(prop.accessKey, prop.secretKey)))
                .withClientConfiguration(
                        prop.proxyHost == null || prop.proxyHost.isEmpty() ? new ClientConfiguration()
                                .withSignerOverride(prop.signerType) : new ClientConfiguration()
                                .withProxyHost(prop.proxyHost)
                                .withProxyPort(prop.proxyPort)
                                .withSignerOverride(prop.signerType));
        this.csClient = csClient.build();
        if (!isBucketExists(prop.bucket)) {
            this.csClient.createBucket(prop.bucket);
        }
    }

    private boolean isBucketExists(String bucket) {
        log.debug("Check bucket exists {}", bucket);
        try {
            var buckets = csClient.listBuckets();
            boolean contains = buckets.stream()
                    .map(Bucket::getName)
                    .anyMatch(s -> s.equals(bucket));
            log.debug("Result: {}", contains);
            return contains;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw ReportException.fromFormat("Not load list Bucket", e);
        }
    }

    @Override
    public void saveFile(TQueue queue, FileStore file) {
        var objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(file.getContentType());
        objectMetadata.addUserMetadata(FILE_NAME_METADATA, file.getName());
        csClient.putObject(prop.bucket, Optional.ofNullable(prop.dir).map(val -> val + "/").orElse("") + queue.getCkId().toString(), new ByteArrayInputStream(file.getFile()), objectMetadata);
    }

    @Override
    public FileStore getFile(TQueue queue) {
        try {
            var obj = csClient.getObject(prop.bucket, Optional.ofNullable(prop.dir).map(val -> val + "/").orElse("") + queue.getCkId().toString());
            return FileStore.builder()
                    .contentType(obj.getObjectMetadata().getContentType())
                    .name(obj.getObjectMetadata().getUserMetaDataOf(FILE_NAME_METADATA))
                    .file(IOUtils.toByteArray(obj.getObjectContent()))
                    .build();
        } catch (IOException e) {
            throw ReportException.fromFormat("Not read file for queue {}", queue.getCkId(), e);
        }
    }

    @Override
    public void deleteFile(TQueue... queues) {
        csClient.deleteObjects(
            new DeleteObjectsRequest(prop.bucket)
            .withKeys(Arrays.stream(queues).map(val -> new DeleteObjectsRequest.KeyVersion(Optional.ofNullable(prop.dir).map(dir -> dir + "/").orElse("") + val.getCkId().toString())).toList())
        );
    }

    @Data
    public static class AwsStoragePrperties {
        private String endpoint = "http://s3.amazonaws.com";
        private String dir;
        private String bucket;
        private String accessKey;
        private String secretKey;
        private String proxyHost;
        private Integer proxyPort;
        private String signerType = SIGNER_TYPE;
    }
}
