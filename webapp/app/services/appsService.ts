import http from "@/app/services/http";
import { AxiosResponse } from "axios";
import { FormFieldsDto } from "../common/dtos/FormFieldsDto";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";
import { OauthResponseDto } from "@/app/common/dtos/OauthResponseDto";
import { ConnectorRequestDto } from "@/app/common/dtos/ConnectorRequestDto";
import { ConnectorDto } from "@/app/common/dtos/ConnectorDto";
import { EntityCreatedResponseDto } from "@/app/common/dtos/EntityCreatedResponseDto";
import { DataFetcherRequestDto } from "@/app/common/utils/types";
import { DataFetcherResponseDto } from "@/app/common/dtos/DataFetcherResponseDto";
import httpUtils from "@/app/common/utils/httpUtils";

export default {
  getById: (id: number): Promise<AxiosResponse<ConnectorDto>> => {
    return http.get(`/v1/apps/${id}`);
  },
  get: (type?: string): Promise<AxiosResponse<ConnectorDto[]>> => {
    return http.get("/v1/apps", { type });
  },
  create: (
    request: ConnectorRequestDto
  ): Promise<AxiosResponse<EntityCreatedResponseDto>> => {
    return http.post("/v1/apps", request);
  },
  update: (
    id: number,
    request: ConnectorRequestDto
  ): Promise<AxiosResponse<EntityCreatedResponseDto>> => {
    return http.put(`/v1/apps/${id}`, request);
  },
  createOauth: (
    request: ConnectorRequestDto
  ): Promise<AxiosResponse<OauthResponseDto>> => {
    return http.post("/v1/apps/oauth", request);
  },
  updateOauth: (
    id: number,
    request: ConnectorRequestDto
  ): Promise<AxiosResponse<OauthResponseDto>> => {
    return http.put(`/v1/apps/oauth/${id}`, request);
  },
  formFields: (type: string): Promise<AxiosResponse<FormFieldsDto>> => {
    return http.get("/v1/apps/form-fields", { type });
  },
  mappingFormFields: (type: string): Promise<AxiosResponse<FormFieldsDto>> => {
    return http.get("/v1/apps/mapping-form-fields", { type });
  },
  types: (): Promise<AxiosResponse<ConnectorTypeDto[]>> => {
    return http.get("/v1/apps/types");
  },
  dynamicFieldValues: (
    optionsRef: string,
    data: DataFetcherRequestDto | undefined
  ): Promise<AxiosResponse<DataFetcherResponseDto>> => {
    return http.post(
      "/v1/apps/app-sync-options" + httpUtils.param({ optionsRef }),
      { ...data }
    );
  },
};
