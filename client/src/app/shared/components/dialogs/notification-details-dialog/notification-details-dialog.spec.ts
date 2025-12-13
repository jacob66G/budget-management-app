import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NotificationDetailsDialog } from './notification-details-dialog';

describe('NotificationDetailsDialog', () => {
  let component: NotificationDetailsDialog;
  let fixture: ComponentFixture<NotificationDetailsDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [NotificationDetailsDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NotificationDetailsDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
