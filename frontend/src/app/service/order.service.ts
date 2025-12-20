import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CheeseDTO } from '../dto/cheese.dto';
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
}