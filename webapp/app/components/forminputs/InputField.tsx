import React, { useEffect, useState } from "react";
import { FieldInputProps, useField } from "formik";
import cn from "classnames";
import { InputBaseProps } from "@/app/common/dtos/InputBaseProps";
import { AxiosResponse } from "axios";
import { DataFetcherResponseDto } from "@/app/common/dtos/DataFetcherResponseDto";
import TextareaAutosize from "react-textarea-autosize";

export interface InputFieldProps extends InputBaseProps {
  type: string;
  minRows?: number;
  optionsRef?: string;
  dataFetcher?: (
    optionsRef: string
  ) => Promise<AxiosResponse<DataFetcherResponseDto>>;
}

const InputField = ({
  title,
  description,
  className,
  onChange,
  setFieldValue,
  setFieldTouched,
  optionsRef,
  dataFetcher,
  ...props
}: InputFieldProps) => {
  const [field, meta] = useField(props);
  const isHidden = props.type === "hidden";

  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (optionsRef) {
      setLoading(true);
      dataFetcher?.(optionsRef).then(({ data }) => {    
        setFieldValue(field.name, data.options[0].value);
        setLoading(false);
      });

    }
  }, [optionsRef]);

  return (
    <div className={className ? className : cn({ "mb-3": !isHidden })}>
      {title && !isHidden && (
        <label htmlFor={props.id || props.name} className="form-label">
          {title}
        </label>
      )}
      {getInput(field, onChange, props, optionsRef)}
      <div className={cn({'spinner-border spinner-border-sm': loading && !isHidden})}></div>
      {meta.touched && meta.error ? (
        <div className="error">{meta.error}</div>
      ) : null}
    </div>
  );
};

function getInput(
  field: FieldInputProps<any>,
  onChange: ((value: string) => void) | undefined,
  props: any,
  optionsRef?: string,
) {
  if (props.type === "textarea") {
    return (
      <TextareaAutosize
        onChange={(e) => {
          field.onChange(e);
          onChange?.(e.currentTarget.value);
        }}
        {...props}
        className={cn(props.className, "form-control")}
        defaultValue={field.value}
      />
    );
  } else {
    return (
      <input
        onChange={(e) => {
          field.onChange(e);
          onChange?.(e.currentTarget.value);
        }}
        {...props}
        className={cn(props.className, "form-control")}
        value={field.value}
        defaultValue={field.value}
        disabled = {optionsRef}
      />
    );
  }
}

export default InputField;
