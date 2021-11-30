export interface Page<T> {
	content: T[];
	page: number;
	size: number;
	totalSize: number;
	sort?: { [key: string]: number };
}
