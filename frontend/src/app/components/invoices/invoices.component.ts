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
    alert(`Descarga de factura ${invoice.invNo} no implementada todavía.`);
  }
}
