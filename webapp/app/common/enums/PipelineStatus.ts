export enum PipelineStatus {
  OK = "OK",
  FAILED = "FAILED",
}

export const PipelineStatusLabel: any = {
  [PipelineStatus.OK]: "OK",
  [PipelineStatus.FAILED]: "Failed",
};
