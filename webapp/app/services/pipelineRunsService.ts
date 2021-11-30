import http from "@/app/services/http";
import { AxiosResponse } from "axios";
import { PipelineRunDto } from "@/app/common/dtos/PipelineRunDto";
import fileDownload from "js-file-download";

export default {
  getById: (runId: number): Promise<AxiosResponse<PipelineRunDto[]>> => {
    return http.get(`/v1/pipeline-runs/${runId}`);
  },
  downloadErrorsById: (
    runId: number
  ): Promise<AxiosResponse<PipelineRunDto[]>> => {
    return http.get(`/v1/pipeline-runs/${runId}/download-errors`);
  },
  getErrorsById: (runId: number): Promise<AxiosResponse<PipelineRunDto[]>> => {
    return http.get(`/v1/pipeline-runs/${runId}/errors`);
  },
  getByPipelineId: (
    pipelineId: number
  ): Promise<AxiosResponse<PipelineRunDto[]>> => {
    return http.get(`/v1/pipeline-runs/pipelines/${pipelineId}`);
  },
  downloadErrorReport: (pipelineRunId: number): void => {
    let uuid: string = Math.random().toString(16).slice(2);
    http
      .get(`/v1/pipeline-runs/${pipelineRunId}/download-errors`)
      .then((response) => {
        fileDownload(response.data, `castled_${uuid}.csv`);
      })
      .catch((error) => {
        console.log(error);
      });
  },
};
