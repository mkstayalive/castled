import { PipelineWizardStepProps } from "@/app/components/pipeline/PipelineWizard";
import Layout from "@/app/components/layout/Layout";
import React, { useEffect, useState } from "react";
import pipelineService from "@/app/services/pipelineService";
import { usePipelineWizContext } from "@/app/common/context/pipelineWizardContext";
import { PipelineSchemaResponseDto } from "@/app/common/dtos/PipelineSchemaResponseDto";
import bannerNotificationService from "@/app/services/bannerNotificationService";
import { Table } from "react-bootstrap";
import { IconTrash } from "@tabler/icons";
import InputSelect from "@/app/components/forminputs/InputSelect";
import InputField from "@/app/components/forminputs/InputField";
import _ from "lodash";
import { Form, Formik } from "formik";
import InputCheckbox from "@/app/components/forminputs/InputCheckbox";
import Loading from "@/app/components/common/Loading";
import classNames from "classnames";
import {
  FieldMapping,
  PipelineMappingDto,
} from "@/app/common/dtos/PipelineCreateRequestDto";
import ButtonSubmit from "@/app/components/forminputs/ButtonSubmit";
import Placeholder from "react-bootstrap/Placeholder";

interface MappingInfo {
  [warehouseKey: string]: {
    appField: string;
    isPrimaryKey: boolean;
  };
}

const PipelineMapping = ({
  curWizardStep,
  steps,
  stepGroups,
  setCurWizardStep,
}: PipelineWizardStepProps) => {
  const { pipelineWizContext, setPipelineWizContext } = usePipelineWizContext();
  const [pipelineSchema, setPipelineSchema] = useState<
    PipelineSchemaResponseDto | undefined
  >();
  const [isLoading, setIsLoading] = useState<boolean>(true);
  useEffect(() => {
    if (!pipelineWizContext) return;
    if (!pipelineWizContext.values) {
      setCurWizardStep("source", "selectType");
      return;
    }
    pipelineService
      .getSchemaForMapping(pipelineWizContext.values)
      .then(({ data }) => {
        setIsLoading(false);
        setPipelineSchema(data);
      })
      .catch(() => {
        setIsLoading(false);
        bannerNotificationService.error("Unable to load schemas");
      });
  }, [pipelineWizContext?.values]);
  if (!pipelineWizContext) {
    return <Loading />;
  }
  const appSchemaFields = pipelineSchema?.appSchema?.fields.map((field) => ({
    value: field.fieldName,
    title: field.fieldName,
  }));
  const transformMapping = (mappingInfo: MappingInfo): PipelineMappingDto => {
    const fieldMappings: FieldMapping[] = [];
    const primaryKeys: string[] = [];
    _.each(mappingInfo, (value, key) => {
      if (value.appField) {
        fieldMappings.push({
          warehouseField: key,
          appField: value.appField,
          skipped: false,
        });
      }
      if (value.isPrimaryKey) {
        primaryKeys.push(value.appField);
      }
    });
    return {
      primaryKeys,
      fieldMappings,
    };
  };

  const initialMappingInfo: MappingInfo = (pipelineWizContext.mappingInfo ||
    {}) as MappingInfo;
  if (!appSchemaFields) {
    pipelineSchema?.warehouseSchema.fields.map(
      (field) =>
        (initialMappingInfo[field.fieldName] = {
          appField: field.fieldName,
          isPrimaryKey: false,
        })
    );
  }

  return (
    <Layout
      title={steps[curWizardStep].title}
      centerTitle={true}
      steps={steps}
      stepGroups={stepGroups}
    >
      <div className="table-responsive">
        <Formik
          initialValues={initialMappingInfo}
          onSubmit={(values, { setSubmitting }) => {
            if (!pipelineWizContext.values) return setSubmitting(false);
            pipelineWizContext.mappingInfo = values;
            pipelineWizContext.values.mapping = transformMapping(values);
            if (
              pipelineWizContext.values.mapping.primaryKeys?.length == 0 &&
              !pipelineSchema?.pkEligibles.autoDetect
            ) {
              setSubmitting(false);
              bannerNotificationService.error(
                "Atleast one primary key should be selected"
              );
              return;
            }
            setPipelineWizContext(pipelineWizContext);
            setCurWizardStep(undefined, "settings");
            setSubmitting(false);
          }}
        >
          {({ values, setFieldValue, setFieldTouched, isSubmitting }) => (
            <Form>
              <Table hover>
                <thead>
                  <tr>
                    <th>Warehouse Column</th>
                    <th>App Column</th>
                    {!pipelineSchema?.pkEligibles.autoDetect && (
                      <th>Primary Key</th>
                    )}
                  </tr>
                </thead>
                <tbody>
                  {pipelineSchema
                    ? pipelineSchema.warehouseSchema.fields.map((field, i) => (
                        <Placeholder as="tr" animation="glow" key={i}>
                          <Placeholder as="td">{field.fieldName}</Placeholder>
                          {appSchemaFields && (
                            <Placeholder as="td">
                              <InputSelect
                                title={undefined}
                                options={appSchemaFields}
                                deps={undefined}
                                values={values}
                                setFieldValue={setFieldValue}
                                setFieldTouched={setFieldTouched}
                                name={field.fieldName + ".appField"}
                              />
                            </Placeholder>
                          )}
                          {!appSchemaFields && (
                            <Placeholder as="td">
                              <InputField
                                type="text"
                                title={undefined}
                                values={values}
                                setFieldValue={setFieldValue}
                                setFieldTouched={setFieldTouched}
                                name={field.fieldName + ".appField"}
                              />
                            </Placeholder>
                          )}
                          <Placeholder
                            as="td"
                            className={classNames({
                              "d-none": pipelineSchema.pkEligibles.autoDetect,
                            })}
                          >
                            <InputCheckbox
                              title={undefined}
                              name={field.fieldName + ".isPrimaryKey"}
                              disabled={
                                pipelineSchema.pkEligibles.eligibles.length >
                                  0 &&
                                (_.get(
                                  values,
                                  field.fieldName + ".appField"
                                ) === undefined ||
                                  !pipelineSchema.pkEligibles.eligibles?.includes(
                                    _.get(values, field.fieldName).appField
                                  ))
                              }
                              defaultValue={false}
                            />
                          </Placeholder>
                          <Placeholder as="td">
                            <IconTrash
                              className={classNames({
                                "d-none":
                                  _.get(
                                    values,
                                    field.fieldName + ".appField"
                                  ) === undefined,
                              })}
                              onClick={() => {
                                setFieldValue(field.fieldName, "");
                              }}
                            ></IconTrash>
                          </Placeholder>
                        </Placeholder>
                      ))
                    : isLoading && (
                        <tr>
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
                      )}
                </tbody>
              </Table>
              <ButtonSubmit submitting={isSubmitting} />
            </Form>
          )}
        </Formik>
      </div>
    </Layout>
  );
};

export default PipelineMapping;
