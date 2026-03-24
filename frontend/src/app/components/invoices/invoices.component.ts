import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { InvoiceService } from '../../service/invoice.service';
import { InvoiceDTO } from '../../dto/invoice.dto';

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

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  constructor(private invoiceService: InvoiceService, private router: Router) {}

  ngOnInit(): void {
    this.loadInvoices();
  }

  loadInvoices(): void {
    this.loading = true;
    this.invoiceService.getAllInvoices(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.invoices = data.content;
        this.totalPages = data.totalPages;
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
      this.currentPage++;
      this.loadInvoices();
    }
  }

  prevPage(): void {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadInvoices();
    }
  }

  downloadInvoice(invoice: InvoiceDTO): void {
    this.invoiceService.downloadInvoicePdf(invoice.id).subscribe({
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
          alert('Factura no encontrada.');
        } else {
          alert('Error al descargar la factura. Intenta de nuevo.');
        }
      }
    });
  }
}
