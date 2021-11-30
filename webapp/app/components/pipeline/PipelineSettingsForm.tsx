import React from "react";
import { Form, Formik, FormikHelpers } from "formik";
import InputField from "@/app/components/forminputs/InputField";
import InputSelect from "@/app/components/forminputs/InputSelect";
import renderUtils from "@/app/common/utils/renderUtils";
import ButtonSubmit from "@/app/components/forminputs/ButtonSubmit";
import {
  ScheduleTimeUnit,
  ScheduleType,
  SchedulTimeUnitLabel,
} from "@/app/common/enums/ScheduleType";

import { QueryMode, QueryModeLabel } from "@/app/common/enums/QueryMode";

import { PipelineSchedule } from "@/app/common/dtos/PipelineCreateRequestDto";
import { title } from "process";

export interface PipelineSettingsProps {
  initialValues: PipelineSettingsConfig;
  submitLabel?: string;
  onSubmit: (
    name: string,
    pipelineSchedule: PipelineSchedule,
    queryMode: QueryMode,
    setSubmitting: (isSubmitting: boolean) => void
  ) => void;
}

export interface PipelineSettingsConfig {
  name?: string;
  schedule: SettingSchedule;
  queryMode?: QueryMode;
}

export interface SettingSchedule {
  timeUnit?: ScheduleTimeUnit;
  frequency?: number;
}

function PipelineSettingsForm({
  initialValues,
  onSubmit,
  submitLabel,
}: PipelineSettingsProps) {
  const getFrequencySecs = (frequency: number, timeUnit: ScheduleTimeUnit) => {
    if (timeUnit == ScheduleTimeUnit.MINUTES) {
      return frequency * 60;
    }
    if (timeUnit == ScheduleTimeUnit.HOURS) {
      return frequency * 60 * 60;
    }
    if (timeUnit == ScheduleTimeUnit.DAYS) {
      return frequency * 24 * 60 * 60;
    }
    return frequency;
  };

  const handleSubmit = (
    pipelineSettings: PipelineSettingsConfig,
    { setSubmitting }: FormikHelpers<any>
  ) => {
    setSubmitting(false); //NP: this is required when the first query responds with an error and the submit button should still be available for next query
    onSubmit(
      pipelineSettings.name!,
      {
        type: ScheduleType.FREQUENCY,
        frequency: getFrequencySecs(
          pipelineSettings.schedule.frequency!,
          pipelineSettings.schedule.timeUnit!
        ),
      },
      pipelineSettings.queryMode!,
      setSubmitting
    );
  };
  return (
    <Formik
      initialValues={initialValues}
      onSubmit={handleSubmit}
      enableReinitialize
    >
      {({ values, setFieldValue, setFieldTouched, isSubmitting }) => (
        <Form>
          <InputField title="Pipeline Name" type="text" name="name" />
          <InputSelect
            title="Query Mode"
            options={renderUtils.selectOptions(QueryModeLabel)}
            values={values}
            setFieldValue={setFieldValue}
            setFieldTouched={setFieldTouched}
            name="queryMode"
          />
          <label className="form-label mb-3">Pipeline Schedule</label>

          <InputField
            title="Frequency"
            type="number"
            name="schedule.frequency"
          />
          <InputSelect
            title="Time Unit"
            options={renderUtils.selectOptions(SchedulTimeUnitLabel)}
            values={values}
            setFieldValue={setFieldValue}
            setFieldTouched={setFieldTouched}
            name="schedule.timeUnit"
          />
          <ButtonSubmit submitting={isSubmitting}>{submitLabel}</ButtonSubmit>
        </Form>
      )}
    </Formik>
  );
}

export default PipelineSettingsForm;
