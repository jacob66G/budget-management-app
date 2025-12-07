import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AddRecurringTemplateDialog } from './add-recurring-template-dialog.component';

describe('AddRecurringTemplateDialog', () => {
  let component: AddRecurringTemplateDialog;
  let fixture: ComponentFixture<AddRecurringTemplateDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AddRecurringTemplateDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AddRecurringTemplateDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
