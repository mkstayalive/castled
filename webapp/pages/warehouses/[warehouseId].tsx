import React from "react";
import { GetServerSidePropsContext } from "next";
import routerUtils from "@/app/common/utils/routerUtils";
import ConnectorEdit from "@/app/components/connectors/ConnectorEdit";

export async function getServerSideProps({ query }: GetServerSidePropsContext) {
  const warehouseId = routerUtils.getInt(query.warehouseId);
  return {
    props: { warehouseId: warehouseId, appBaseUrl: process.env.APP_BASE_URL }
  };
}

interface AppEditProps {
  warehouseId: number;
  appBaseUrl: string;
}
const AppEdit = ({ warehouseId, appBaseUrl }: AppEditProps) => {
  return (
    <ConnectorEdit
      appBaseUrl={appBaseUrl}
      category={"Warehouse"}
      connectorId={warehouseId}
    />
  );
};

export default AppEdit;
