import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

export interface DialogState {
  type: 'alert' | 'confirm';
  message: string;
  confirmLabel: string;
  cancelLabel?: string;
  onConfirm?: () => void;
  onCancel?: () => void;
}

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  private readonly dialogStateSubject = new BehaviorSubject<DialogState | null>(null);
  private alertTimeoutId: ReturnType<typeof setTimeout> | null = null;

  readonly dialogState$ = this.dialogStateSubject.asObservable();

  alert(message: string): void {
    this.clearAlertTimeout();

    this.dialogStateSubject.next({
      type: 'alert',
      message,
      confirmLabel: 'OK'
    });

    const currentDialog = this.dialogStateSubject.value;
    this.alertTimeoutId = setTimeout(() => {
      if (this.dialogStateSubject.value === currentDialog) {
        this.dialogStateSubject.next(null);
      }
      this.clearAlertTimeout();
    }, 3500);
  }

  confirm(message: string, onConfirm: () => void, onCancel?: () => void): void {
    this.clearAlertTimeout();

    this.dialogStateSubject.next({
      type: 'confirm',
      message,
      confirmLabel: 'Confirmar',
      cancelLabel: 'Cancelar',
      onConfirm,
      onCancel
    });
  }

  accept(): void {
    this.clearAlertTimeout();

    const dialogState = this.dialogStateSubject.value;

    if (!dialogState) {
      return;
    }

    this.dialogStateSubject.next(null);

    if (dialogState.type === 'confirm' && dialogState.onConfirm) {
      dialogState.onConfirm();
    }
  }

  cancel(): void {
    this.clearAlertTimeout();

    const dialogState = this.dialogStateSubject.value;

    if (!dialogState) {
      return;
    }

    this.dialogStateSubject.next(null);

    if (dialogState.type === 'confirm' && dialogState.onCancel) {
      dialogState.onCancel();
    }
  }

  private clearAlertTimeout(): void {
    if (this.alertTimeoutId !== null) {
      clearTimeout(this.alertTimeoutId);
      this.alertTimeoutId = null;
    }
  }
}
