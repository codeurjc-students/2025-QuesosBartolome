import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { UserDTO } from '../dto/user.dto';
import { Page } from '../dto/page.dto';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiUrl = 'https://localhost:443/api/v1/users';

  constructor(private http: HttpClient) {}

  getCurrentUser(): Observable<UserDTO> {
    return this.http.get<UserDTO>(`${this.apiUrl}` , { withCredentials: true });
  }
  getUserImage(id: number): Observable<Blob> {
    return this.http.get(`${this.apiUrl}/${id}/image`, { responseType: 'blob' , withCredentials: true });
  }

  getAllUsers(page: number, size: number): Observable<Page<UserDTO>> {
    return this.http.get<Page<UserDTO>>(`${this.apiUrl}/all?page=${page}&size=${size}`, { withCredentials: true });
  }
}