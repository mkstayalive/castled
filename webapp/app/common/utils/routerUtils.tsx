export default {
	getArray: (id: string[] | string | undefined): any => {
		return Array.isArray(id) ? id : id ? [id] : [];
	},
	getString: (id: string[] | string | undefined): any => {
		return Array.isArray(id) ? id[0] : id || "";
	},
	getInt: (id: string[] | string | undefined): any => {
		return Array.isArray(id) ? parseInt(id[0]) : id ? parseInt(id) : null;
	},
};
