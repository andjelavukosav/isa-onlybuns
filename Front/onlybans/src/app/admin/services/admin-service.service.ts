import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/app/env/enviroment';
import { PostCommentAnalytics } from '../model/post-comment-analytics.model';
import { UsersActivityPercentagesDTO } from '../model/user-activity-percentages.model';

@Injectable({
  providedIn: 'root'
})
export class AdminServiceService {
  private baseUrl = environment.apiHost + '/admin';

  constructor(private http: HttpClient) { }

  getPostsAndCommentsAnalytics(): Observable<PostCommentAnalytics> {
    return this.http.get<PostCommentAnalytics>(`${this.baseUrl}/analytics/posts-comments`);
  }

  getUserActivityPercentages(): Observable<UsersActivityPercentagesDTO> {
    return this.http.get<UsersActivityPercentagesDTO>(`${this.baseUrl}/analytics/user-activity`);
  }
}
