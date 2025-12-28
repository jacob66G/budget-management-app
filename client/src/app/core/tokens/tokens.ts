import { HttpContextToken } from "@angular/common/http";

export const IS_S3_REQUEST = new HttpContextToken<boolean>(() => false);