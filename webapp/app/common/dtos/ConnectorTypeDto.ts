import { FormFieldsDto } from "@/app/common/dtos/FormFieldsDto";
import { SelectOptionDto } from "@/app/common/dtos/SelectOptionDto";
import { AccessType } from "@/app/common/enums/AccessType";

export interface ConnectorTypeDto extends SelectOptionDto {
  accessType: AccessType;
  logoUrl: string;
  docUrl: string;
  count: number;
}
