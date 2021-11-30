import { useSession } from "@/app/common/context/sessionContext";
import { Dropdown, Nav } from "react-bootstrap";
import DropdownCaretDown from "@/app/components/bootstrap/DropdownCaretDown";
import SearchForm from "@/app/components/layout/SearchForm";
import Link from "next/link";
import cn from "classnames";
import router, { useRouter } from "next/router";
import { DebugPipelineWizContext } from "@/app/components/pipeline/DebugPipelineWizContext";
import WizardStepsLayout from "@/app/components/layout/WizardStepsLayout";
import React, { useState } from "react";
import { WizardSteps } from "@/app/common/dtos/internal/WizardSteps";
import { StringAnyMap } from "@/app/common/utils/types";
import buttonHandler from "@/app/common/utils/buttonHandler";

export interface HeaderProps {
  title: string | JSX.Element;
  pageTitle?: string;
  centerTitle?: boolean;
  navLinks?: {
    href: string;
    title: string;
  }[];
  rightBtn?: {
    id: string;
    href?: string;
    title: string;
    onClick?: any;
    dataLayer?: StringAnyMap;
    isLoading?: boolean;
  };
  steps?: WizardSteps;
  stepGroups?: WizardSteps;
}

const Header = ({
  title,
  centerTitle,
  navLinks,
  rightBtn,
  steps,
  stepGroups,
}: HeaderProps) => {
  const router = useRouter();
  const { user } = useSession();

  return (
    <>
      <header>
        <nav className="navbar navbar-expand navbar-light">
          <div className="container-fluid">
            <div className="navbar-nav d-flex">
              <SearchForm />
            </div>
            <ul className="navbar-nav d-flex">
              {user && (
                <Dropdown align="end">
                  <Dropdown.Toggle
                    as={DropdownCaretDown}
                    id="dropdown-custom-components"
                  >
                    <img
                      src={`https://ui-avatars.com/api/?name=${user.name}`}
                      alt={user.name}
                      height={36}
                      className="rounded-circle"
                    />
                    <span>{user.name}</span>
                  </Dropdown.Toggle>
                  <Dropdown.Menu align="end">
                    <Dropdown.Item href="/auth/logout">Logout</Dropdown.Item>
                  </Dropdown.Menu>
                </Dropdown>
              )}
            </ul>
          </div>
        </nav>
        <WizardStepsLayout
          title={title}
          steps={steps}
          stepGroups={stepGroups}
        />
        <div className="container-fluid container-main py-4">
          {title && (
            <div className="d-flex">
              <div className="flex-grow-1">
                <h1
                  className={cn("title-fs fs-3", {
                    "text-center": centerTitle,
                  })}
                >
                  {title}
                </h1>
              </div>
              <div className="">
                {rightBtn && (
                  <button
                    onClick={buttonHandler(
                      { id: rightBtn.id },
                      rightBtn.href
                        ? () => {
                            if (process.browser) {
                              router.push(rightBtn.href!).then();
                            }
                          }
                        : rightBtn.onClick
                    )}
                    className="btn btn-primary float-end"
                  >
                    {rightBtn.title}
                  </button>
                )}
              </div>
            </div>
          )}
          {navLinks?.length && (
            <Nav variant="tabs" defaultActiveKey={navLinks[0].href}>
              {navLinks.map((navLink, i) => (
                <Nav.Item key={i}>
                  <Nav.Link
                    href={navLink.href}
                    className={cn("nav-link", {
                      active: router.pathname === navLink.href,
                    })}
                  >
                    {navLink.title}
                  </Nav.Link>
                </Nav.Item>
              ))}
            </Nav>
          )}
        </div>
      </header>
      <DebugPipelineWizContext />
    </>
  );
};
export default Header;
