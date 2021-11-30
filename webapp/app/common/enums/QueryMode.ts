export enum QueryMode {
    INCREMENTAL ='INCREMENTAL',
    FULL_LOAD = 'FULL_LOAD'
};

export const QueryModeLabel: any = {
    [QueryMode.INCREMENTAL]: "Incremental",
    [QueryMode.FULL_LOAD]: "Full Load",
  };