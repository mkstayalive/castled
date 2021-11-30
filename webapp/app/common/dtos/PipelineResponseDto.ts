import {
  FieldMapping,
  PipelineSchedule,
} from "@/app/common/dtos/PipelineCreateRequestDto";
import { AppSyncMode } from "@/app/common/enums/AppSyncMode";
import { PipelineSyncStatus } from "@/app/common/enums/PipelineSyncStatus";
import { PipelineStatus } from "@/app/common/enums/PipelineStatus";
import { PipelineRunStatus } from "@/app/common/enums/PipelineRunStatus";
import { QueryMode } from "../enums/QueryMode";

export interface PipelineResponseDto {
  id: number;
  seqId: number;
  teamId: number;
  uuid: string;
  name: string;
  jobSchedule: PipelineSchedule;
  sourceQuery: string;
  status: PipelineStatus;
  syncStatus: PipelineSyncStatus;
  queryMode: QueryMode;
  appSyncConfig: {
    appType: string;
    mode: AppSyncMode;
    object?: {
      objectName: string;
      appType: string;
    };
    subResource?: {
      objectName: string;
      appType: string;
    };
  };
  dataMapping: {
    primaryKeys: string[];
    fieldMappings: FieldMapping[];
  };
  app: ConnectorDetails;
  warehouse: ConnectorDetails;
  lastRunDetails: {
    lastRuns: PipelineLastRun[];
    lastRunTs: number;
  };
  deleted: true;
}

export interface PipelineLastRun {
  id: number;
  status: PipelineRunStatus;
  pipelineId: number;
  failureMessage: string;
  syncStats: {
    recordsProcessed: number;
    recordsFailed: number;
    recordsSkipped: number;
  };
  processedTs: number;
  createdTs: number;
}

export interface ConnectorDetails {
  id: number;
  type: string;
  name: string;
}
