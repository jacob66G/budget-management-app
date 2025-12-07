import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecurringTemplatesPage } from './recurring-templates-page.component';

describe('RecurringTemplatesPage', () => {
  let component: RecurringTemplatesPage;
  let fixture: ComponentFixture<RecurringTemplatesPage>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecurringTemplatesPage]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecurringTemplatesPage);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
