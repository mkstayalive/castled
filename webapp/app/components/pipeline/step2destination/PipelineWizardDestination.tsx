import React from "react";
import ConnectorWizard from "@/app/components/connectors/ConnectorWizard";
import { PipelineWizardStepProps } from "@/app/components/pipeline/PipelineWizard";
import _ from "lodash";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";
import DestinationSettings from "@/app/components/pipeline/step2destination/DestinationSettings";
import Loading from "@/app/components/common/Loading";

const CUR_WIZARD_STEP_GROUP = "destination";

const PipelineWizardDestination = ({
  appBaseUrl,
  curWizardStep,
  steps,
  stepGroups,
  setCurWizardStep
}: PipelineWizardStepProps) => {
  const { pipelineWizContext, setPipelineWizContext } = usePipelineWizContext();
  if (!pipelineWizContext) return <Loading />;
  return (
    <>
      {curWizardStep !== "settings" && (
        <ConnectorWizard
          appBaseUrl={appBaseUrl}
          category={"App"}
          curWizardStepGroup={CUR_WIZARD_STEP_GROUP}
          curWizardStep={curWizardStep}
          steps={steps}
          stepGroups={stepGroups}
          setCurWizardStep={setCurWizardStep}
          onConnectorTypeSelect={(type: ConnectorTypeDto) => {
            _.set(
              pipelineWizContext,
              "values.appSyncConfig.appType",
              type.value
            );
            _.set(pipelineWizContext, "appType", type);
            setPipelineWizContext(pipelineWizContext);
          }}
          onFinish={(id) => {
            _.set(pipelineWizContext, "values.appId", id);
            setPipelineWizContext(pipelineWizContext);
            setCurWizardStep(CUR_WIZARD_STEP_GROUP, "settings");
          }}
        />
      )}
      {curWizardStep === "settings" && (
        <DestinationSettings
          appBaseUrl={appBaseUrl}
          curWizardStep={curWizardStep}
          steps={steps}
          stepGroups={stepGroups}
          setCurWizardStep={setCurWizardStep}
        />
      )}
    </>
  );
};

export default PipelineWizardDestination;
