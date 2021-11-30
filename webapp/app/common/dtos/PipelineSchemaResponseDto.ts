export interface PipelineSchemaResponseDto {
  warehouseSchema: {
    schemaName: string;
    fields: {
      fieldName: string;
      type: string;
      optional: true;
    }[];
  };
  appSchema: {
    schemaName: string;
    fields: {
      fieldName: string;
      type: string;
      optional: true;
    }[];
  };
  pkEligibles: {
    eligibles : string[],
    autoDetect : boolean
  }
}
