package io.castled.pipelines;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.castled.constants.CommonConstants;
import io.castled.jarvis.JarvisTaskGroup;
import io.castled.jarvis.JarvisTaskType;
import io.castled.jarvis.taskmanager.JarvisTasksClient;
import io.castled.jarvis.taskmanager.models.RetryCriteria;
import io.castled.jarvis.taskmanager.models.requests.TaskCreateRequest;
import io.castled.models.Pipeline;
import io.castled.services.PipelineService;
import io.castled.utils.TimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

@Slf4j
public class PipelineTriggerJob implements Job {

    private final JarvisTasksClient jarvisTasksClient;
    private final PipelineService pipelineService;

    @Inject
    public PipelineTriggerJob(JarvisTasksClient jarvisTasksClient, PipelineService pipelineService) {
        this.jarvisTasksClient = jarvisTasksClient;
        this.pipelineService = pipelineService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        Long pipelineId = jobDataMap.getLongValue(CommonConstants.PIPELINE_ID);
        Pipeline pipeline = pipelineService.getActivePipeline(pipelineId);
        if (pipeline == null) {
            return;
        }
        try {
            TaskCreateRequest taskCreateRequest = TaskCreateRequest.builder()
                    .group(JarvisTaskGroup.PIPELINE_RUN.name())
                    .type(JarvisTaskType.PIPELINE_RUN.name())
                    .expiry(Math.max(TimeUtils.secondsToMillis(pipeline.getJobSchedule().getExecutionTime()),
                            TimeUtils.minutesToMillis(5)))
                    .params(ImmutableMap.of(CommonConstants.PIPELINE_ID, pipelineId))
                    .uniqueId(String.valueOf(pipelineId)).retryCriteria(new RetryCriteria(3, true))
                    .build();
            jarvisTasksClient.createTask(taskCreateRequest);
        } catch (Exception e) {
            log.error("Pipeline executor job failed for pipeline {}", pipelineId);
            throw new JobExecutionException(e);
        }
    }
}
