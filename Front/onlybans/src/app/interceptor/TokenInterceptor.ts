import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpInterceptor,
  HttpEvent,
  HttpErrorResponse
} from '@angular/common/http';
import { AuthService } from '../service';
import { take, switchMap, catchError } from 'rxjs/operators';
import { Observable, throwError, of } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Router } from '@angular/router';

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  private jwtHelper = new JwtHelperService();

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return this.auth.getTokenObservable().pipe(
      take(1),
      switchMap(token => {
        if (token) {
          // 🔍 Proveri da li je token istekao
         /* if (this.jwtHelper.isTokenExpired(token)) {
            this.handleSessionExpired();
            return throwError(() => new Error('Session expired'));
          }*/

          // 🔐 Ako nije istekao, dodaj ga u header
          request = request.clone({
            setHeaders: {
              Authorization: `Bearer ${token}`
            }
          });
        }

        return next.handle(request).pipe(
          catchError((error: HttpErrorResponse) => {
          /*  if (error.status === 401) {
              this.handleSessionExpired();
            }*/
            return throwError(() => error);
          })
        );
      })
    );
  }

  private handleSessionExpired() {
    alert('Sesija je istekla. Molimo vas da se ponovo prijavite.');
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
