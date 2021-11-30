package io.castled.misc;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.castled.pipelines.PipelineTriggerJob;
import io.castled.constants.CommonConstants;
import io.castled.exceptions.CastledRuntimeException;
import io.castled.jarvis.scheduler.JarvisScheduler;
import io.castled.jarvis.scheduler.models.JarvisScheduledJob;
import io.castled.models.Pipeline;
import io.castled.services.PipelineService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;

@Slf4j
@Singleton
public class PipelineScheduleManager {

    private static final String JOB_GROUP = "pipelines";

    private final JarvisScheduler jarvisScheduler;
    private final PipelineService pipelineService;

    @Inject
    public PipelineScheduleManager(JarvisScheduler jarvisScheduler, PipelineService pipelineService) {
        this.jarvisScheduler = jarvisScheduler;
        this.pipelineService = pipelineService;
    }

    public void reschedulePipeline(Long pipelineId) {
        try {
            Pipeline pipeline = this.pipelineService.getActivePipeline(pipelineId);
            JobKey jobKey = JobKey.jobKey(String.valueOf(pipelineId), JOB_GROUP);
            JarvisScheduledJob jarvisScheduledJob = JarvisScheduledJob.builder()
                    .jobKey(jobKey).jobSchedule(pipeline.getJobSchedule()).jobClazz(PipelineTriggerJob.class)
                    .jobParams(ImmutableMap.of(CommonConstants.PIPELINE_ID, pipelineId))
                    .build();
            this.jarvisScheduler.unScheduleJob(jobKey);
            this.jarvisScheduler.scheduleJob(jarvisScheduledJob);
        } catch (Exception e) {
            log.error("Reschedule failed for pipeline {}", pipelineId);
            throw new CastledRuntimeException(e);
        }

    }

    public void unschedulePipeline(Long pipelineId) {
        try {
            JobKey jobKey = JobKey.jobKey(String.valueOf(pipelineId), JOB_GROUP);
            this.jarvisScheduler.deleteJob(jobKey);
        } catch (Exception e) {
            log.error("Unschedule failed for pipeline {}", pipelineId);
            throw new CastledRuntimeException(e);
        }

    }
}
