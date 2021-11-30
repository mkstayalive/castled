import React from "react";
import { GetServerSidePropsContext } from "next";
import routerUtils from "@/app/common/utils/routerUtils";
import ConnectorEdit from "@/app/components/connectors/ConnectorEdit";

export async function getServerSideProps({ query }: GetServerSidePropsContext) {
  const appId = routerUtils.getInt(query.appId);
  return {
    props: { appId, appBaseUrl: process.env.APP_BASE_URL }
  };
}

interface AppEditProps {
  appId: number;
  appBaseUrl: string;
}
const AppEdit = ({ appBaseUrl, appId }: AppEditProps) => {
  return (
    <ConnectorEdit
      appBaseUrl={appBaseUrl}
      category={"App"}
      connectorId={appId}
    />
  );
};

export default AppEdit;
