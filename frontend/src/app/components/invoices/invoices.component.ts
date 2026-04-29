import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { InvoiceService } from '../../service/invoice.service';
import { InvoiceDTO } from '../../dto/invoice.dto';
import { DialogService } from '../../service/dialog.service';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';

@Component({
  selector: 'app-invoices',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './invoices.component.html',
  styleUrls: ['./invoices.component.css']
})
export class InvoicesComponent implements OnInit {

  invoices: InvoiceDTO[] = [];
  loading = true;
  currentUser: UserDTO | null = null;

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  constructor(
    private invoiceService: InvoiceService,
    private userService: UserService,
    private router: Router,
    private dialogService: DialogService
  ) {}

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.loadLastInvoices();
      },
      error: () => {
        this.currentUser = null;
        this.loadLastInvoices();
      }
    });
  }

  loadInvoices(): void {
    this.loadInvoicesPage(this.currentPage);
  }

  private loadLastInvoices(): void {
    this.loading = true;

    if (!this.isAdmin() && !this.currentUser?.id) {
      this.router.navigate(['/auth/login']);
      return;
    }

    const request = this.isAdmin()
      ? this.invoiceService.getAllInvoices(0, this.pageSize)
      : this.userService.getMyInvoices(this.currentUser!.id, 0, this.pageSize);

    request.subscribe({
      next: (data) => {
        this.totalPages = data.totalPages;
        this.currentPage = Math.max(data.totalPages - 1, 0);

        if (this.currentPage === 0) {
          this.invoices = data.content;
          this.loading = false;
          return;
        }

        this.loadInvoicesPage(this.currentPage);
      },
      error: (err) => {
        this.loading = false;
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  private loadInvoicesPage(page: number): void {
    this.loading = true;

    if (!this.isAdmin() && !this.currentUser?.id) {
      this.router.navigate(['/auth/login']);
      return;
    }

    const request = this.isAdmin()
      ? this.invoiceService.getAllInvoices(page, this.pageSize)
      : this.userService.getMyInvoices(this.currentUser!.id, page, this.pageSize);

    request.subscribe({
      next: (data) => {
        this.invoices = data.content;
        this.totalPages = data.totalPages;
        this.currentPage = data.number;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.loadInvoicesPage(this.currentPage + 1);
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.loadInvoicesPage(this.currentPage - 1);
    }
  }

  downloadInvoice(invoice: InvoiceDTO): void {
    if (!this.isAdmin() && !this.currentUser?.id) {
      this.router.navigate(['/auth/login']);
      return;
    }

    const request = this.isAdmin()
      ? this.invoiceService.downloadInvoicePdf(invoice.id)
      : this.userService.downloadMyInvoicePdf(this.currentUser!.id, invoice.id);

    request.subscribe({
      next: (pdfBlob: Blob) => {
        // Create a URL for the blob
        const blobUrl = window.URL.createObjectURL(pdfBlob);
        
        // Create a temporary link and trigger download
        const link = document.createElement('a');
        link.href = blobUrl;
        link.download = `${invoice.invNo}.pdf`;
        document.body.appendChild(link);
        link.click();
        
        // Clean up
        document.body.removeChild(link);
        window.URL.revokeObjectURL(blobUrl);
      },
      error: (err) => {
        if (err.status === 404) {
          this.dialogService.alert('Factura no encontrada.');
        } else {
          this.dialogService.alert('Error al descargar la factura. Intenta de nuevo.');
        }
      }
    });
  }

  isAdmin(): boolean {
    return this.currentUser?.rols?.includes('ADMIN') ?? false;
  }
}
