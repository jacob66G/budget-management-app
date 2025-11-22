import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ManageTfaDialog } from './manage-tfa-dialog';

describe('ManageTfaDialog', () => {
  let component: ManageTfaDialog;
  let fixture: ComponentFixture<ManageTfaDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ManageTfaDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ManageTfaDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
