import { FormFieldType } from "../enums/FormFieldType";
import { CodeBlock } from "./CodeBlock";

export interface FormFieldsDto {
  fields: {
    [key: string]: {
      group: string;
      fieldProps: {
        type: FormFieldType;
        title: string;
        description: string;
        placeholder?: string;
        optionsRef?: string;
      };
      validations: {
        required: boolean;
      };
    };
  };
  helpText? : string,
  codeBlock? : CodeBlock,
  groupActivators: {
    [key: string]: {
      dependencies: string[];
      condition? : string;
    };
  };
}
