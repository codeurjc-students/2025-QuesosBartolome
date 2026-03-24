import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { InvoiceDTO } from '../dto/invoice.dto';
import { OrderDTO } from '../dto/order.dto';
import { Page } from '../dto/page.dto';

@Injectable({
  providedIn: 'root'
})
export class InvoiceService {

  private apiUrl = 'https://localhost:443/api/v1/invoices';

  constructor(private http: HttpClient) { }

  createInvoiceFromOrder(order: OrderDTO): Observable<InvoiceDTO> {
    return this.http.post<InvoiceDTO>(
      this.apiUrl,
      order,
      { withCredentials: true }
    );
  }

  getInvoiceById(id: number): Observable<InvoiceDTO> {
    return this.http.get<InvoiceDTO>(
      `${this.apiUrl}/${id}`,
      { withCredentials: true }
    );
  }

  getAllInvoices(page: number = 0, size: number = 10): Observable<Page<InvoiceDTO>> {
    return this.http.get<Page<InvoiceDTO>>(
      `${this.apiUrl}?page=${page}&size=${size}`,
      { withCredentials: true }
    );
  }

  downloadInvoicePdf(invoiceId: number): Observable<Blob> {
    return this.http.get(
      `${this.apiUrl}/${invoiceId}/download-pdf`,
      {
        responseType: 'blob',
        withCredentials: true
      }
    );
  }
}
