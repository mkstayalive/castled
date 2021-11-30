export default {
	safeJSON: (obj: any): any => {
		return JSON.parse(JSON.stringify(obj));
	},
};
