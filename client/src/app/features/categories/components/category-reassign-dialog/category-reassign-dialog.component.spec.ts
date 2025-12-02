import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CategoryReassignDialogComponent } from './category-reassign-dialog.component';

describe('CategoryReassignDialogComponent', () => {
  let component: CategoryReassignDialogComponent;
  let fixture: ComponentFixture<CategoryReassignDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CategoryReassignDialogComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CategoryReassignDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
