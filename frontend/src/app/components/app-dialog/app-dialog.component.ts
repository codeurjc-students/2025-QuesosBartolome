import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { DialogService } from '../../service/dialog.service';

@Component({
  selector: 'app-dialog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app-dialog.component.html',
  styleUrls: ['./app-dialog.component.css']
})
export class AppDialogComponent {
  constructor(private dialogService: DialogService) {}

  get dialogState$() {
    return this.dialogService.dialogState$;
  }

  accept(): void {
    this.dialogService.accept();
  }

  cancel(): void {
    this.dialogService.cancel();
  }
}