import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CheeseDTO } from '../dto/cheese.dto';

@Injectable({
  providedIn: 'root'
})
export class CheeseService {
  addCheeseToOrder(userId: number, cheeseId: number, boxes: number) {
    throw new Error('Method not implemented.');
  }

  private apiUrl = 'https://localhost:443/api/v1/cheeses';

  constructor(private http: HttpClient) { }

  getAllCheeses(): Observable<CheeseDTO[]> {
    return this.http.get<CheeseDTO[]>(this.apiUrl);
  }

  getCheeseById(id: number): Observable<CheeseDTO> {
    return this.http.get<CheeseDTO>(`${this.apiUrl}/${id}`);
  }

  getCheeseImage(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/image`, { responseType: 'blob', withCredentials: true });
  }

  createCheese(cheeseData: CheeseDTO) {
    return this.http.post<CheeseDTO>(`${this.apiUrl}/new`, cheeseData, { withCredentials: true }
    );
  }

  uploadCheeseImage(id: number, file: File | null): Observable<any> {
    // Don't make HTTP request if there's no file
    if (!file) {
      return new Observable(observer => {
        observer.complete();
      });
    }

    const formData = new FormData();
    formData.append("file", file);

    return this.http.post(`${this.apiUrl}/${id}/image`, formData, { withCredentials: true });
  }

  updateCheese(id: number, cheeseData: CheeseDTO): Observable<CheeseDTO> {
    return this.http.put<CheeseDTO>(`${this.apiUrl}/${id}`, cheeseData, { withCredentials: true });
  }

  updateCheeseImage(id: number, file: File): Observable<any> {
    const formData = new FormData();
    formData.append("file", file);
    return this.http.put(`${this.apiUrl}/${id}/image`, formData, { withCredentials: true });
  }

  deleteCheese(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }

  addBox(id: number, boxWeight: number): Observable<CheeseDTO> {
    return this.http.put<CheeseDTO>(`${this.apiUrl}/${id}/boxes/add`, { weight: boxWeight }, { withCredentials: true });
  }

  removeBox(id: number, boxIndex: number): Observable<CheeseDTO> {
    return this.http.put<CheeseDTO>(`${this.apiUrl}/${id}/boxes/remove/${boxIndex}`, {}, { withCredentials: true });
  }

}