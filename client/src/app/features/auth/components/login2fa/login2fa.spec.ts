import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Login2fa } from './login2fa';

describe('Login2fa', () => {
  let component: Login2fa;
  let fixture: ComponentFixture<Login2fa>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Login2fa]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Login2fa);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
