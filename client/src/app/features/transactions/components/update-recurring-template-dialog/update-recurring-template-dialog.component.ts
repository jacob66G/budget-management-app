import { Component, Inject, inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from "@angular/material/dialog";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatDividerModule } from "@angular/material/divider"; // Zmieniono na Module
import { MatIconModule } from "@angular/material/icon";
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { RecurringTemplate } from '../../model/recurring-template.model';
import { MatInputModule } from '@angular/material/input';

@Component({
  selector: 'app-update-recurring-template-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatFormFieldModule, MatDividerModule, MatIconModule, MatInputModule, MatButtonModule],
  templateUrl: './update-recurring-template-dialog.component.html',
  styleUrl: './update-recurring-template-dialog.component.scss'
})
export class UpdateRecurringTemplateDialogComponent {

  private fb = inject(FormBuilder);
  private dialogRef = inject(MatDialogRef<UpdateRecurringTemplateDialogComponent>);

  updateTemplateForm: FormGroup;

  constructor(@Inject(MAT_DIALOG_DATA) public data: RecurringTemplate) {
    this.updateTemplateForm = this.fb.group({
      title: [data.title, Validators.required],
      amount: [data.amount, [Validators.required, Validators.min(0)]],
      description: [data.description]
    });
  }

  onReset(): void {
    this.updateTemplateForm.reset({
      title: this.data.title,
      amount: this.data.amount,
      description: this.data.description
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  onSubmit(): void {
    if (this.updateTemplateForm.valid) {
      const result = {
        id: this.data.id,
        ...this.updateTemplateForm.value
      };
      this.dialogRef.close(result);
    }
  }

}
