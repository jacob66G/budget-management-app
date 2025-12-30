import { Injectable, signal } from '@angular/core';
import { TransactionAttachmentDetails } from '../model/transaction-attachment-details.model';

export interface TransactionAttachment {
  transactionId: number,
  attachmentDetails: TransactionAttachmentDetails
}

@Injectable({
  providedIn: 'root'
})
export class TransactionAttachmentStore {
  
  private attachments = signal<TransactionAttachment[]>([]);

  addTransactionAttachment(transactionId: number, attachmentDetails: TransactionAttachmentDetails): void {
    const attachment = {
      transactionId: transactionId,
      attachmentDetails: attachmentDetails
    } as TransactionAttachment
    this.attachments.update(currentItems => [...currentItems, attachment]);
  }

  getAttachmentDetailsByTransactionId(transactionId: number): TransactionAttachmentDetails | null {
    const details = this.attachments().find( (att) => att.transactionId === transactionId);
    if (!details) {
      return null;
    }
    const {attachmentDetails} = details;
    return attachmentDetails;
  }

  updateAttachmentDetailsByTransactionId(transactionId: number, data: {originalFileName: string, downloadUrl: string}): void {
    this.attachments.update(items => 
      items.map(att => {
        if (att.transactionId === transactionId) {
          return {
            ...att,
            attachmentDetails: {
              ...att.attachmentDetails,
              originalFileName: data.originalFileName,
              downloadUrl: data.downloadUrl
            }
          };
        }
        return att;
      })
    );
  }
}
