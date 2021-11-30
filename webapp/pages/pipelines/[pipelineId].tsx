import React, { useEffect, useState } from "react";
import Layout from "@/app/components/layout/Layout";
import {
  Badge,
  Dropdown,
  Form,
  OverlayTrigger,
  Table,
  Tooltip,
  Button,
  Tabs,
  Tab,
} from "react-bootstrap";
import pipelineService from "@/app/services/pipelineService";

import PipelineRunView from "@/app/components/pipeline/PipelineRunView";
import PipelineMappingView from "@/app/components/pipeline/PipelineMappingView";
import { PipelineResponseDto } from "@/app/common/dtos/PipelineResponseDto";
import { GetServerSidePropsContext } from "next";
import routerUtils from "@/app/common/utils/routerUtils";
import DefaultErrorPage from "next/error";
import Loading from "@/app/components/common/Loading";
import pipelineRunsService from "@/app/services/pipelineRunsService";
import { PipelineRunDto } from "@/app/common/dtos/PipelineRunDto";
import bannerNotificationService from "@/app/services/bannerNotificationService";
import { IconArrowRight, IconDots } from "@tabler/icons";
import {
  PipelineSyncStatus,
  PipelineSyncStatusLabel,
} from "@/app/common/enums/PipelineSyncStatus";
import _ from "lodash";
import DropdownPlain from "@/app/components/bootstrap/DropdownPlain";
import { NextRouter, useRouter } from "next/router";
import renderUtils from "@/app/common/utils/renderUtils";
import PipelineSettings from "@/app/components/pipeline/step4settings/PipelineSettings";
import PipelineSettingsView from "@/app/components/pipeline/PipelineSettingsView";

export async function getServerSideProps({ query }: GetServerSidePropsContext) {
  const pipelineId = routerUtils.getInt(query.pipelineId);
  return {
    props: { pipelineId },
  };
}

interface PipelineInfoProps {
  pipelineId: number;
}

const PipelineInfo = ({ pipelineId }: PipelineInfoProps) => {
  const router = useRouter();
  const [reloadKey, setReloadKey] = useState<number>(0);
  const [pipeline, setPipeline] = useState<
    PipelineResponseDto | undefined | null
  >();
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [pipelineRuns, setPipelineRuns] = useState<
    PipelineRunDto[] | undefined | null
  >();
  useEffect(() => {
    pipelineService
      .getById(pipelineId)
      .then(({ data }) => {
        setPipeline(data);
      })
      .catch(() => {
        setPipeline(null);
      });
    pipelineRunsService
      .getByPipelineId(pipelineId)
      .then(({ data }) => {
        setPipelineRuns(data);
        setIsLoading(false);
      })
      .catch(() => {
        setPipelineRuns(null);
      });
  }, [reloadKey]);


  if (pipeline === null) return <DefaultErrorPage statusCode={404} />;
  return (
    <Layout
      title={renderTitle(pipeline, router, setPipeline, setReloadKey)}
      pageTitle={pipeline ? "Pipeline " + pipeline.name : ""}
      rightBtn={{
        id: "sync_pipeline_button",
        title: "Sync Now",
        isLoading: isLoading,
        onClick: () => {
          setIsLoading(true);
          pipelineService.triggerRun(pipelineId).then(() => {
            setReloadKey(Math.random());
            bannerNotificationService.success("Triggered Run");
          });
        },
      }}
    >
      {!pipeline && <Loading />}
      {pipeline && (
        <div className="mb-4">
          <span>{pipeline.warehouse.name}</span>
          <IconArrowRight className="ms-2 me-2" />
          <span>{pipeline.app.name}</span>
        </div>
      )}

      <Tabs defaultActiveKey="Runs" className="mb-3">
        <Tab eventKey="Runs" title="Runs">
          <PipelineRunView pipelineRuns={pipelineRuns}></PipelineRunView>
        </Tab>
        <Tab eventKey="Mapping" title="Query & Mapping">
          <PipelineMappingView
            sourceQuery={pipeline?.sourceQuery}
            dataMapping={pipeline?.dataMapping}
          ></PipelineMappingView>
        </Tab>
        <Tab eventKey="Schedule" title="Schedule">
          <PipelineSettingsView
            pipelineId={pipeline?.id}
            name={pipeline?.name}
            schedule={pipeline?.jobSchedule}
            queryMode = {pipeline?.queryMode}
          ></PipelineSettingsView>
        </Tab>
      </Tabs>
    </Layout>
  );
};

function renderTitle(
  pipeline: PipelineResponseDto | undefined,
  router: NextRouter,
  setPipeline: (value: any) => void,
  setReloadKey: (value: any) => void
) {
  if (!pipeline) return "";
  const isActive = pipeline.syncStatus === PipelineSyncStatus.ACTIVE;
  return (
    <>
      <span>#{pipeline.id} | </span> <span>{pipeline.name}</span>
      <span className="ms-2">
        <Badge bg={pipeline.status === "OK" ? "success" : "danger"}>
          {pipeline.status}
        </Badge>
      </span>
      <OverlayTrigger
        placement="right"
        key={`pipeline-sync-status-${pipeline.id}`}
        overlay={
          <Tooltip
            id={`pipeline-sync-status-${pipeline.id}`}
            className="tooltip"
          >
            Pipeline is {PipelineSyncStatusLabel[pipeline.syncStatus]}
          </Tooltip>
        }
      >
        <Form.Check
          className="d-inline-block ms-4"
          type="switch"
          id="pipeline-switch"
          checked={isActive}
          onChange={() => {
            const pipelineNew = _.cloneDeep(pipeline);
            if (isActive) {
              pipelineService.pause(pipeline.id).then(() => {
                if (pipeline) {
                  pipelineNew.syncStatus = PipelineSyncStatus.PAUSED;
                  setPipeline(pipelineNew);
                }
                bannerNotificationService.success("Pipeline Paused");
              });
            } else {
              pipelineService.resume(pipeline.id).then(() => {
                if (pipeline) {
                  pipelineNew.syncStatus = PipelineSyncStatus.ACTIVE;
                  setPipeline(pipelineNew);
                }
                bannerNotificationService.success("Pipeline Resumed");
              });
            }
          }}
        />
      </OverlayTrigger>
      <Dropdown align="end" className="d-inline-block float-end me-4">
        <Dropdown.Toggle as={DropdownPlain} id="dropdown-custom-components">
          <IconDots />
        </Dropdown.Toggle>
        <Dropdown.Menu align="end">
          <Dropdown.Item
            onClick={() => {
              pipelineService.restart(pipeline.id).then(() => {
                setReloadKey(Math.random());
                bannerNotificationService.success("Pipeline Restarted");
              });
            }}
          >
            Restart
          </Dropdown.Item>
          <Dropdown.Item
            onClick={() => {
              pipelineService.delete(pipeline.id).then(() => {
                bannerNotificationService.success("Pipeline Deleted");
                router.push("/pipelines").then();
              });
            }}
          >
            Delete
          </Dropdown.Item>
        </Dropdown.Menu>
      </Dropdown>
    </>
  );
}

export default PipelineInfo;
