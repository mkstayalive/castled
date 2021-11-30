export interface PageRequest {
	page: number;
	size: number;
	sort?: { [key: string]: number };
}
