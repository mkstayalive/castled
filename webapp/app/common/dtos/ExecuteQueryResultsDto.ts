export interface ExecuteQueryResultsDto {
  status: "PENDING" | "SUCCEEDED" | "FAILED";
  failureMessage: string;
  queryResults: {
    headers: string[];
    rows: string[][];
  };
}
