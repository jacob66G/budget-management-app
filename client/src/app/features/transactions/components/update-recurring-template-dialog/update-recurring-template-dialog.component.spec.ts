import { ComponentFixture, TestBed } from '@angular/core/testing';

import { UpdateRecurringTemplateDialog } from './update-recurring-template-dialog.component';

describe('UpdateRecurringTemplateDialog', () => {
  let component: UpdateRecurringTemplateDialog;
  let fixture: ComponentFixture<UpdateRecurringTemplateDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [UpdateRecurringTemplateDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(UpdateRecurringTemplateDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
