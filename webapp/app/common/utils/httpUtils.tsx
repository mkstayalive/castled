import _ from "lodash";
import { StringAnyMap } from "./types";

const getQueryParams = (url?: string) => {
	let search: string | undefined = "";
	if (url) {
		const queryIndex = url.indexOf("?");
		if (queryIndex !== -1) {
			search = url.substring(queryIndex + 1);
		}
	} else {
		search = window.location.search.substring(1);
	}
	return new URLSearchParams(search);
};

const param = (params?: {
	[key: string]: string | number | boolean | string[] | number[];
}) => {
	if (!params) return "";
	const paramStr = Object.keys(params)
		.filter((k) => params[k])
		.map(function (k) {
			const val = params[k];
			const key = encodeURIComponent(k);
			if (Array.isArray(val)) {
				return [...val].map((v) => key + "=" + encodeURIComponent(v)).join("&");
			} else {
				return key + "=" + encodeURIComponent(val);
			}
		})
		.join("&");
	return paramStr.length ? "?" + paramStr : "";
};

export default {
	is2xx: (status: number) => {
		return status >= 200 && status < 300;
	},
	getUrl: (baseUrl: string, params?: StringAnyMap) => {
		if (!params) return baseUrl;
		return baseUrl + "?" + param(params);
	},
	dtoToMultipartFormData: (data: any) => {
		const formData = new FormData();
		_.map(data, (value, key) => {
			formData.append(key, value);
		});
		return formData;
	},
	getQueryParams,
	getQueryParam: (key: string, url?: string) => {
		return getQueryParams(url).get(key);
	},
	param,
};
