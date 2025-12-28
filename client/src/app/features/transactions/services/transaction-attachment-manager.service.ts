import { inject, Injectable } from '@angular/core';
import { concatMap, map, Observable, of, tap } from 'rxjs';
import { TransactionAttachmentService } from './transaction-attachment.service';
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
        this.attachmentStoreService.addTransactionAttachment(transactionId, result);
      }),

    );
  }
  
}
