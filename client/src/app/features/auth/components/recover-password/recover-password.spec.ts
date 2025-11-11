import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecoverPassword } from './recover-password';

describe('RecoverPassword', () => {
  let component: RecoverPassword;
  let fixture: ComponentFixture<RecoverPassword>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecoverPassword]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecoverPassword);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
