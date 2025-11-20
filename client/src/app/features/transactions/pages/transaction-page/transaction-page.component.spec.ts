import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TransactionPageComponent } from './transaction-page.component';

describe('TransactionPageComponent', () => {
  let component: TransactionPageComponent;
  let fixture: ComponentFixture<TransactionPageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TransactionPageComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TransactionPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
