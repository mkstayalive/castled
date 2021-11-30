export interface WizardSteps {
  [key: string]: {
    title: string;
    description: string;
    stepKey?: string;
  };
}
