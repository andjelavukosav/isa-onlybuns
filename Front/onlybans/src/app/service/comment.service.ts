import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../env/enviroment';
import { PostComment } from '../model/comment';
import { AuthService } from './auth.service';
import { CommentDTO } from '../model/commentDto';

@Injectable({
  providedIn: 'root'
})
export class CommentService {
  private apiUrl = `${environment.apiHost}/comments`;

  constructor(private http: HttpClient, private authService: AuthService) {}

  getCommentsByPost(postId: number): Observable<CommentDTO[]> {
    return this.http.get<CommentDTO[]>(`${this.apiUrl}/${postId}`);
  }
  

  addComment(postId: number, comment: CommentDTO): Observable<CommentDTO> {
    const token = localStorage.getItem('token'); // Ili preko AuthService
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    });

    return this.http.post<CommentDTO>(
      `${this.apiUrl}/${postId}/comments`,
      comment,
      { headers }
    );
  }
  
  
}
