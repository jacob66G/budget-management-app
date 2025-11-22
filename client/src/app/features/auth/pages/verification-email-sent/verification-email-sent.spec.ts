import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerificationEmailSent } from './verification-email-sent';

describe('VerifyEmail', () => {
  let component: VerificationEmailSent;
  let fixture: ComponentFixture<VerificationEmailSent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerificationEmailSent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerificationEmailSent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
