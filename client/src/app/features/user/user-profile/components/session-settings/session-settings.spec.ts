import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SessionSettings } from './session-settings';

describe('SessionSettings', () => {
  let component: SessionSettings;
  let fixture: ComponentFixture<SessionSettings>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SessionSettings]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SessionSettings);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
