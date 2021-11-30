package io.castled.utils;

import io.castled.exceptions.CastledRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class FileUtils {

    public static void deleteDirectory(Path directory) {
        try {
            Files.walk(directory)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (NoSuchFileException e) {
        } catch (Exception e) {
            throw new CastledRuntimeException(e);
        }
    }

    public static void compressFile(Path inputFile, Path outputFile) {
        try {
            FileInputStream fis = new FileInputStream(inputFile.toFile());
            FileOutputStream fos = new FileOutputStream(outputFile.toFile());
            GZIPOutputStream gzipOS = new GZIPOutputStream(fos);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzipOS.write(buffer, 0, len);
            }
            //close resources
            gzipOS.close();
            fis.close();
        } catch (IOException e) {
            log.error("Failed to compress file {}", inputFile.toString(), e);
            throw new CastledRuntimeException(e);
        }
    }

    public static List<Path> listFiles(Path directory) throws IOException {
        return Files.walk(directory).filter(Files::isRegularFile)
                .collect(Collectors.toList());
    }

    public static String getResourceFileAsString(String fileName) throws IOException {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(fileName)) {
            if (is == null) return null;
            try (InputStreamReader isr = new InputStreamReader(is);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }
}
