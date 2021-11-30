import { PipelineCreateRequestDto } from "@/app/common/dtos/PipelineCreateRequestDto";

export type StringAnyMap = { [key: string]: any };
export type BootstrapSize = "xs" | "sm" | "md" | "lg" | "xl";
export type BootstrapColor =
  | "success"
  | "default"
  | "danger"
  | "warning"
  | "primary"
  | "secondary"
  | "glow";

export type ConnectorCategory = "App" | "Warehouse";
export type DataFetcherRequestDto = PipelineCreateRequestDto;
