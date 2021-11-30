import React from "react";
import { GetServerSidePropsContext } from "next";
import routerUtils from "@/app/common/utils/routerUtils";
import ConnectorWizard from "@/app/components/connectors/ConnectorWizard";
import { useRouter } from "next/router";
import wizardUtils from "@/app/common/utils/wizardUtils";
import Loading from "@/app/components/common/Loading";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";

export async function getServerSideProps({ query }: GetServerSidePropsContext) {
  const wizardStep = routerUtils.getString(query.wizardStep);
  return {
    props: { wizardStep, appBaseUrl: process.env.APP_BASE_URL }
  };
}

interface WarehouseCreateProps {
  wizardStep: string;
  appBaseUrl: string;
}

const WarehouseCreate = ({ wizardStep, appBaseUrl }: WarehouseCreateProps) => {
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
      oauthCallback="/warehouses"
      category={"Warehouse"}
      curWizardStepGroup={undefined}
      curWizardStep={wizardStep}
      setCurWizardStep={setCurWizardStep}
      steps={{
        selectType: {
          title: "Select Warehouse Type",
          description: "Which warehouse do you own?"
        },
        configure: {
          title: "Configure Warehouse",
          description:
            "Follow the guide on the right to set up your Source or invite a team member to do it for you"
        }
      }}
      onFinish={() => {
        if (process.browser) {
          router.push("/warehouses").then();
        }
      }}
    />
  );
};

export default WarehouseCreate;
