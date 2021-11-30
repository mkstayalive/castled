import { PipelineSchedule } from "@/app/common/dtos/PipelineCreateRequestDto";
import React from "react";
import PipelineSettingsForm, {
  PipelineSettingsConfig,
} from "./PipelineSettingsForm";

import { SettingSchedule } from "@/app/components/pipeline/PipelineSettingsForm";
import bannerNotificationService from "@/app/services/bannerNotificationService";

import pipelineService from "@/app/services/pipelineService";
import { ScheduleTimeUnit } from "@/app/common/enums/ScheduleType";
import { QueryMode } from "@/app/common/enums/QueryMode";

export interface PipelineSettingsViewProps {
  pipelineId?: number;
  name?: string;
  schedule?: PipelineSchedule;
  queryMode? : QueryMode;
}
function PipelineSettingsView({
  pipelineId,
  name,
  schedule,
  queryMode,
}: PipelineSettingsViewProps) {

  const handleSettingsUpdate = (
    name: string,
    pipelineSchedule: PipelineSchedule
  ) => {
    pipelineService.updatePipeline(pipelineId!, {
      name: name,
      schedule: pipelineSchedule,
    });
    bannerNotificationService.success("Pipeline Updated");
  };


  const getSettingsSchedule = (
    schedule?: PipelineSchedule
  ): SettingSchedule => {
    if (schedule === undefined) {
      return {};
    }

    const MINUTES_MULTIPLIER: number = 60;
    const HOURS_MULTIPLIER: number = 3600;
    const DAYS_MULTIPLIER: number = 86400;

    let frequency: number = schedule.frequency!;

    if (frequency / DAYS_MULTIPLIER > 0 && frequency % DAYS_MULTIPLIER === 0) {
      return {
        frequency: frequency / DAYS_MULTIPLIER,
        timeUnit: ScheduleTimeUnit.DAYS,
      };
    }

    if (
      frequency / HOURS_MULTIPLIER > 0 &&
      frequency % HOURS_MULTIPLIER === 0
    ) {
      return {
        frequency: frequency / HOURS_MULTIPLIER,
        timeUnit: ScheduleTimeUnit.HOURS,
      };
    }

    return {
      frequency: frequency / MINUTES_MULTIPLIER,
      timeUnit: ScheduleTimeUnit.MINUTES,
    };
  };

  return (
    <PipelineSettingsForm
      initialValues={
        {
          name: name,
          queryMode: queryMode,
          schedule: getSettingsSchedule(schedule),
        } as PipelineSettingsConfig
      }
      onSubmit={handleSettingsUpdate}
      submitLabel="Save"
    ></PipelineSettingsForm>
  );
}

export default PipelineSettingsView;
