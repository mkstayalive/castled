export enum AccessType {
  OAUTH = "OAUTH",
  API_KEY = "API_KEY",
  PASSWORD = "PASSWORD",
}

export const AccessTypeLabel: any = {
  [AccessType.OAUTH]: "Oauth",
  [AccessType.API_KEY]: "Api Key",
  [AccessType.PASSWORD]: "Password",
};
