import { PipelineCreateRequestDto } from "@/app/common/dtos/PipelineCreateRequestDto";
import { ConnectorTypeDto } from "@/app/common/dtos/ConnectorTypeDto";

export interface PipelineWizardContextDto {
  values?: PipelineCreateRequestDto;
  warehouseType?: ConnectorTypeDto;
  appType?: ConnectorTypeDto;
  mappingInfo?: any;
}
