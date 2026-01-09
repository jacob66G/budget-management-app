import { HttpClient, HttpContext } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ApiPaths } from '../../constans/api-paths';
import { IS_S3_REQUEST } from '../tokens/tokens';

export interface ReceiptUploadUrlRequest {
  fileName: string,
  fileType: string,
  fileSize: number
}

export interface ReceiptUploadUrlResponse {
  key: string,
  url: string
}

export interface ConfirmAttachmentUploadRequest {
  originalFileName: string,
  key: string
}

export interface AttachmentViewResponse {
  originalFileName: string,
  downloadUrl: string,
  expiresAt: Date
}

@Injectable({
  providedIn: 'root'
})
export class TransactionAttachmentService {

  private http = inject(HttpClient);

  getPresignedPutUrl(reqBody: ReceiptUploadUrlRequest, transactionId: number): Observable<ReceiptUploadUrlResponse> {
    const url = ApiPaths.Transactions.Attachments.INIT_UPLOAD(transactionId);
    return this.http.post<ReceiptUploadUrlResponse>(url, reqBody);
  }
  
  uploadAttachment(url: string, file: File): Observable<void> {
    return this.http.put(url, file, {
      headers: {'Content-Type': file.type},
      context: new HttpContext().set(IS_S3_REQUEST, true),
      responseType: 'text'
    }).pipe(
      map(() => void 0)
    );
  }

  confirmAttachmentUpload(reqBody: ConfirmAttachmentUploadRequest, transactionId: number): Observable<AttachmentViewResponse> {
    const url = ApiPaths.Transactions.Attachments.CONFIRM_UPLOAD(transactionId);
    return this.http.post<AttachmentViewResponse>(url, reqBody);
  }

  getPresignedGetUrl(transactionId: number): Observable<AttachmentViewResponse> {
    const url = ApiPaths.Transactions.Attachments.GET_DOWNLOAD_URL(transactionId);
    return this.http.get<AttachmentViewResponse>(url);
  }
}
