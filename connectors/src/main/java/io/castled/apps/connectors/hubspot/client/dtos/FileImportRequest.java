package io.castled.apps.connectors.hubspot.client.dtos;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class FileImportRequest {

    private String name;
    private List<File> files;


    @Builder
    public FileImportRequest(String name, String fileName, FileFormat fileFormat, DateFormat dateFormat,
                             boolean hasHeader, List<ColumnMapping> columnMappings) {
        this.name = name;
        this.files = Lists.newArrayList(new File(fileName, fileFormat, dateFormat, new FileImportPage(hasHeader, columnMappings)));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    private static class File {

        private String fileName;
        private FileFormat fileFormat;
        private DateFormat dateFormat;
        private FileImportPage fileImportPage;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class FileImportPage {
        private boolean hasHeader;
        private List<ColumnMapping> columnMappings;
    }
}
