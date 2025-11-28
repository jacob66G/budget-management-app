import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountDetailsPage } from './account-details';

describe('AccountDetails', () => {
  let component: AccountDetailsPage;
  let fixture: ComponentFixture<AccountDetailsPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountDetailsPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountDetailsPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
