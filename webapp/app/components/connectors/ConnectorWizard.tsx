import React from "react";
import Layout from "@/app/components/layout/Layout";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";
import ConnectorForm from "@/app/components/connectors/ConnectorForm";
import { WizardSteps } from "@/app/common/dtos/internal/WizardSteps";
import SelectConnectorType from "@/app/components/connectors/SelectConnectorType";
import { ConnectorCategory } from "@/app/common/utils/types";
import SelectExistingConnector from "@/app/components/connectors/SelectExistingConnector";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import Loading from "@/app/components/common/Loading";
import _ from "lodash";

interface ConnectorWizardProps {
  appBaseUrl: string;
  category: ConnectorCategory;
  curWizardStepGroup: string | undefined;
  curWizardStep: string;
  steps: WizardSteps;
  stepGroups?: WizardSteps;
  oauthCallback?: string;
  setCurWizardStep: (stepGroup: string | undefined, step: string) => void;
  onConnectorTypeSelect?: (type: ConnectorTypeDto) => void;
  onFinish: (id: number) => void;
}

const ConnectorWizard = ({
  appBaseUrl,
  category,
  curWizardStepGroup,
  curWizardStep,
  steps,
  stepGroups,
  setCurWizardStep,
  onConnectorTypeSelect,
  oauthCallback,
  onFinish
}: ConnectorWizardProps) => {
  const { pipelineWizContext, setPipelineWizContext } = usePipelineWizContext();
  if (!pipelineWizContext) return <Loading />;
  const typeOption =
    category === "App"
      ? pipelineWizContext.appType
      : pipelineWizContext.warehouseType;
  const saveTypeOption = (type: ConnectorTypeDto) => {
    if (category === "App") {
      pipelineWizContext.appType = type;
      _.set(pipelineWizContext, "mappingInfo.appType", type.value);
    } else {
      pipelineWizContext.warehouseType = type;
    }
    setPipelineWizContext(pipelineWizContext);
  };
  return (
    <Layout
      title={steps[curWizardStep].title}
      centerTitle={true}
      steps={steps}
      stepGroups={stepGroups}
    >
      <>
        {curWizardStep === "selectType" && (
          <SelectConnectorType
            category={category}
            onSelect={(type) => {
              onConnectorTypeSelect?.(type);
              saveTypeOption(type);
              setCurWizardStep(
                curWizardStepGroup,
                "selectExisting" in steps && type.count > 0
                  ? "selectExisting"
                  : "configure"
              );
            }}
          />
        )}
        {curWizardStep === "selectExisting" && typeOption && (
          <SelectExistingConnector
            category={category}
            typeOption={typeOption}
            onCreate={() => {
              setCurWizardStep(curWizardStepGroup, "configure");
            }}
            onSelect={onFinish}
          />
        )}
        {curWizardStep === "configure" && typeOption && (
          <ConnectorForm
            appBaseUrl={appBaseUrl}
            oauthCallback={oauthCallback}
            category={category}
            connectorType={typeOption.value}
            accessType={typeOption.accessType}
            docUrl={typeOption.docUrl}
            onFinish={onFinish}
          />
        )}
      </>
    </Layout>
  );
};

export default ConnectorWizard;
