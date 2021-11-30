import React, { useEffect, useState } from "react";
import Layout from "@/app/components/layout/Layout";
import { Alert, Badge, Table } from "react-bootstrap";
import appsService from "@/app/services/appsService";
import { ConnectorDto } from "@/app/common/dtos/ConnectorDto";
import Loading from "@/app/components/common/Loading";
import DefaultErrorPage from "next/error";
import { ConnectorCategory } from "@/app/common/utils/types";
import warehouseService from "@/app/services/warehouseService";
import bannerNotificationService from "@/app/services/bannerNotificationService";

import Link from "next/link";
import { useRouter } from "next/router";

interface ConnectorViewProps {
  category: ConnectorCategory;
}

const ConnectorView = ({ category }: ConnectorViewProps) => {
  const [connectors, setConnectors] = useState<
    ConnectorDto[] | undefined | null
  >();

  const router = useRouter();

  const path = category === "App" ? "/apps" : "/warehouses";
  const headers = ["#", "Name", "Type", "Pipelines", "Status"];

  useEffect(() => {
    if (!router.isReady) return;
    if (router.query.id) {
      bannerNotificationService.success("SUCCESS");
    }

    // codes using router.query
  }, [router.isReady]);

  useEffect(() => {
    const fetcher = category === "App" ? appsService : warehouseService;
    fetcher
      .get()
      .then(({ data }) => {
        setConnectors(data);
      })
      .catch(() => {
        setConnectors(null);
      });
  }, []);
  if (connectors === null) return <DefaultErrorPage statusCode={404} />;
  return (
    <Layout
      title={category + "s"}
      rightBtn={{
        id: `add_${category.toLowerCase()}_button`,
        title: `Add ${category}`,
        href: `${path}/create?wizardStep=selectType`,
      }}
    >
      {!connectors && <Loading />}
      {connectors && connectors.length === 0 && (
        <Alert variant="light" className="text-center">
          No app connections created yet!
        </Alert>
      )}
      {connectors && connectors.length > 0 && (
        <div className="table-responsive">
          <Table hover>
            <thead>
              <tr>
                {headers.map((header, idx) => (
                  <th key={idx}>{header}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {connectors.map((connector, idx) => (
                <tr key={idx}>
                  <td>{connector.id}</td>
                  <td>
                    <Link href={`${path}/${connector.id}`}>
                      <a>{connector.name}</a>
                    </Link>
                  </td>
                  <td>{connector.type}</td>
                  <td>{connector.pipelines}</td>
                  <td>
                    <Badge
                      bg={connector.status === "OK" ? "success" : "danger"}
                    >
                      {connector.status}
                    </Badge>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      )}
    </Layout>
  );
};

export default ConnectorView;
