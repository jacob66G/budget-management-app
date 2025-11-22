import { TestBed } from '@angular/core/testing';
import { CanActivateFn } from '@angular/router';

import { PublicGuard } from './public-guard';

describe('PublicGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) => 
      TestBed.runInInjectionContext(() => PublicGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({});
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });
});
