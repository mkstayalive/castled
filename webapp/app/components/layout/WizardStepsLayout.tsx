import { WizardSteps } from "@/app/common/dtos/internal/WizardSteps";
import Link from "next/link";
import { IconArrowNarrowLeft } from "@tabler/icons";
import { useRouter } from "next/router";
import routerUtils from "@/app/common/utils/routerUtils";
import wizardUtils from "@/app/common/utils/wizardUtils";
import { WizardStepDisplayDto } from "@/app/common/dtos/internal/WizardStepDisplayDto";
import cn from "classnames";
import CircularProgress from "@/app/common/utils/CircularProgress";

interface WizardStepsLayoutProps {
  steps?: WizardSteps;
  stepGroups?: WizardSteps;
  title: string | JSX.Element;
}

const WizardStepsLayout = ({
  title,
  steps,
  stepGroups,
}: WizardStepsLayoutProps) => {
  const router = useRouter();
  const wizardStepKey = routerUtils.getString(router.query.wizardStep);
  if (!wizardStepKey) return null;
  const stepsDisplay = getStepsDisplay(wizardStepKey, steps, stepGroups);
  const percentage: number = getStepList(steps, title);

  return (
    <div className="wizard-header">
      <ul className="nav">
        <li className="nav-item">
          <a className="nav-link btn" onClick={() => router.back()}>
            <IconArrowNarrowLeft />
            GO BACK
          </a>
        </li>
        {stepsDisplay.map((step, i) => (
          <li className="nav-item" key={i}>
            <Link href={step.href}>
              <a
                className={cn("nav-link", {
                  disabled: step.active,
                  nextDisabled: !step.active ? !step.success : false,
                  active: step.active,
                  success: step.success,
                })}
              >
                <CircularProgress
                  size={25}
                  color="#66c9be"
                  active={step.active}
                  percentage={step.success ? 100 : step.active ? percentage : 0}
                  strokeWidth={1}
                  textValue={`${i + 1}`}
                />
                <span className="ml-2">{step.title}</span>
              </a>
            </Link>
          </li>
        ))}
      </ul>
    </div>
  );
};

const getStepList = (
  steps: WizardSteps | undefined,
  title: string | JSX.Element
): number => {
  if (!steps) return 0;
  let percentage = 0;
  const stepKeys = Object.keys(steps);
  const list = stepKeys.map((key: string) => steps[key]);
  let flag = true;
  list.forEach((v, i) => {
    if (v.title === title) {
      flag = false;
    } else if (flag) {
      percentage = ((i + 1) * 100) / list.length;
    }
  });
  return percentage;
};

const getStepsDisplay = (
  wizardStepKey: string,
  steps?: WizardSteps,
  stepGroups?: WizardSteps
): WizardStepDisplayDto[] => {
  const [wizardStepGroup, wizardStep] =
    wizardUtils.getWizardStepAndGroup(wizardStepKey);
  const stepsDisplay: WizardStepDisplayDto[] = [];
  const stepsEffective = stepGroups || steps || {};
  const keys = Object.keys(stepsEffective);
  let success = true;
  let disabled = false;
  for (const key of keys) {
    const stepInfo = stepsEffective[key];
    const active = key === (wizardStepGroup || wizardStep);
    if (active) {
      success = false;
    }
    stepsDisplay.push({
      title: stepInfo.title,
      href: "?wizardStep=" + (stepInfo.stepKey || key),
      active,
      disabled: false,
      success: success,
    });
    if (active) disabled = true;
  }
  return stepsDisplay;
};

export default WizardStepsLayout;
