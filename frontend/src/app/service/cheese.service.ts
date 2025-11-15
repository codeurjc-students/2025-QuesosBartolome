import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CheeseDTO } from '../dto/cheese.dto';

@Injectable({
  providedIn: 'root'
})
export class CheeseService {

  private apiUrl = 'https://localhost:443/api/v1/cheeses';

  constructor(private http: HttpClient) {}

  getAllCheeses(): Observable<CheeseDTO[]> {
    return this.http.get<CheeseDTO[]>(this.apiUrl);
  }

  getCheeseById(id: number): Observable<CheeseDTO> {
    return this.http.get<CheeseDTO>(`${this.apiUrl}/${id}`);
  }
}