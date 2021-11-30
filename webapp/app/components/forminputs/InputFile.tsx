import React, { useEffect, useState } from "react";
import { FieldInputProps, useField } from "formik";
import cn from "classnames";
import { InputBaseProps } from "@/app/common/dtos/InputBaseProps";
import { AxiosResponse } from "axios";
import { DataFetcherResponseDto } from "@/app/common/dtos/DataFetcherResponseDto";
import TextareaAutosize from "react-textarea-autosize";
import exp from "constants";

export interface InputFileProps extends InputBaseProps {
    type: string;//only json for now
}

const InputField = ({
    title,
    description,
    className,
    onChange,
    setFieldValue,
    setFieldTouched,
    type,
    ...props
}: InputFileProps) => {

    const [field, meta] = useField(props);

    return (
        <div className="mb-3">
            {title && (
                <label htmlFor={props.id || props.name} className="form-label">
                    {title}
                </label>
            )}
            <input id="file" name="file" type="file" onChange={(event) => {
                if (event.currentTarget.files) {
                    const fileReader = new FileReader();
                    fileReader.onload = () => {
                        if (fileReader.readyState === 2) {
                            if (type === 'json') {
                                setFieldValue(field.name, JSON.parse(fileReader.result! as string));
                            }
                            if (type === 'text') {
                                setFieldValue(field.name, fileReader.result!);
                            }
                        }
                    };
                    fileReader.readAsText(event.currentTarget.files[0]!);
                }
            }} className="form-control" />
        </div>
    );

}

export default InputField;