import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryFormDialogComponent } from './category-form-dialog.component';

describe('CategoryFormDialogComponent', () => {
  let component: CategoryFormDialogComponent;
  let fixture: ComponentFixture<CategoryFormDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryFormDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoryFormDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
