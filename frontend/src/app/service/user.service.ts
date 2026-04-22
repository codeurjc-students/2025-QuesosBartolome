import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDTO } from '../dto/user.dto';
import { Page } from '../dto/page.dto';
import { OrderDTO } from '../dto/order.dto';
import { InvoiceDTO } from '../dto/invoice.dto';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'https://localhost:443/api/v1/users';

  constructor(private http: HttpClient) {}

  getCurrentUser(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}` , { withCredentials: true });
  }

  getUserById(id: number): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  getUserImage(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/image`, { responseType: 'blob' , withCredentials: true });
  }

  getAllUsers(page: number, size: number): Observable<Page<UserDTO>> {
    return this.http.get<Page<UserDTO>>(`${this.apiUrl}/all?page=${page}&size=${size}`, { withCredentials: true });
  }

  getMyOrders(userId: number, page: number = 0, size: number = 10): Observable<Page<OrderDTO>> {
    return this.http.get<Page<OrderDTO>>(`${this.apiUrl}/${userId}/orders?page=${page}&size=${size}`, { withCredentials: true });
  }

  getMyOrderById(userId: number, id: number): Observable<OrderDTO> {
    return this.http.get<OrderDTO>(`${this.apiUrl}/${userId}/orders/${id}`, { withCredentials: true });
  }

  getMyInvoices(userId: number, page: number = 0, size: number = 10): Observable<Page<InvoiceDTO>> {
    return this.http.get<Page<InvoiceDTO>>(`${this.apiUrl}/${userId}/invoices?page=${page}&size=${size}`, { withCredentials: true });
  }

  getMyInvoiceById(userId: number, id: number): Observable<InvoiceDTO> {
    return this.http.get<InvoiceDTO>(`${this.apiUrl}/${userId}/invoices/${id}`, { withCredentials: true });
  }

  downloadMyInvoicePdf(userId: number, invoiceId: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${userId}/invoices/${invoiceId}/download-pdf`, {
      responseType: 'blob',
      withCredentials: true
    });
  }

  updateUser(id: number, user: Partial<UserDTO>): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.apiUrl}/${id}`, user, { withCredentials: true });
  }

  updateUserImage(id: number, file: File): Observable<void> {
    const formData = new FormData();
    formData.append('file', file);
    return this.http.put<void>(`${this.apiUrl}/${id}/image`, formData, { withCredentials: true });
  }

  changePassword(id: number, payload: { currentPassword: string; newPassword: string; confirmPassword: string }): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/password`, payload, { withCredentials: true });
  }

  toggleUserBan(id: number): Observable<UserDTO> {
    return this.http.put<UserDTO>(`${this.apiUrl}/${id}/ban`, {}, { withCredentials: true });
  }
}