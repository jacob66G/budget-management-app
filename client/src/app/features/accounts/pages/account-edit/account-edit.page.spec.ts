import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountEditPage } from './account-edit.page';

describe('AccountEditPage', () => {
  let component: AccountEditPage;
  let fixture: ComponentFixture<AccountEditPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountEditPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountEditPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
