import React, { useEffect, useState } from "react";
import Layout from "@/app/components/layout/Layout";
import { Alert, Badge, Table } from "react-bootstrap";
import pipelineService from "@/app/services/pipelineService";
import { PipelineResponseDto } from "@/app/common/dtos/PipelineResponseDto";
import Link from "next/link";
import DefaultErrorPage from "next/error";
import Loading from "@/app/components/common/Loading";

const Pipelines = () => {
  const [pipelines, setPipelines] = useState<
    PipelineResponseDto[] | undefined | null
  >();
  const headers = ["#", "Name", "Source", "Destination", "Status"];
  useEffect(() => {
    pipelineService
      .get()
      .then(({ data }) => {
        setPipelines(data);
      })
      .catch(() => {
        setPipelines(null);
      });
  }, []);
  if (pipelines === null) return <DefaultErrorPage statusCode={404} />;
  return (
    <Layout
      title="Pipelines"
      rightBtn={{
        id: "create_pipeline_button",
        title: "Create",
        href: "/pipelines/create?wizardStep=source:selectType",
      }}
    >
      {!pipelines && <Loading />}
      {pipelines && pipelines.length === 0 && (
        <Alert variant="light" className="text-center">
          No app connections created yet!
        </Alert>
      )}
      {pipelines && pipelines.length > 0 && (
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
              {pipelines.map((pipeline, idx) => (
                <tr key={idx}>
                  <td>{pipeline.id}</td>
                  <td>
                    <Link href={`/pipelines/${pipeline.id}`}>
                      <a>{pipeline.name}</a>
                    </Link>
                  </td>
                  <td>{pipeline.warehouse.name}</td>
                  <td>{pipeline.app.name}</td>
                  <td>
                    <Badge bg={pipeline.status === "OK" ? "success" : "danger"}>
                      {pipeline.status}
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

export default Pipelines;
