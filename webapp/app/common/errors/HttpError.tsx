import { HttpErrorCode } from "../enums/HttpErrorCode";

export class HttpError extends Error {
	errorCode: HttpErrorCode;

	protected constructor(errorCode: HttpErrorCode, message: any) {
		super(message);
		this.name = this.constructor.name;
		Error.captureStackTrace(this, this.constructor);
		this.errorCode = errorCode;
	}
}
