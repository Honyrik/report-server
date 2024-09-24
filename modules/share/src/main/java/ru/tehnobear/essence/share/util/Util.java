package ru.tehnobear.essence.share.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.NoArgsConstructor;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.http.codec.multipart.Part;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@NoArgsConstructor
public class Util {
    public static Util util = new Util();
    public static final ObjectMapper objectMapper;
    public static final ObjectMapper objectMapperAll;
    static  {
        objectMapper = new ObjectMapper(new JsonFactory());
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        objectMapper.registerModules(new JavaTimeModule(), new Jdk8Module());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapperAll = new ObjectMapper(new JsonFactory());
        objectMapperAll.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapperAll.registerModules(new JavaTimeModule(), new Jdk8Module());
        objectMapperAll.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public Mono<String> partToString(Part part) {
        return part.content()
            .map(val -> val.asInputStream(true))
            .reduce(SequenceInputStream::new)
            .map(this::getString);
    }
    public Mono<byte[]> partToByte(Part part) {
        return part.content()
                .map(val -> val.asInputStream(true))
                .reduce(SequenceInputStream::new)
                .map(this::getByte);
    }
    public Mono<InputStream> partToInputStream(Part part) {
        return part.content()
                .map(val -> val.asInputStream(true))
                .reduce(SequenceInputStream::new);
    }

    public String getString(InputStream inputStream) {

        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            for (int result = bis.read(); result != -1; result = bis.read()) {
                buf.write((byte) result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return buf.toString(StandardCharsets.UTF_8);
    }

    public byte[] getByte(InputStream inputStream) {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        try {
            for (int result = bis.read(); result != -1; result = bis.read()) {
                buf.write((byte) result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return buf.toByteArray();
    }

    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static String hmac(String alg, String data, String key)
            throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), alg);
        Mac mac = Mac.getInstance(alg);
        mac.init(secretKeySpec);
        return bytesToHex(mac.doFinal(data.getBytes()));
    }

    public static void buildFlattenedMap(Map<String, Object> result, Map<String, Object> source, @Nullable String path) {
        source.forEach((key, value) -> {
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    key = path + key;
                }
                else {
                    key = path + '.' + key;
                }
            }
            if (value instanceof String) {
                result.put(key, value);
            }
            else if (value instanceof Map map) {
                // Need a compound key
                result.put(key, value);
                buildFlattenedMap(result, map, key);
            }
            else if (value instanceof Collection collection) {
                // Need a compound key
                if (collection.isEmpty()) {
                    result.put(key, "");
                }
                else {
                    int count = 0;
                    result.put(key, value);
                    for (Object object : collection) {
                        buildFlattenedMap(result, Collections.singletonMap(
                                "[" + (count++) + "]", object), key);
                    }
                }
            }
            else {
                result.put(key, (value != null ? value : ""));
            }
        });
    }

    public static void resolveMap(Map<String, Object> source, @Nullable String path, PropertySourcesPropertyResolver resolver) {
        source.forEach((key, value) -> {
            var keyPath = key;
            if (StringUtils.hasText(path)) {
                if (key.startsWith("[")) {
                    keyPath = path + key;
                }
                else {
                    keyPath = path + '.' + key;
                }
            }
            if (value instanceof String) {
                source.put(key, resolver.getProperty(keyPath));
            }
            else if (value instanceof Map map) {
                resolveMap(map, keyPath, resolver);
            }
            else if (value instanceof Collection collection) {
                source.put(key, buildResolveArr(collection, path, resolver));
            }
        });
    }

    public static Collection<Object> buildResolveArr(Collection<Object> source, @Nullable String path, PropertySourcesPropertyResolver resolver) {
        int key = 0;
        var res = new ArrayList<Object>();
        source.forEach((value) -> {
            var keyPath = String.format("%s", path, key);
            if (StringUtils.hasText(path)) {
                keyPath = String.format("%s[%s]", path, key);
            }
            if (value instanceof String) {
                res.add(resolver.getProperty(keyPath));
            }
            else if (value instanceof Map map) {
                res.add(map);
                resolveMap(map, keyPath, resolver);
            }
            else if (value instanceof Collection collection) {
                res.add(buildResolveArr(collection, path, resolver));
            } else {
                res.add(value);
            }
        });
        return res;
    }
}
