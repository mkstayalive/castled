import axios from "axios";
import { Params } from "../common/enums/Params";
import httpUtils from "../common/utils/httpUtils";
import bannerNotification from "./bannerNotificationService";
import authUtils from "@/app/common/utils/authUtils";

axios.interceptors.request.use((config) => {
  config.url = (process.env.API_BASE || "") + config.url;
  return config;
});

axios.interceptors.response.use(undefined, (error) => {
  if (!error.response) {
    return Promise.reject(error);
  }
  const { status } = error.response;
  const { url } = error.response.config;
  if (status === 504) {
    const tillStr = sessionStorage.getItem(Params.SUPPRESS_CONNECT_ERROR_TILL);
    const till = tillStr ? new Date(tillStr) : undefined;
    if (!till || new Date().getTime() > till.getTime()) {
      bannerNotification.error("Unable to connect to Castled servers");
      sessionStorage.setItem(
        Params.SUPPRESS_CONNECT_ERROR_TILL,
        new Date(new Date().getTime() + 10 * 1000).toISOString()
      );
    }
  } else if (status >= 500) {
    bannerNotification.error(
      "An unexpected error occurred at our end. We are working on it"
    );
  } else if (status === 401 || status === 403) {
    authUtils.logoutUnauthenticated(url);
  } else if (status === 400) {
    const errorMessage: string = error.response.data.message;
    if (
      errorMessage.indexOf("Exception") === -1 &&
      errorMessage.indexOf("parameter '") === -1
    ) {
      // No internal exception, just bad request by user
      bannerNotification.error(errorMessage);
    } else {
      // Incorrect call by UI to the ZS API.
      console.error(errorMessage);
      bannerNotification.error("Couldn't process your request");
    }
  } else if (status === 413) {
    bannerNotification.error("The uploaded file is too large");
  }
  return Promise.reject(error);
});

export default {
  get: (url: string, params?: any) => {
    return axios.get(url + httpUtils.param(params));
  },
  post: axios.post,
  put: axios.put,
  delete: axios.delete,
  all: axios.all,
  spread: axios.spread,
};
