package io.castled.pubsub.registry;

import io.castled.pubsub.MessageType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PipelineUpdatedMessage extends Message {

    private Long pipelineId;

    public PipelineUpdatedMessage(Long pipelineId) {
        super(MessageType.PIPELINE_UPDATED);
        this.pipelineId = pipelineId;
    }
}
