import React, { useEffect } from "react";
import { WizardSteps } from "@/app/common/dtos/internal/WizardSteps";
import { useRouter } from "next/router";
import PipelineWizardSource from "@/app/components/pipeline/step1source/PipelineWizardSource";
import wizardUtils from "@/app/common/utils/wizardUtils";
import Loading from "@/app/components/common/Loading";
import PipelineWizardDestination from "@/app/components/pipeline/step2destination/PipelineWizardDestination";
import PipelineMapping from "@/app/components/pipeline/step3mapping/PipelineMapping";
import PipelineSettings from "@/app/components/pipeline/step4settings/PipelineSettings";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import routerUtils from "@/app/common/utils/routerUtils";
import _ from "lodash";

interface PipelineWizardProps {
  appBaseUrl: string;
  curWizardStepGroup: string | undefined;
  curWizardStep: string;
  steps: WizardSteps;
  stepGroups?: WizardSteps;
  onFinish: (id: number) => void;
}

export interface PipelineWizardStepProps {
  appBaseUrl: string;
  curWizardStep: string;
  steps: WizardSteps;
  stepGroups?: WizardSteps;
  setCurWizardStep: (stepGroup: string | undefined, step: string) => void;
  onFinish?: (id: number) => void;
}

const PipelineWizard = ({
  appBaseUrl,
  curWizardStepGroup,
  curWizardStep,
  onFinish,
  steps
}: PipelineWizardProps) => {
  const router = useRouter();
  const { pipelineWizContext, setPipelineWizContext } = usePipelineWizContext();
  const wizardStepKey = routerUtils.getString(router.query.wizardStep);
  const setCurWizardStep = (stepGroup: string | undefined, step: string) => {
    wizardUtils.setCurWizardStep(router, stepGroup, step);
  };
  useEffect(() => {
    if (!pipelineWizContext) return;
    if (wizardStepKey === "source:selectType") {
      setPipelineWizContext({});
    }
  }, [wizardStepKey, !!pipelineWizContext]);
  if (!curWizardStepGroup && !curWizardStep) {
    setCurWizardStep("source", "selectType");
    return <Loading />;
  }
  return (
    <>
      {curWizardStepGroup === "source" && (
        <PipelineWizardSource
          appBaseUrl={appBaseUrl}
          curWizardStep={curWizardStep}
          stepGroups={steps}
          steps={{
            selectType: {
              title: "Select Warehouse Type",
              description: "Which warehouse do you own?"
            },
            selectExisting: {
              title: "Select Existing or Create New",
              description:
                "Choose from your existing warehouse or create a new one"
            },
            configure: {
              title: "Configure Warehouse",
              description:
                "Follow the guide on the right to set up your Source or invite a team member to do it for you"
            },
            model: {
              title: "Configure Model",
              description: "Write a query to fetch data from source warehouse"
            }
          }}
          setCurWizardStep={setCurWizardStep}
        />
      )}
      {curWizardStepGroup === "destination" && (
        <PipelineWizardDestination
          appBaseUrl={appBaseUrl}
          curWizardStep={curWizardStep}
          stepGroups={steps}
          steps={{
            selectType: {
              title: "Select App Type",
              description: "Which app do you wish to connect to?"
            },
            selectExisting: {
              title: "Select Existing or Create New",
              description: "Choose from your existing app or create a new one"
            },
            configure: {
              title: "Configure App",
              description:
                "Follow the guide on the right to set up your Source or invite a team member to do it for you"
            },
            settings: {
              title: "App Sync Settings",
              description: "Configure how you wish to load data into the app"
            }
          }}
          setCurWizardStep={setCurWizardStep}
        />
      )}
      {curWizardStepGroup === undefined && curWizardStep === "mapping" && (
        <PipelineMapping
          appBaseUrl={appBaseUrl}
          curWizardStep={curWizardStep}
          stepGroups={steps}
          steps={{
            mapping: {
              title: "Map fields",
              description: "Map source fields to destination"
            }
          }}
          setCurWizardStep={setCurWizardStep}
        />
      )}
      {curWizardStepGroup === undefined && curWizardStep === "settings" && (
        <PipelineSettings
          appBaseUrl={appBaseUrl}
          curWizardStep={curWizardStep}
          stepGroups={steps}
          steps={{
            settings: {
              title: "Final Settings",
              description: "You are almost there. Setup pipeline frequency"
            }
          }}
          setCurWizardStep={setCurWizardStep}
          onFinish={onFinish}
        />
      )}
    </>
  );
};

export default PipelineWizard;
