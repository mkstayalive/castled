import React, { Context, useEffect, useState } from "react";
import { PipelineWizardContextDto } from "@/app/common/dtos/context/PipelineWizardContextDto";

export const PIPELINE_WIZARD_PROVIDER_KEY = "PIPELINE_WIZARD_PROVIDER";

type PipelineWizardContextType = PipelineWizardContextDto | undefined;

type PipelineWizardProviderType = {
  pipelineWizContext: PipelineWizardContextType;
  setPipelineWizContext: (
    pipelineWizContext: PipelineWizardContextType
  ) => void;
};

let PipelineWizardContext: Context<PipelineWizardProviderType>;
let { Provider } = (PipelineWizardContext =
  React.createContext<PipelineWizardProviderType>({
    pipelineWizContext: {},
    setPipelineWizContext: () => {},
  }));

export const usePipelineWizContext = () =>
  React.useContext(PipelineWizardContext);

export default function PipelineWizardProvider({ children }: any) {
  const [pipelineWizContext, setPipelineWizContext] =
    useState<PipelineWizardContextType>(undefined);
  useEffect(() => {
    if (pipelineWizContext === undefined) {
      const oldValue = JSON.parse(
        sessionStorage.getItem(PIPELINE_WIZARD_PROVIDER_KEY) || "{}"
      );
      setPipelineWizContext(oldValue);
    } else {
      sessionStorage.setItem(
        PIPELINE_WIZARD_PROVIDER_KEY,
        JSON.stringify(pipelineWizContext)
      );
    }
  }, [JSON.stringify(pipelineWizContext)]);
  return (
    <Provider value={{ pipelineWizContext, setPipelineWizContext }}>
      {children}
    </Provider>
  );
}
