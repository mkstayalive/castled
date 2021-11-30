package io.castled.constants;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ConnectorExecutionConstants {

    public static final String CASTLED_CONTAINER = "castled";
    public static final String UNCOMMITTED_SNAPSHOT = "uncommitted_snapshot";
    public static final String COMMITTED_SNAPSHOT = "committed_snapshot";
    public static final String COMMITTED_SNAPSHOT_BACKUP = "committed_snapshot_bkp";
    public static final String FAILED_RECORDS = "failed_records";

    public static final Path WAREHOUSE_UNLOAD_DIR_PATH = Paths.get("warehouse_unloads");
    public static final Path FAILURE_RECORDS_DIR = Paths.get("pipeline_failed_records");
    public static final Path APP_UPLOADS_PATH = Paths.get("app_uploads");


    public static String getQualifiedCommittedSnapshot(String uuid) {
        return String.format("%s.%s_%s", CASTLED_CONTAINER, uuid, COMMITTED_SNAPSHOT);
    }

    public static String getFailedRecordsTable(String uuid) {
        return String.format("%s_%s", uuid, FAILED_RECORDS);
    }

    public static String getQualifiedUncommittedSnapshot(String uuid) {
        return String.format("%s.%s_%s", CASTLED_CONTAINER, uuid, UNCOMMITTED_SNAPSHOT);
    }

    public static String getCommittedSnapshot(String uuid) {
        return String.format("%s_%s", uuid, COMMITTED_SNAPSHOT);
    }

    public static String getUncommittedSnapshot(String uuid) {
        return String.format("%s_%s", uuid, UNCOMMITTED_SNAPSHOT);
    }

    public static String getQualifiedCommittedSnapshotBkp(String uuid) {
        return String.format("%s.%s_%s", CASTLED_CONTAINER, uuid, COMMITTED_SNAPSHOT_BACKUP);
    }

    public static String getCommittedSnapshotBackup(String uuid) {
        return String.format("%s_%s", uuid, COMMITTED_SNAPSHOT_BACKUP);
    }

}
