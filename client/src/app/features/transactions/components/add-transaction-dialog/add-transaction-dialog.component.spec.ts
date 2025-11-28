import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddTransactionDialogComponent } from './add-transaction-dialog.component';

describe('AddTransactionDialogComponent', () => {
  let component: AddTransactionDialogComponent;
  let fixture: ComponentFixture<AddTransactionDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddTransactionDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddTransactionDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
