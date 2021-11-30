import React, { useEffect, useState } from "react";
import ConnectorForm from "@/app/components/connectors/ConnectorForm";
import { ConnectorDto } from "@/app/common/dtos/ConnectorDto";
import appsService from "@/app/services/appsService";
import DefaultErrorPage from "next/error";
import { ConnectorCategory } from "@/app/common/utils/types";
import warehouseService from "@/app/services/warehouseService";
import Loading from "@/app/components/common/Loading";
import Layout from "@/app/components/layout/Layout";

interface ConnectorEditProps {
  appBaseUrl: string;
  category: ConnectorCategory;
  connectorId: number;
}

const ConnectorEdit = ({
  appBaseUrl,
  category,
  connectorId
}: ConnectorEditProps) => {
  const [connector, setConnector] = useState<ConnectorDto | undefined | null>();

  useEffect(() => {
    const fetcher = category === "App" ? appsService : warehouseService;
    fetcher
      .getById(connectorId)
      .then(({ data }) => {
        setConnector(data);
      })
      .catch(() => {
        setConnector(null);
      });
  }, []);

  if (connector === null) return <DefaultErrorPage statusCode={404} />;
  return (
    <Layout title={connector?.name || ""}>
      {!connector && <Loading />}
      {connector && (
        <ConnectorForm
          appBaseUrl={appBaseUrl}
          oauthCallback={category == "App" ? "/apps" : "/warehouses"}
          editConnector={connector}
          category={category}
          connectorType={connector.type}
          accessType={connector.accessType}
          docUrl={connector.docUrl}
          onFinish={() => {}}
        />
      )}
    </Layout>
  );
};

export default ConnectorEdit;
