import React, { useEffect, useState } from "react";
import { useField } from "formik";
import { InputBaseProps } from "@/app/common/dtos/InputBaseProps";
import { SelectOptionDto } from "@/app/common/dtos/SelectOptionDto";
import _, { values } from "lodash";
import { AxiosResponse } from "axios";

import { ObjectUtils } from "@/app/common/utils/objectUtils";

import { DataFetcherResponseDto } from "@/app/common/dtos/DataFetcherResponseDto";
import Select from "react-select";
import cn from "classnames";

export interface InputSelectOptions extends InputBaseProps {
  options: SelectOptionDto[] | undefined;
  values: any;
  dValues?: any[];
  setFieldValue: (field: string, value: any, shouldValidate?: boolean) => void;
  setFieldTouched: (
    field: string,
    isTouched?: boolean,
    shouldValidate?: boolean
  ) => void;
  optionsRef?: string;
  deps?: string[];
  dataFetcher?: (
    optionsRef: string
  ) => Promise<AxiosResponse<DataFetcherResponseDto>>;
  hidden?: boolean;
}

const InputSelect = ({
  title,
  description,
  options,
  onChange,
  optionsRef,
  deps,
  setFieldValue,
  setFieldTouched,
  dataFetcher,
  values,
  dValues,
  ...props
}: InputSelectOptions) => {
  const [field, meta] = useField(props);
  const [optionsDynamic, setOptionsDynamic] = useState(options);

  const [optionsLoading, setOptionsLoading] = useState(false);

  const depValues = dValues? dValues : [];

  useEffect(() => {
    if (optionsRef) {
      setOptionsLoading(true);
      dataFetcher?.(optionsRef).then(({ data }) => {
        if (data.options?.length === 1) {
          setFieldValue?.(field.name, data.options[0].value);
        }
        setOptionsDynamic(data.options);
        setOptionsLoading(false);
      });
    } else {
      if (options?.length === 1) {
        setFieldValue?.(field.name, options[0].value);
      }
      setOptionsDynamic(options);
    }
  }, [optionsRef, ...depValues]);
  return (
    <div className={cn("mb-3", { "d-none": props.hidden })}>
      {title && (
        <label htmlFor={props.id || props.name} className="form-label">
          {title}
        </label>
      )}
      <Select
        {...props}
        options={
          !optionsDynamic
            ? [{ label: "Loading.." }]
            : optionsDynamic.map((o) => ({
              value: o.value,
              label: o.title,
            }))
        }
        onChange={(v) => setFieldValue?.(field.name, v?.value)}
        onBlur={() => setFieldTouched?.(field.name, true)}
        value={
          optionsLoading || !optionsDynamic
            ? { label: "Loading..." }
            : {
              value: field.value,
              label: optionsDynamic
                .filter((o) => ObjectUtils.objectEquals(o.value, field.value))
                .map((o) => o.title),
            }
        }
      />
      {meta.touched && meta.error ? (
        <div className="error">{meta.error}</div>
      ) : null}
    </div>
  );
};
export default InputSelect;
