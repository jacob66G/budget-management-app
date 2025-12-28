import { TestBed } from '@angular/core/testing';

import { TransactionAttachment } from './transaction-attachment.service';

describe('TransactionAttachment', () => {
  let service: TransactionAttachment;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(TransactionAttachment);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
