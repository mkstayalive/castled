import { FormFieldsDto } from "@/app/common/dtos/FormFieldsDto";
import { Form, Formik, FormikValues } from "formik";
import InputField from "@/app/components/forminputs/InputField";
import DynamicFormFields from "@/app/components/connectors/DynamicFormFields";
import ButtonSubmit from "@/app/components/forminputs/ButtonSubmit";
import React, { useEffect, useState } from "react";
import formHandler from "@/app/common/utils/formHandler";
import appsService from "@/app/services/appsService";
import bannerNotificationService from "@/app/services/bannerNotificationService";
import { AccessType } from "@/app/common/enums/AccessType";
import { ConnectorCategory } from "@/app/common/utils/types";
import warehouseService from "@/app/services/warehouseService";
import { useRouter } from "next/router";
import routerUtils from "@/app/common/utils/routerUtils";
import { Card } from "react-bootstrap";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import { ConnectorDto } from "@/app/common/dtos/ConnectorDto";
import { GetServerSidePropsContext } from "next";

import stringUtils from "@/app/common/utils/stringUtils";

const API_BASE = process.env.API_BASE || "";

export interface ConnectorFormProps {
  appBaseUrl: string;
  editConnector?: ConnectorDto | null;
  category: ConnectorCategory;
  connectorType: string;
  docUrl?: string;
  oauthCallback?: string;
  accessType: AccessType;
  onFinish: (id: number) => void;
}

const ConnectorForm = ({
  appBaseUrl,
  editConnector,
  category,
  connectorType,
  docUrl,
  accessType,
  oauthCallback,
  onFinish
}: ConnectorFormProps) => {
  const router = useRouter();
  const { pipelineWizContext } = usePipelineWizContext();
  const wizardStep = routerUtils.getString(router.query.wizardStep);
  const id = routerUtils.getInt(router.query.id);
  const type = connectorType || routerUtils.getInt(router.query.type);
  const [formFields, setFormFields] = useState<FormFieldsDto | undefined>();
  useEffect(() => {
    if (!pipelineWizContext) return;
    if (id) {
      onFinish(id);
      return;
    }
    if (type) {
      const fetcher =
        category === "App"
          ? appsService.formFields
          : warehouseService.formFields;
      fetcher(type).then(({ data }) => {
        setFormFields(data);
      });
    }
  }, [!!pipelineWizContext, type, id]);

  const isOauth = accessType === AccessType.OAUTH;
  const createConnector: any =
    category === "Warehouse"
      ? warehouseService.create
      : isOauth
      ? appsService.createOauth
      : appsService.create;
  const updateConnector: any =
    category === "Warehouse"
      ? warehouseService.update
      : isOauth
      ? appsService.updateOauth
      : appsService.update;
  const fetcher = editConnector
    ? (data: any) => updateConnector(editConnector.id, data)
    : createConnector;
  const onSubmit = formHandler(
    {
      id: "connector_form",
      pickFieldsForEvent: ["name", "config.type"],
      dataLayer: { connectorCategory: category }
    },
    fetcher,
    (res: any) => {
      if (res?.redirectUrl) {
        window.location = res.redirectUrl;
      } else {
        bannerNotificationService.success("Success");
        onFinish(res.id);
      }
    },
    undefined,
    (values: any): any => {
      if (isOauth) {
        const defaultOauthUrl = `${appBaseUrl}${
          (location?.pathname || router.pathname) +
          "?wizardStep=" +
          wizardStep +
          "&type=" +
          values.config.type +
          "&success=1"
        }`;

        return {
          ...values,
          successUrl: oauthCallback
            ? `${appBaseUrl}${oauthCallback}`
            : defaultOauthUrl,
          failureUrl: `${appBaseUrl}${
            oauthCallback +
            "?wizardStep=" +
            wizardStep +
            "&type=" +
            values.config.type +
            "&failed=1"
          }`,
          serverUrl: `${appBaseUrl}${API_BASE}`
        };
      }
      return values;
    }
  );

  const submitLabel = !editConnector
    ? `Add ${category}`
    : isOauth
    ? "Reauthorize"
    : "Save";

  const getCodeBlock = (formFields: FormFieldsDto, values: FormikValues) => {
    if (
      formFields.codeBlock?.dependencies.filter(
        (dependencyRef) => values.config[dependencyRef]
      ).length !== formFields.codeBlock?.dependencies.length
    ) {
      return null;
    }
    return (
      <div className="mt-3">
        <label> {formFields?.codeBlock?.title}</label>
        {formFields.codeBlock?.snippets.map((codeSnippet) => (
          <Card.Body>
            <Card.Header>
              <strong>{codeSnippet.title}</strong>
            </Card.Header>
            <Card.Footer>
              {stringUtils.replaceTemplate(codeSnippet.code, values.config)}
            </Card.Footer>
          </Card.Body>
        ))}
      </div>
    );
  };

  return (
    <Formik
      initialValues={
        editConnector || { name: "", config: { type: connectorType } }
      }
      onSubmit={onSubmit}
    >
      {({ values, setFieldValue, setFieldTouched, isSubmitting }) => (
        <Form>
          <InputField
            type="text"
            name="name"
            title="Name"
            placeholder="Enter name"
          />
          <InputField type="hidden" name="config.type" title="Type" />
          {connectorType && (
            <DynamicFormFields
              namePrefix="config"
              skipNames={["name"]}
              formFields={formFields}
              values={values}
              setFieldValue={setFieldValue}
              setFieldTouched={setFieldTouched}
              dataFetcher={(optionsRef) =>
                warehouseService.configOptions(optionsRef, values)
              }
            />
          )}
          {formFields?.codeBlock && getCodeBlock(formFields, values)}
          <ButtonSubmit submitting={isSubmitting} className="mb-3">
            {submitLabel}
          </ButtonSubmit>
          {docUrl && (
            <p>
              Need help? Read our{" "}
              <a href={docUrl} target="_blank">
                doc
              </a>
            </p>
          )}
        </Form>
      )}
    </Formik>
  );
};

export default ConnectorForm;
