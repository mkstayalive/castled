import { ConnectorRequestDto } from "../common/dtos/ConnectorRequestDto";
import http from "@/app/services/http";
import { AxiosResponse } from "axios";
import { FormFieldsDto } from "../common/dtos/FormFieldsDto";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";
import { ConnectorDto } from "@/app/common/dtos/ConnectorDto";
import { EntityCreatedResponseDto } from "@/app/common/dtos/EntityCreatedResponseDto";
import { ExecuteQueryResponseDto } from "@/app/common/dtos/ExecuteQueryResponseDto";
import { ExecuteQueryRequestDto } from "@/app/common/dtos/ExecuteQueryRequestDto";
import { ExecuteQueryResultsDto } from "@/app/common/dtos/ExecuteQueryResultsDto";
import { DataFetcherResponseDto } from "@/app/common/dtos/DataFetcherResponseDto";
import httpUtils from "@/app/common/utils/httpUtils";

export default {
  getById: (id: number): Promise<AxiosResponse<ConnectorDto>> => {
    return http.get(`/v1/warehouses/${id}`);
  },
  get: (type?: string): Promise<AxiosResponse<ConnectorDto[]>> => {
    return http.get("/v1/warehouses", { type });
  },
  create: (
    request: ConnectorRequestDto
  ): Promise<AxiosResponse<EntityCreatedResponseDto>> => {
    return http.post("/v1/warehouses", request);
  },
  update: (
    id: number,
    request: ConnectorRequestDto
  ): Promise<AxiosResponse<EntityCreatedResponseDto>> => {
    return http.put(`/v1/warehouses/${id}`, request);
  },
  formFields: (type: string): Promise<AxiosResponse<FormFieldsDto>> => {
    return http.get("/v1/warehouses/form-fields", { type });
  },
  types: (): Promise<AxiosResponse<ConnectorTypeDto[]>> => {
    return http.get("/v1/warehouses/types");
  },
  executeQuery: ({
    warehouseId,
    query,
  }: ExecuteQueryRequestDto): Promise<
    AxiosResponse<ExecuteQueryResponseDto>
  > => {
    return http.put(`/v1/warehouses/${warehouseId}/query-preview`, { query });
  },
  executeQueryResults: (
    queryId: string
  ): Promise<AxiosResponse<ExecuteQueryResultsDto>> => {
    return http.get(`/v1/warehouses/queries/${queryId}/results`);
  },

  configOptions: (
    optionsRef: string,
    data: ConnectorRequestDto | undefined
  ): Promise<AxiosResponse<DataFetcherResponseDto>> => {
    return http.post(
      "/v1/warehouses/config-options" + httpUtils.param({ optionsRef }),
      { ...data }
    );
  },
};
