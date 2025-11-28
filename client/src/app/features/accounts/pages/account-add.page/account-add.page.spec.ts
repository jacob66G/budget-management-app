import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountAddPage } from './account-add.page';

describe('AccountAddPage', () => {
  let component: AccountAddPage;
  let fixture: ComponentFixture<AccountAddPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountAddPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountAddPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
