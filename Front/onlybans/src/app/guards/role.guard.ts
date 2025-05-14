import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthModule } from '../infrastructure/auth/auth.module';
import { AuthService } from '../service';
import { map } from 'rxjs';

export const roleGuard: CanActivateFn = (route, state) => {

  const authService = inject(AuthService);
  const router = inject(Router);

  return authService.user$.pipe(
    map(user => {
      const allowedRoles: string[] = route.data['roles'];

      if(user?.roles.some(role => allowedRoles.includes(role))){
        return true;
      }
      return false;
    })
  );

};
