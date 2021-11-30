import { AccessType } from "@/app/common/enums/AccessType";

export interface ConnectorDto {
  id: number;
  name: string;
  teamId: number;
  config: {
    type: string;
    [key: string]: string;
  };
  status: string;
  type: string;
  logoUrl: string;
  docUrl: string;
  accessType: AccessType;
  pipelines: number;
}
