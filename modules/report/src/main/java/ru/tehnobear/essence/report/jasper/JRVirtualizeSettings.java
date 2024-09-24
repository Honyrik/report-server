package ru.tehnobear.essence.report.jasper;

import com.fasterxml.jackson.annotation.JsonCreator;
import jakarta.annotation.Nonnull;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.JRVirtualizer;
import net.sf.jasperreports.engine.fill.JRFileVirtualizer;
import net.sf.jasperreports.engine.fill.JRGzipVirtualizer;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import org.springframework.lang.Nullable;

import java.util.Arrays;

@Data
@Builder
@Slf4j
public class JRVirtualizeSettings {
    @Builder.Default
    private EVirtualize type = EVirtualize.NO_VIRTUALIZE;
    @Builder.Default
    private int maxSize = 300;
    @Builder.Default
    private String directoryName = System.getProperty("java.io.tmpdir");
    @Builder.Default
    private int blockSize = 8192;
    @Builder.Default
    private int minGrowCount = 128;
    @Builder.Default
    private boolean swapOwner = true;


    public enum EVirtualize {
        NO_VIRTUALIZE,
        FILE,
        SWAP_FILE,
        GZIP;

        @Nonnull
        @JsonCreator
        public static EVirtualize fromText(@Nonnull String name) {
            String upName = name.toUpperCase();
            return Arrays.stream(EVirtualize.values())
                    .filter(val -> val.name().equals(upName))
                    .findFirst()
                    .orElse(NO_VIRTUALIZE);
        }
    }

    @Nullable
    public JRVirtualizer getVirtualizer() {
        var virtualizerType = this.getType();
        log.debug("Virtualizer type: {}", virtualizerType);
        switch (virtualizerType) {
            case FILE:
                return getFileVirtualizer();
            case SWAP_FILE:
                return getSwapFileVirtualizer();
            case GZIP:
                return getGzipVirtualizer();
            default:
                return null;

        }
    }

    private JRVirtualizer getGzipVirtualizer() {
        int maxSize = this.getMaxSize();
        return new JRGzipVirtualizer(maxSize);
    }

    private JRVirtualizer getSwapFileVirtualizer() {
        int blockSize = this.getBlockSize();
        int minGrowCount = this.getMinGrowCount();
        boolean swapOwner = this.isSwapOwner();
        String directoryName = this.getDirectoryName();
        JRSwapFile swap = new JRSwapFile(directoryName, blockSize, minGrowCount);
        int maxSize = this.getMaxSize();
        return new JRSwapFileVirtualizer(maxSize, swap, swapOwner);
    }

    private JRVirtualizer getFileVirtualizer() {
        int maxSize = this.getMaxSize();
        String directoryName = this.getDirectoryName();
        return new JRFileVirtualizer(maxSize, directoryName);
    }
}
