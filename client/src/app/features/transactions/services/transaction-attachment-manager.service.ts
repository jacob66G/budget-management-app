import { inject, Injectable } from '@angular/core';
import { catchError, concatMap, lastValueFrom, map, Observable, of, tap } from 'rxjs';
import { AttachmentViewResponse, TransactionAttachmentService } from '../../../core/services/transaction-attachment.service';
import { TransactionAttachmentStore } from './transaction-attachment-store.service';

@Injectable({
  providedIn: 'root'
})
export class TransactionAttachmentManager {

  attachmentService = inject(TransactionAttachmentService);
  attachmentStoreService = inject(TransactionAttachmentStore)

  manageAttachmentUpload(file: File, transactionId: number): Observable<any> {

    return this.attachmentService.getPresignedPutUrl({fileName: file.name, fileType: file.type, fileSize: file.size}, transactionId).pipe(

      concatMap((res) => {
        console.log("Put url generated:", res.url);

        return this.attachmentService.uploadAttachment(res.url, file).pipe(
          map( () => res.key)
        );
      }),

      concatMap( (key) => {
        return this.attachmentService.confirmAttachmentUpload({originalFileName: file.name, key: key}, transactionId);
      }),

      tap((result) => {
        console.log("Received data: ", result);
        this.saveAttachmentData(transactionId, result);
      }),
    );
  }

  async getTransactionAttachmentData(transactionId: number): Promise<{originalFileName: string, downloadUrl: string} | null> {

    const storedDetails = this.attachmentStoreService.getAttachmentDetailsByTransactionId(transactionId);

    if (storedDetails) {
      if (this.isUrlExpired(storedDetails.expiresAt)) {
        console.log("Link expires. Fetchig new one.");
        const newData = await this.getPresignedGetUrl(transactionId);
        if (newData) {
          const data = {originalFileName: newData.originalFileName, downloadUrl:newData.downloadUrl}
          this.attachmentStoreService.updateAttachmentDetailsByTransactionId(transactionId, data);
        }
        return newData;
      }
      console.log("Using cached attachment data.");
      return {
        originalFileName: storedDetails.originalFileName,
        downloadUrl: storedDetails.downloadUrl
      };
    }

    console.log("No attachment data found");
    console.log("Fetching new download url");
    const data = await this.getPresignedGetUrl(transactionId);
    if (data) {
      this.attachmentStoreService.addTransactionAttachment(transactionId, data);
    }
    return data;
  }

  private async getPresignedGetUrl(transactionId: number): Promise<AttachmentViewResponse | null> {
    try {
      return await lastValueFrom(this.attachmentService.getPresignedGetUrl(transactionId));
    } catch (error) {
      console.error("Error occurrec when fetching presigned url", error);
    }
    return null;
  }

  private isUrlExpired(expiresAt: Date): boolean {
    if (!expiresAt) {
      return true;
    }

    const expirationDate = new Date(expiresAt);
    const now = new Date();

    const safetyBuffer = 2 * 60 * 1000;

    return now.getTime() > (expirationDate.getTime() - safetyBuffer);
  }

  private saveAttachmentData(transactionId: number, attachmentData: AttachmentViewResponse) {
    const att = this.attachmentStoreService.getAttachmentDetailsByTransactionId(transactionId);
      if (!att) {
        this.attachmentStoreService.addTransactionAttachment(transactionId, attachmentData);
      } else {
        this.attachmentStoreService.updateAttachmentDetailsByTransactionId(transactionId, attachmentData);
      }
  }
}
