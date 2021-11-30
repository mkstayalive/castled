import { PipelineWizardStepProps } from "@/app/components/pipeline/PipelineWizard";
import Layout from "@/app/components/layout/Layout";
import { Form, Formik } from "formik";
import formHandler from "@/app/common/utils/formHandler";
import React, { useEffect, useState } from "react";
import ButtonSubmit from "@/app/components/forminputs/ButtonSubmit";
import warehouseService from "@/app/services/warehouseService";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import Loading from "@/app/components/common/Loading";
import { ExecuteQueryRequestDto } from "@/app/common/dtos/ExecuteQueryRequestDto";
import bannerNotificationService from "@/app/services/bannerNotificationService";
import { ExecuteQueryResultsDto } from "@/app/common/dtos/ExecuteQueryResultsDto";
import { Table } from "react-bootstrap";
import _ from "lodash";
import InputField from "@/app/components/forminputs/InputField";
import { IconPlayerPlay, IconLoader } from "@tabler/icons";
import { Button } from "react-bootstrap";

const WarehouseModel = ({
  curWizardStep,
  steps,
  stepGroups,
  setCurWizardStep,
}: PipelineWizardStepProps) => {
  const [queryResults, setQueryResults] = useState<
    ExecuteQueryResultsDto | undefined
  >();
  const { pipelineWizContext, setPipelineWizContext } = usePipelineWizContext();
  if (!pipelineWizContext) return <Loading />;
  const [query, setQuery] = useState<string | undefined>();
  const warehouseId = pipelineWizContext.values?.warehouseId;
  useEffect(() => {
    const warehouseId = pipelineWizContext.values?.warehouseId;
    if (!warehouseId) {
      setCurWizardStep("source", "selectType");
    }
  }, []);
  if (!warehouseId) return <Loading />;
  const getQueryResults = (queryId: string) => {
    warehouseService
      .executeQueryResults(queryId)
      .then(({ data }) => {
        if (data.status === "PENDING") {
          setTimeout(() => getQueryResults(queryId), 1000);
        }
        setQueryResults(data);
      })
      .catch(() => {
        bannerNotificationService.error("Query failed unexpectedly");
      });
  };
  return (
    <Layout
      title={steps[curWizardStep].title}
      centerTitle={true}
      steps={steps}
      stepGroups={stepGroups}
    >
      <Formik
        key={pipelineWizContext.values?.sourceQuery}
        initialValues={
          {
            warehouseId,
            query: pipelineWizContext.values?.sourceQuery,
          } as ExecuteQueryRequestDto
        }
        onSubmit={formHandler(
          {
            id: "warehouse_query_form",
            pickFieldsForEvent: ["query"],
          },
          warehouseService.executeQuery,
          (res) => {
            getQueryResults(res.queryId);
          }
        )}
      >
        {({ isSubmitting }) => (
          <Form>
            <InputField
              type="textarea"
              minRows={3}
              title="Query"
              name="query"
              onChange={setQuery}
              placeholder="Enter Query..."
              className="border-0 border-bottom mono-font"
            />
            <ButtonSubmit submitting={isSubmitting}>Run Query</ButtonSubmit>
          </Form>
        )}
      </Formik>
      {queryResults &&
        renderQueryResults(queryResults, () => {
          _.set(pipelineWizContext, "values.sourceQuery", query);
          setPipelineWizContext(pipelineWizContext);
          setCurWizardStep("destination", "selectType");
        })}
    </Layout>
  );
};

function renderQueryResults(
  result: ExecuteQueryResultsDto,
  nextStep: () => void
) {
  if (result.status === "PENDING") {
    return (
      <div>
        <p>Query in progress...</p>
        <div className="table-responsive mx-auto mt-2">
          <Table hover>
            <tbody>
              <tr className="pt-4 pb-4">
                <td>
                  <div className="linear-background"></div>
                </td>
                <td>
                  <div className="linear-background"></div>
                </td>
                <td>
                  <div className="linear-background"></div>
                </td>
                <td>
                  <div className="linear-background"></div>
                </td>
              </tr>
            </tbody>
          </Table>
        </div>
      </div>
    );
  } else if (result.status === "FAILED") {
    return <p>Query failed with error: {result.failureMessage}</p>;
  } else if (result.queryResults) {
    return (
      <>
        <div className="d-flex justify-content-end pt-2">
          <Button className="btn" variant="outline-primary" onClick={nextStep}>
            Next
          </Button>
        </div>
        <div className="table-responsive mx-auto mt-2">
          <Table hover>
            <thead>
              <tr>
                {result.queryResults.headers.map((header, i) => (
                  <th key={i}>{header}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {result.queryResults.rows.map((row, i) => (
                <tr key={i}>
                  {row.map((item, j) => (
                    <td key={j}>{item}</td>
                  ))}
                </tr>
              ))}
            </tbody>
          </Table>
        </div>
      </>
    );
  }
}

export default WarehouseModel;
