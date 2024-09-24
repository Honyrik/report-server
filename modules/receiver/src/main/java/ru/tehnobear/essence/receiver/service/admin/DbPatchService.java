package ru.tehnobear.essence.receiver.service.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.io.IOUtils;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;
import reactor.core.publisher.Mono;
import ru.tehnobear.essence.dao.dto.Audit;
import ru.tehnobear.essence.dao.entries.*;
import ru.tehnobear.essence.dao.util.Util;
import ru.tehnobear.essence.receiver.dto.admin.CreatePatchFetch;
import ru.tehnobear.essence.receiver.dto.admin.CreatePatchResult;
import ru.tehnobear.essence.receiver.dto.admin.DbPatchBody;
import ru.tehnobear.essence.receiver.store.DirPatchStorage;
import ru.tehnobear.essence.share.dto.FileStore;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.exception.ReportException;
import ru.tehnobear.essence.share.util.QueryUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.UUID;

@Component
@Slf4j
public class DbPatchService extends AbstractService {
    private static final String VAR_NAME = "patch";
    public static final int DEFAULT_FILE_EXECUTE = 040555;
    private final DirPatchStorage dirPatchStorage;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")
            .withLocale( new Locale.Builder().setLanguage("ru").setRegion("RU").build())
            .withZone( ZoneId.systemDefault() );
    public DbPatchService(
            JPAQueryFactory queryFactory,
            EntityManager entityManager,
            TransactionTemplate transactionTemplate,
            DirPatchStorage dirPatchStorage
    ) {
        super(queryFactory, entityManager, transactionTemplate);
        this.dirPatchStorage = dirPatchStorage;
    }

    public Mono<CreatePatchResult> fetch(CreatePatchFetch fetch) {
        var query = queryFactory.selectFrom(QTCreatePatch.tCreatePatch);
        if (!fetch.getData().containsKey("clDeleted")) {
            fetch.getData().put("clDeleted", false);
        }
        QueryUtil.util.filterAndSort(query, fetch, QTCreatePatch.tCreatePatch);
        var res = query.fetchResults();

        return Mono.just(
                CreatePatchResult
                        .builder()
                        .total(res.getTotal())
                        .data(res.getResults())
                        .build()
        );
    }

    public Mono<ResponseEntity<Object>> getFileDbPatch(UUID id) {
        var result = Result
                .builder()
                .build();
        var patch = queryFactory
                .selectFrom(QTCreatePatch.tCreatePatch)
                .where(QTCreatePatch.tCreatePatch.ckId.eq(id))
                .fetchOne();
        if (patch == null) {
            result.addError("Not Found patch");
        }

        if (result.isError() || result.isWarning()) {
            return Mono.just(ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(result));
        }

        var file = dirPatchStorage.getFile(patch.getCkId().toString());

        if (file == null) {
            return Mono.just(ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result.addError("Not Found file"))
            );
        }

        return Mono.just(
                ResponseEntity
                        .ok()
                        .header("Content-Type", file.getContentType())
                        .header("Content-Disposition", String.format("attachment; filename=%s", file.getName()))
                        .body(file.getFile())
        );
    }

    public Mono<Result> delete(UUID id, String user) {
        var result = Result
                .builder()
                .ckId(id)
                .build();
        var patch = queryFactory
                .selectFrom(QTCreatePatch.tCreatePatch)
                .where(QTCreatePatch.tCreatePatch.ckId.eq(id))
                .fetchOne();
        if (patch == null) {
            result.addError("Not Found patch");
        }

        if (result.isError() || result.isWarning()) {
            return Mono.just(result);
        }

        dirPatchStorage.deleteFile(id.toString());
        transactionTemplate.execute(status -> {
            queryFactory.delete(QTCreatePatch.tCreatePatch)
                    .where(QTCreatePatch.tCreatePatch.ckId.eq(id))
                    .execute();
            return null;
        });

        return Mono.just(result);
    }

    public Mono<ResponseEntity<Object>> generateDbPatch(DbPatchBody fetch, String user) throws IOException {
        var result = Result
                .builder()
                .build();
        var now = Instant.now();
        var reports = queryFactory.selectFrom(QTReport.tReport)
                .where(QTReport.tReport.ckId.in(fetch.getReports()))
                .fetch();
        if (reports.isEmpty()) {
            return Mono.just(ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(result.addError("Not Found report"))
            );
        }
        var dir = Files.createTempDirectory(VAR_NAME);
        var patchDir = Path.of(dir.toAbsolutePath().toString(), "report").toFile();
        patchDir.mkdirs();
        var classloader = Thread.currentThread().getContextClassLoader();
        var is = classloader.getResourceAsStream("archive.tar.gz");
        uncompressTarGZ(is, dir.toFile());
        var include = new ArrayList<String>();
        for(var report : reports) {
            var id = "report_" + report.getCkId().toString();
            include.add(id);
            try(var writer = new FileOutputStream(Path.of(patchDir.toString(), id + ".sql").toFile())) {
                var stream = new WriterSql(writer, id);
                stream.write(report.getAuthorization());
                stream.write(report.getQueue());
                var reportAssets = queryFactory.selectFrom(QTReportAsset.tReportAsset)
                        .where(QTReportAsset.tReportAsset.report.ckId.eq(report.getCkId()))
                        .fetch();
                var reportFormats = queryFactory.selectFrom(QTReportFormat.tReportFormat)
                        .where(QTReportFormat.tReportFormat.report.ckId.eq(report.getCkId()))
                        .fetch();
                var reportSchedulers = queryFactory.selectFrom(QTScheduler.tScheduler)
                        .where(QTScheduler.tScheduler.report.ckId.eq(report.getCkId()))
                        .fetch();
                var temp = new HashSet<Audit>();
                reportAssets.forEach(ra -> {
                    temp.add(ra.getAsset().getType());
                });
                reportFormats.forEach(rf -> {
                    temp.add(rf.getAsset().getType());
                });
                reportFormats.forEach(rf -> {
                    temp.add(rf.getFormat());
                });
                reportFormats.forEach(rf -> {
                    temp.add(rf.getSource().getSourceType());
                });
                temp.forEach(stream::write);
                reportFormats.forEach(rf -> {
                    stream.write(rf.getSource());
                });
                var temp2 = new HashSet<Audit>();
                reportFormats.forEach(rf -> {
                    temp2.add(rf.getAsset());
                });
                reportAssets.forEach(ra -> {
                    temp2.add(ra.getAsset());
                });
                temp2.forEach(stream::write);
                stream.write(report);
                reportFormats.forEach(stream::write);
                reportAssets.forEach(stream::write);
                reportSchedulers.forEach(stream::write);
                writer.flush();
            }
        }
        try(var writer = new FileOutputStream(Path.of(patchDir.toString(),  "report.xml").toFile())) {
            writer.write(("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "<databaseChangeLog\n" +
                    "  xmlns=\"http://www.liquibase.org/xml/ns/dbchangelog\"\n" +
                    "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                    "  xsi:schemaLocation=\"http://www.liquibase.org/xml/ns/dbchangelog\n" +
                    "         http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.1.xsd\">\n")
                    .getBytes(StandardCharsets.UTF_8));
            include.forEach(id -> {
                try {
                    writer.write(("        <include file=\"" + id +".sql\" relativeToChangelogFile=\"true\" />\n").getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            writer.write("</databaseChangeLog>".getBytes(StandardCharsets.UTF_8));
            writer.flush();
        }
        var tempFile = File.createTempFile("archive", "");
        tempFile.deleteOnExit();
        try (
            var fOut = new FileOutputStream(tempFile);
            var bOut = new BufferedOutputStream(fOut);
            var gzOut = new GzipCompressorOutputStream(bOut);
            var tOut = new TarArchiveOutputStream(gzOut)
        ) {
            var children = dir.toFile().listFiles();
            if (children != null) {
                for (var child : children) {
                    addFileToTarGz(tOut, child.getAbsolutePath(), "");
                }
            }
        }
        var data = Files.readAllBytes(tempFile.toPath());
        var file = FileStore.builder()
                .file(data)
                .name("Patch_" + formatter.format(now) +".tar.gz")
                .contentType("application/gzip")
                .build();
        var res = transactionTemplate.execute(status -> {
            TCreatePatch row = null;
            try {
                row = TCreatePatch.builder()
                        .ckUser(user)
                        .ctCreate(now)
                        .ctChange(now)
                        .cnSize(data.length)
                        .cctParameter(Util.objectMapper.writeValueAsString(fetch))
                        .cvFileName(file.getName())
                        .build();
            } catch (JsonProcessingException e) {
                throw new ReportException(e.getMessage(), e);
            }
            return entityManager.merge(row);
        });
        dirPatchStorage.saveFile(res.getCkId().toString(), file);
        dir.toFile().deleteOnExit();
        return Mono.just(
                ResponseEntity
                        .ok()
                        .header("Content-Type", file.getContentType())
                        .header("Content-Disposition", String.format("attachment; filename=%s", file.getName()))
                        .body(file.getFile())
        );
    }
    private void addFileToTarGz(TarArchiveOutputStream tOut, String path, String base)
            throws IOException
    {
        var f = new File(path);
        var entryName = base + f.getName();
        var tarEntry = new TarArchiveEntry(f, entryName);
        if (
            entryName.endsWith("gradlew") ||
            entryName.endsWith("gradlew.bat") ||
            entryName.endsWith("update") ||
            entryName.endsWith("update.bat")
        ) {
            tarEntry.setMode(DEFAULT_FILE_EXECUTE);
        }
        tOut.putArchiveEntry(tarEntry);

        if (f.isFile()) {
            IOUtils.copy(new FileInputStream(f), tOut);
            tOut.closeArchiveEntry();
        } else {
            tOut.closeArchiveEntry();
            var children = f.listFiles();
            if (children != null) {
                for (var child : children) {
                    addFileToTarGz(tOut, child.getAbsolutePath(), entryName + "/");
                }
            }
        }
    }
    private static void uncompressTarGZ(InputStream tarFile, File dest) throws IOException {
        try(var tarIn = new TarArchiveInputStream(
                new GzipCompressorInputStream(
                        new BufferedInputStream(
                                tarFile
                        )
                )
        )) {
            var tarEntry = tarIn.getNextTarEntry();
            while (tarEntry != null) {
                var destPath = new File(dest, tarEntry.getName());
                if (tarEntry.isDirectory()) {
                    destPath.mkdirs();
                } else {
                    destPath.createNewFile();
                    var btoRead = new byte[1024];
                    var bout =
                            new BufferedOutputStream(new FileOutputStream(destPath));
                    int len = 0;
                    while ((len = tarIn.read(btoRead)) != -1) {
                        bout.write(btoRead, 0, len);
                    }
                    bout.close();
                }
                tarEntry = tarIn.getNextTarEntry();
            }
        }
    }
    private static class WriterSql {
        final FileOutputStream writer;
        public WriterSql(FileOutputStream writer, String id) throws IOException {
            this.writer = writer;
            writer.write("--liquibase formatted sql\n".getBytes(StandardCharsets.UTF_8));
            writer.write(("--changeset patcher-core:" + id + " dbms:postgresql runOnChange:true splitStatements:false stripComments:false\n").getBytes(StandardCharsets.UTF_8));
        }
        public void write(Audit audit) {
            try {
                writer.write(audit.toPostgresPatch().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
