import React from "react";
import { GetServerSidePropsContext } from "next";
import routerUtils from "@/app/common/utils/routerUtils";
import ConnectorWizard from "@/app/components/connectors/ConnectorWizard";
import wizardUtils from "@/app/common/utils/wizardUtils";
import { useRouter } from "next/router";
import Loading from "@/app/components/common/Loading";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";

export async function getServerSideProps({ query }: GetServerSidePropsContext) {
  const wizardStep = routerUtils.getString(query.wizardStep);
  return {
    props: { wizardStep, appBaseUrl: process.env.APP_BASE_URL }
  };
}

interface AppsCreateProps {
  wizardStep: string;
  appBaseUrl: string;
}

const AppsCreate = ({ wizardStep, appBaseUrl }: AppsCreateProps) => {
  const router = useRouter();
  const setCurWizardStep = (stepGroup: string | undefined, step: string) =>
    wizardUtils.setCurWizardStep(router, stepGroup, step);
  if (!wizardStep) {
    setCurWizardStep(undefined, "selectType");
    return <Loading />;
  }
  return (
    <ConnectorWizard
      appBaseUrl={appBaseUrl}
      oauthCallback="/apps"
      category={"App"}
      curWizardStepGroup={undefined}
      curWizardStep={wizardStep}
      setCurWizardStep={setCurWizardStep}
      steps={{
        selectType: {
          title: "Select App Type",
          description: "Which app do you own?"
        },
        configure: {
          title: "Configure App",
          description:
            "Follow the guide on the right to set up your Source or invite a team member to do it for you"
        }
      }}
      onFinish={() => {
        if (process.browser) {
          router.push("/apps").then();
        }
      }}
    />
  );
};

export default AppsCreate;
