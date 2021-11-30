import { PipelineRunStatus } from "@/app/common/enums/PipelineRunStatus";
import { PipelineRunStage } from "@/app/common/enums/PipelineRunStage";

export interface PipelineRunDto {
  id: number;
  status: PipelineRunStatus;
  stage: PipelineRunStage;
  pipelineId: number;
  failureMessage: string;
  pipelineSyncStats: {
    recordsSynced: number;
    recordsFailed: number;
    recordsSkipped: number;
    offset: number;
  };
  processedTs: number;
  createdTs: number;
}
