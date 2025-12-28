import { TestBed } from '@angular/core/testing';

import { TransactionAttachmentState } from './transaction-attachment-store.service';

describe('TransactionAttachmentState', () => {
  let service: TransactionAttachmentState;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TransactionAttachmentState);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
