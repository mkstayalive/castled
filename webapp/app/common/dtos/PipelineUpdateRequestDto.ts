import { PipelineSchedule } from "@/app/common/dtos/PipelineCreateRequestDto";

export interface PipelineUpdateRequestDto {
  name: string;
  schedule: PipelineSchedule;
}
