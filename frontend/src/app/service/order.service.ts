import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Page } from '../dto/page.dto';
import { OrderDTO } from '../dto/order.dto';

@Injectable({
  providedIn: 'root'
})
export class OrderService {

  private apiUrl = 'https://localhost:443/api/v1/orders';

  constructor(private http: HttpClient) { }


  confirmOrder(): Observable<OrderDTO> {
    return this.http.post<OrderDTO>(
      `${this.apiUrl}/confirm`,
      null,
      { withCredentials: true }
    );
  }

  getAllOrders(page: number = 0, size: number = 10): Observable<Page<OrderDTO>> {
    return this.http.get<Page<OrderDTO>>(
      `${this.apiUrl}?page=${page}&size=${size}`,
      { withCredentials: true }
    );
  }

  getOrderById(id: number): Observable<OrderDTO> {
    return this.http.get<OrderDTO>(
      `${this.apiUrl}/${id}`,
      { withCredentials: true }
    );
  }

  rejectOrder(id: number): Observable<OrderDTO> {
    return this.http.put<OrderDTO>(
      `${this.apiUrl}/${id}/reject`,
      null,
      { withCredentials: true }
    );
  }
}