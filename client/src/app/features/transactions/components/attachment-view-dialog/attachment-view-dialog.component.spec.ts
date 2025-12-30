import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AttachmentViewDialog } from './attachment-view-dialog.component';

describe('AttachmentViewDialog', () => {
  let component: AttachmentViewDialog;
  let fixture: ComponentFixture<AttachmentViewDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AttachmentViewDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AttachmentViewDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
