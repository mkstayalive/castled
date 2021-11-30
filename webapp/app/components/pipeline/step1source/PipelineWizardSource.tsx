import React from "react";
import ConnectorWizard from "@/app/components/connectors/ConnectorWizard";
import { PipelineWizardStepProps } from "@/app/components/pipeline/PipelineWizard";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import _ from "lodash";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";
import WarehouseModel from "@/app/components/pipeline/step1source/WarehouseModel";
import Loading from "@/app/components/common/Loading";

const CUR_WIZARD_STEP_GROUP = "source";

const PipelineWizardSource = ({
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
      {curWizardStep !== "model" && (
        <ConnectorWizard
          appBaseUrl={appBaseUrl}
          category={"Warehouse"}
          curWizardStepGroup={CUR_WIZARD_STEP_GROUP}
          curWizardStep={curWizardStep}
          steps={steps}
          stepGroups={stepGroups}
          setCurWizardStep={setCurWizardStep}
          onConnectorTypeSelect={(type: ConnectorTypeDto) => {
            _.set(pipelineWizContext, "warehouseType", type);
            setPipelineWizContext(pipelineWizContext);
          }}
          onFinish={(id) => {
            _.set(pipelineWizContext, "values.warehouseId", id);
            setPipelineWizContext(pipelineWizContext);
            setCurWizardStep(CUR_WIZARD_STEP_GROUP, "model");
          }}
        />
      )}
      {curWizardStep === "model" && (
        <WarehouseModel
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

export default PipelineWizardSource;
