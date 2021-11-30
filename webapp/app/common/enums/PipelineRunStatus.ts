export enum PipelineRunStatus {
  PROCESSING = "PROCESSING",
  PROCESSED = "PROCESSED",
  FAILED = "FAILED",
}

export const PipelineRunStatusLabel: any = {
  [PipelineRunStatus.PROCESSING]: "Processing",
  [PipelineRunStatus.PROCESSED]: "Completed",
  [PipelineRunStatus.FAILED]: "Failed",
};
