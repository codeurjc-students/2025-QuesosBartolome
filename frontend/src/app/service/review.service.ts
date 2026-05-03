import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReviewDTO } from '../dto/review.dto';
import { Page } from '../dto/page.dto';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ReviewService {

  private apiUrl = `${environment.apiBaseUrl}/api/v1/reviews`;

  constructor(private http: HttpClient) { }

  getReviewsByCheeseId(cheeseId: number, page: number = 0, size: number = 3): Observable<Page<ReviewDTO>> {
    return this.http.get<Page<ReviewDTO>>(`${this.apiUrl}/cheese/${cheeseId}?page=${page}&size=${size}`);
  }

  getReviewsByUserId(userId: number, page: number = 0, size: number = 10): Observable<Page<ReviewDTO>> {
    return this.http.get<Page<ReviewDTO>>(`${this.apiUrl}/user/${userId}?page=${page}&size=${size}`);
  }

  getReviewById(id: number): Observable<ReviewDTO> {
    return this.http.get<ReviewDTO>(`${this.apiUrl}/${id}`);
  }

  createReview(rating: number, comment: string, cheeseId: number): Observable<ReviewDTO> {
    const reviewData = { rating, comment, cheeseId };
    return this.http.post<ReviewDTO>(this.apiUrl, reviewData, { withCredentials: true });
  }

  deleteReview(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { withCredentials: true });
  }
}
