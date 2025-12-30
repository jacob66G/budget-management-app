import { Component, Inject, inject, input, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef, MatDialogContent, MatDialogActions } from '@angular/material/dialog';
import { MatIcon } from "@angular/material/icon";
import { MatProgressSpinner } from "@angular/material/progress-spinner";

@Component({
  selector: 'app-attachment-view-dialog',
  imports: [MatIcon, MatDialogContent, MatProgressSpinner, MatDialogActions],
  standalone: true,
  templateUrl: './attachment-view-dialog.component.html',
  styleUrl: './attachment-view-dialog.component.scss'
})

export class AttachmentViewDialog {

  private dialogRef = inject(MatDialogRef<AttachmentViewDialog>);

  constructor(@Inject(MAT_DIALOG_DATA) public data: {originalFileName:string, downloadUrl:string}){}

  isLoading = signal<boolean>(true);
  hasError = signal<boolean>(false);

  close(): void {
    this.dialogRef.close();
  }

  onImageLoad(): void {
    this.isLoading.set(false);
  }

  handleError(): void {
    this.isLoading.set(false);
    this.hasError.set(true);
  }

}