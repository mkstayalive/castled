import { ScheduleType } from "@/app/common/enums/ScheduleType";
import { QueryMode } from "../enums/QueryMode";

export interface PipelineCreateRequestDto {
  name?: string;
  schedule?: PipelineSchedule;
  appId?: number;
  warehouseId?: number;
  sourceQuery?: string;
  queryMode? : QueryMode;
  appSyncConfig: {
    appType?: string;
    [key: string]: string | undefined;
  };
  mapping?: PipelineMappingDto;
}

export interface PipelineMappingDto {
  primaryKeys?: string[];
  fieldMappings: FieldMapping[];
}

export interface FieldMapping {
  warehouseField?: string;
  appField?: string;
  skipped?: boolean;
}

export interface PipelineSchedule {
  type?: ScheduleType;
  cronExpression?: string;
  frequency?: number;
}
