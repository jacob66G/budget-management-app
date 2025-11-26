import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateTransactionDialogComponent } from './update-transaction-dialog.component';

describe('UpdateTransactionDialogComponent', () => {
  let component: UpdateTransactionDialogComponent;
  let fixture: ComponentFixture<UpdateTransactionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateTransactionDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateTransactionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
