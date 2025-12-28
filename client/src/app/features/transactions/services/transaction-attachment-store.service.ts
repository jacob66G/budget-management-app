import { Injectable, signal } from '@angular/core';
import { AttachmentViewResponse } from './transaction-attachment.service';
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

}
