import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpcomingTransactions } from './upcoming-transactions.component';

describe('UpcomingTransactions', () => {
  let component: UpcomingTransactions;
  let fixture: ComponentFixture<UpcomingTransactions>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpcomingTransactions]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpcomingTransactions);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
