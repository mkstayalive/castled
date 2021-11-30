export enum PipelineRunStage {
  RUN_TRIGGERED = "RUN_TRIGGERED",
  RECORDS_POLLED = "RECORDS_POLLED",
  RECORDS_SYNCED = "RECORDS_SYNCED",
  FAILURE_RECORDS_PROCESSED = "FAILURE_RECORDS_PROCESSED",
}

export const PipelineRunStageLabel: any = {
  [PipelineRunStage.RUN_TRIGGERED]: "Run Triggered",
  [PipelineRunStage.RECORDS_POLLED]: "Records Polled",
  [PipelineRunStage.RECORDS_SYNCED]: "Records Synced",
  [PipelineRunStage.FAILURE_RECORDS_PROCESSED]: "Failure Records Processed",
};
