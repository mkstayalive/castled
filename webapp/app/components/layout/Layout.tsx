import Header, { HeaderProps } from "@/app/components/layout/Header";
import React from "react";
import LeftSidebar from "@/app/components/layout/LeftSidebar";
import HeadCommon from "@/app/components/layout/HeadCommon";
import { WizardSteps } from "@/app/common/dtos/internal/WizardSteps";

interface LayoutProps extends HeaderProps {
  children: React.ReactNode;
  steps?: WizardSteps;
  stepGroups?: WizardSteps;
}

const Layout = ({
  title,
  centerTitle,
  pageTitle,
  navLinks,
  rightBtn,
  children,
  steps,
  stepGroups,
}: LayoutProps) => {
  return (
    <div className="layout-holder">
      <HeadCommon title={typeof title === "string" ? title : pageTitle || ""} />
      <LeftSidebar />
      <main>
        <Header
          title={title}
          centerTitle={centerTitle}
          navLinks={navLinks}
          rightBtn={rightBtn}
          steps={steps}
          stepGroups={stepGroups}
        />
        <div className="container-fluid container-main">{children}</div>
      </main>
    </div>
  );
};
export default Layout;
