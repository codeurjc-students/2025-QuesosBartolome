import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CartDTO } from '../dto/cart.dto';

@Injectable({
    providedIn: 'root'
})
export class CartService {

    private apiUrl = 'https://localhost:443/api/v1/cart';

    constructor(private http: HttpClient) { }

    getMyCart(): Observable<CartDTO> {
        return this.http.get<CartDTO>(`${this.apiUrl}`, { withCredentials: true });
    }
    
    addCheeseToOrder(userId: number, cheeseId: number, boxes: number): Observable<CartDTO> {
        return this.http.put<CartDTO>(`${this.apiUrl}/addItem`,
            null,
            { params: { cheeseId, boxes }, withCredentials: true });
    }

    removeItemFromCart(itemId: number): Observable<CartDTO> {
        return this.http.put<CartDTO>(
            `${this.apiUrl}/removeItem`,
            null,
            { params: { itemId }, withCredentials: true }
        );
    }

}