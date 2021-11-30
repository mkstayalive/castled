export enum PipelineSyncStatus {
  ACTIVE = "ACTIVE",
  PAUSED = "PAUSED",
}

export const PipelineSyncStatusLabel: any = {
  [PipelineSyncStatus.ACTIVE]: "Active",
  [PipelineSyncStatus.PAUSED]: "Paused",
};
