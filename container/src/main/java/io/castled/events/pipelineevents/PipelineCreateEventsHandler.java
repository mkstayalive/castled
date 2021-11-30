package io.castled.events.pipelineevents;

import com.google.inject.Inject;
import io.castled.misc.PipelineScheduleManager;

public class PipelineCreateEventsHandler implements PipelineEventsHandler {

    private final PipelineScheduleManager pipelineScheduleManager;

    @Inject
    public PipelineCreateEventsHandler(PipelineScheduleManager pipelineScheduleManager) {
        this.pipelineScheduleManager = pipelineScheduleManager;
    }

    @Override
    public void handlePipelineEvent(PipelineEvent pipelineEvent) {
        this.pipelineScheduleManager.reschedulePipeline(pipelineEvent.getPipelineId());
    }
}
