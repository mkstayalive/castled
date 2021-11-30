export interface ConnectorRequestDto {
  name: string;
  config: { [key: string]: any };
  successUrl?: string;
  failureUrl?: string;
  serverUrl?: string;
}
