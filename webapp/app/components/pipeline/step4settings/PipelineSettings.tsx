import { PipelineWizardStepProps } from "@/app/components/pipeline/PipelineWizard";
import Layout from "@/app/components/layout/Layout";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import React, { useEffect } from "react";
import Loading from "@/app/components/common/Loading";
import {
  PipelineCreateRequestDto,
  PipelineSchedule,
} from "@/app/common/dtos/PipelineCreateRequestDto";
import pipelineService from "@/app/services/pipelineService";
import bannerNotificationService from "@/app/services/bannerNotificationService";
import PipelineSettingsForm, {
  PipelineSettingsConfig,
} from "@/app/components/pipeline/PipelineSettingsForm";
import { QueryMode } from "@/app/common/enums/QueryMode";

const PipelineSettings = ({
  curWizardStep,
  steps,
  stepGroups,
  setCurWizardStep,
  onFinish,
}: PipelineWizardStepProps) => {
  const { pipelineWizContext, setPipelineWizContext } = usePipelineWizContext();
  useEffect(() => {
    if (!pipelineWizContext) return;
    if (!pipelineWizContext.values) {
      setCurWizardStep("source", "selectType");
      return;
    }
  }, [pipelineWizContext?.values]);
  if (!pipelineWizContext?.values) {
    return <Loading />;
  }
  const pipelineCreateFinish = (
    values: PipelineCreateRequestDto,
    onFinish?: (id: number) => void,
    setSubmitting?: (isSubmitting: boolean) => void
  ) => {
    pipelineService
      .create(values)
      .then(({ data }) => {
        bannerNotificationService.success(`Pipeline created!`);
        if (setSubmitting) setSubmitting(false);
        onFinish?.(data.id);
      })
      .catch((err) => {
        if (setSubmitting) setSubmitting(false);
      });
  };
  return (
    <Layout
      title={steps[curWizardStep].title}
      centerTitle={true}
      steps={steps}
      stepGroups={stepGroups}
    >
      <PipelineSettingsForm
        initialValues={
          { queryMode: QueryMode.INCREMENTAL } as PipelineSettingsConfig
        }
        onSubmit={(
          name: string,
          pipelineSchedule: PipelineSchedule,
          queryMode: QueryMode,
          setSubmitting: (isSubmitting: boolean) => void
        ) => {
          if (!pipelineWizContext || !pipelineWizContext.values) {
            console.log("Not submitting because context is not set");
            return;
          }

          pipelineWizContext.values = {
            ...pipelineWizContext.values,
            name: name,
            queryMode: queryMode,
            schedule: pipelineSchedule,
          };
          pipelineCreateFinish(pipelineWizContext.values!, onFinish, setSubmitting);
        }}
      ></PipelineSettingsForm>
    </Layout>
  );
};

export default PipelineSettings;
