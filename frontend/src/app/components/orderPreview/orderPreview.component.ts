import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { OrderDTO } from '../../dto/order.dto';
import { OrderService } from '../../service/order.service';
import { InvoiceService } from '../../service/invoice.service';
import { DialogService } from '../../service/dialog.service';
import { UserService } from '../../service/user.service';

@Component({
  selector: 'app-order-preview',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orderPreview.component.html',
  styleUrls: ['./orderPreview.component.css']
})
export class OrderPreviewComponent implements OnInit {

  order: OrderDTO | null = null;
  loading = true;
  creatingInvoice = false;
  rejectingOrder = false;
  canManageOrder = false;
  currentUserId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private orderService: OrderService,
    private invoiceService: InvoiceService,
    private dialogService: DialogService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id || Number.isNaN(id)) {
      this.router.navigate(['/orders']);
      return;
    }

    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.canManageOrder = user.rols?.includes('ADMIN') ?? false;
        this.currentUserId = user.id;
        this.loadOrder(id);
      },
      error: () => {
        this.router.navigate(['/auth/login']);
      }
    });

  }

  private loadOrder(id: number): void {
    if (!this.canManageOrder && !this.currentUserId) {
      this.router.navigate(['/auth/login']);
      return;
    }

    const request = this.canManageOrder
      ? this.orderService.getOrderById(id)
      : this.userService.getMyOrderById(this.currentUserId!, id);

    request.subscribe({
      next: (data) => {
        this.order = data;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;

        if (err.status === 404) {
          this.router.navigate(['/orders']);
          return;
        }

        if (err.status >= 500) {
          this.router.navigate(['/error']);
          return;
        }

        this.router.navigate(['/orders']);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/orders']);
  }

  onConfirm(): void {
    if (!this.order || this.creatingInvoice || !this.canManageOrder) {
      return;
    }

    this.dialogService.confirm('¿Confirmar este pedido?', () => this.createInvoiceFromOrder());
  }

  onCancel(): void {
    if (!this.order || this.rejectingOrder || this.creatingInvoice || !this.canManageOrder) {
      return;
    }

    this.dialogService.confirm('¿Rechazar este pedido?', () => this.rejectOrder());
  }

  private createInvoiceFromOrder(): void {
    if (!this.order) {
      return;
    }

    this.creatingInvoice = true;

    this.invoiceService.createInvoiceFromOrder(this.order).subscribe({
      next: (invoice) => {
        this.creatingInvoice = false;
        this.dialogService.alert(`Factura creada correctamente: ${invoice.invNo}`);
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.creatingInvoice = false;

        if (err.status === 409) {
          this.dialogService.alert('Este pedido ya estaba procesado y ya tiene factura.');
          this.router.navigate(['/orders']);
          return;
        }

        if (err.status >= 500) {
          this.router.navigate(['/error']);
          return;
        }

        this.dialogService.alert('No se ha podido crear la factura para este pedido.');
      }
    });
  }

  private rejectOrder(): void {
    if (!this.order) {
      return;
    }

    this.rejectingOrder = true;

    this.orderService.rejectOrder(this.order.id).subscribe({
      next: () => {
        this.rejectingOrder = false;
        this.dialogService.alert('Pedido rechazado.');
        this.router.navigate(['/orders']);
      },
      error: (err) => {
        this.rejectingOrder = false;

        if (err.status === 409) {
          this.dialogService.alert('Este pedido ya estaba procesado.');
          this.router.navigate(['/orders']);
          return;
        }

        if (err.status === 404) {
          this.dialogService.alert('Pedido no encontrado.');
          this.router.navigate(['/orders']);
          return;
        }

        if (err.status >= 500) {
          this.router.navigate(['/error']);
          return;
        }

        this.dialogService.alert('No se ha podido rechazar el pedido.');
      }
    });
  }
}
