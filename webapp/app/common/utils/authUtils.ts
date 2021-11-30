import bannerNotification from "@/app/services/bannerNotificationService";
import { ExternalLoginType } from "@/app/common/enums/ExternalLoginType";
import httpUtils from "@/app/common/utils/httpUtils";

const API_BASE = process.env.API_BASE || "";

const getOauthParams = (
  appBaseUrl: string,
  successUrl: string,
  failureUrl: string
) => {
  return httpUtils.param({
    serverUrl: `${appBaseUrl}${API_BASE}/`,
    successUrl,
    failureUrl
  });
};

export default {
  logoutUnauthenticated: (apiUrl?: string) => {
    const { pathname } = window.location;
    const apiBase = process.env.API_BASE;
    if (
      pathname.startsWith("/auth") ||
      apiUrl === `${apiBase}/v1/users/whoami` ||
      apiUrl === `${apiBase}/v1/users/logout`
    ) {
      return;
    }
    bannerNotification.error("Not authenticated");
    window.location.href = "/auth/logout";
  },
  getExternalLoginUrl: (
    appBaseUrl: string,
    loginType: ExternalLoginType,
    pathName: string
  ) => {
    return (
      `${API_BASE}/v1/users/${loginType}/signin` +
      getOauthParams(appBaseUrl, appBaseUrl, appBaseUrl + "/" + pathName)
    );
  }
};
