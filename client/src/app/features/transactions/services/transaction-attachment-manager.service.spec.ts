import { TestBed } from '@angular/core/testing';

import { TransactionAttachmentManager } from './transaction-attachment-manager.service';

describe('TransactionAttachmentManager', () => {
  let service: TransactionAttachmentManager;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TransactionAttachmentManager);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
