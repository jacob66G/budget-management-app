import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountsListPage } from './accounts-list';

describe('AccountsList', () => {
  let component: AccountsListPage;
  let fixture: ComponentFixture<AccountsListPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountsListPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountsListPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
