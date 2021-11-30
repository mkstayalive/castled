package io.castled.pipelines;

import com.google.inject.Singleton;
import io.castled.ObjectRegistry;
import io.castled.apps.DataSink;
import io.castled.apps.models.DataSinkRequest;
import io.castled.commons.models.AppSyncStats;
import io.castled.commons.models.PipelineSyncStats;
import io.castled.commons.streams.ErrorOutputStream;
import io.castled.core.IncessantRunner;
import io.castled.functionalinterfaces.Action;
import io.castled.services.PipelineService;
import io.castled.utils.TimeUtils;

import java.util.Optional;

@Singleton
public class MonitoredDataSink {

    private static class SyncStatsUpdateAction implements Action {

        private final Long pipelineRunId;
        private final PipelineSyncStats startingSyncStats;
        private final DataSink dataSink;
        private final DataSinkRequest dataSinkRequest;
        private PipelineSyncStats lastUpdatedStats;


        public SyncStatsUpdateAction(Long pipelineRunId, PipelineSyncStats startingSyncStats, DataSink dataSink,
                                     DataSinkRequest dataSinkRequest) {
            this.pipelineRunId = pipelineRunId;
            this.startingSyncStats = startingSyncStats;
            this.dataSink = dataSink;
            this.dataSinkRequest = dataSinkRequest;
            this.lastUpdatedStats = new PipelineSyncStats(startingSyncStats.getRecordsSynced(), startingSyncStats.getRecordsFailed(),
                    startingSyncStats.getRecordsSkipped(), startingSyncStats.getOffset());
        }

        @Override
        public void execute() {
            PipelineSyncStats pipelineSyncStats = getPipelineSyncStats(startingSyncStats, dataSink.getSyncStats(), dataSinkRequest.getErrorOutputStream());
            PipelineSyncStats verifiedSyncStats = new PipelineSyncStats(Math.max(pipelineSyncStats.getRecordsSynced(), lastUpdatedStats.getRecordsSynced()),
                    Math.max(pipelineSyncStats.getRecordsFailed(), lastUpdatedStats.getRecordsFailed()),
                    Math.max(pipelineSyncStats.getRecordsSkipped(), lastUpdatedStats.getRecordsSkipped()),
                    pipelineSyncStats.getOffset());
            ObjectRegistry.getInstance(PipelineService.class).updateSyncStats(pipelineRunId, verifiedSyncStats);
            updateFirstDataSynced(dataSinkRequest.getExternalApp().getTeamId(), verifiedSyncStats);
            this.lastUpdatedStats = verifiedSyncStats;
        }

    }

    public PipelineSyncStats syncRecords(DataSink dataSink, PipelineSyncStats startingSyncStats,
                                         Long pipelineRunId, DataSinkRequest dataSinkRequest) throws Exception {
        IncessantRunner incessantRunner = new IncessantRunner(new SyncStatsUpdateAction(pipelineRunId, startingSyncStats, dataSink, dataSinkRequest), TimeUtils.secondsToMillis(5));
        dataSink.syncRecords(dataSinkRequest);
        incessantRunner.shutdown(TimeUtils.minutesToMillis(1));
        PipelineSyncStats pipelineSyncStats = getPipelineSyncStats(startingSyncStats, dataSink.getSyncStats(), dataSinkRequest.getErrorOutputStream());
        updateFirstDataSynced(dataSinkRequest.getExternalApp().getTeamId(), pipelineSyncStats);
        return pipelineSyncStats;
    }

    private static void updateFirstDataSynced(Long userId, PipelineSyncStats pipelineSyncStats) {
        /*
        if (ObjectRegistry.getInstance(UsersCache.class).getValue(userId).getFirstSyncTs() == null
                && pipelineSyncStats.getRecordsSynced() > 0) {
            ObjectRegistry.getInstance(UsersService.class).markFirstDataSynced(userId);
        }
        */
    }

    private static PipelineSyncStats getPipelineSyncStats(PipelineSyncStats startingSyncStats, AppSyncStats appSyncStats,
                                                          ErrorOutputStream errorOutputStream) {

        PipelineSyncStats pipelineSyncStats = Optional.ofNullable(appSyncStats).map(statsRef ->
                        new PipelineSyncStats(statsRef.getRecordsProcessed() - (errorOutputStream.getFailedRecords().get() + statsRef.getRecordsSkipped()),
                                errorOutputStream.getFailedRecords().get(), statsRef.getRecordsSkipped(), Math.min(statsRef.getOffset(),
                                Optional.ofNullable(errorOutputStream.getFirstFailedMessageId()).map(messageId -> messageId - 1).orElse(Long.MAX_VALUE))))
                .orElse(new PipelineSyncStats(0, 0, 0, 0));

        return new PipelineSyncStats(pipelineSyncStats.getRecordsSynced() + startingSyncStats.getRecordsSynced(),
                pipelineSyncStats.getRecordsFailed() + startingSyncStats.getRecordsFailed(),
                pipelineSyncStats.getRecordsSkipped() + startingSyncStats.getRecordsSkipped(),
                Math.max(pipelineSyncStats.getOffset(), startingSyncStats.getOffset()));

    }
}
