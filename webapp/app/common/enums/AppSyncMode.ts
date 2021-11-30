export enum AppSyncMode {
  INSERT = "INSERT",
  UPSERT = "UPSERT",
  UPDATE = "UPDATE",
}

export const AppSyncModeLabel: any = {
  [AppSyncMode.INSERT]: "Insert",
  [AppSyncMode.UPSERT]: "Upsert",
  [AppSyncMode.UPDATE]: "Update",
};
