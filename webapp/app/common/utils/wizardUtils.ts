import { NextRouter } from "next/router";

export default {
  setCurWizardStep(
    router: NextRouter,
    stepGroup: string | undefined,
    step: string
  ) {
    const stepKey = (stepGroup ? stepGroup + ":" : "") + step;
    process.browser &&
      router.push(router.pathname + "?wizardStep=" + stepKey).then();
  },
  getWizardStepAndGroup(wizardStepKey: string): [string | undefined, string] {
    if (wizardStepKey.indexOf(":") !== -1) {
      const parts = wizardStepKey.split(":");
      return [parts[0], parts[1]];
    } else {
      return [undefined, wizardStepKey];
    }
  },
  getBackLink(wizardStepKey: string, stepKeysSequence: string[] | undefined) {
    if (!stepKeysSequence) return undefined;
    const pos = stepKeysSequence.indexOf(wizardStepKey);
    if (pos === -1 || pos === 0) return undefined;
    return stepKeysSequence[pos - 1];
  },
};
