import { NgModule } from '@angular/core';
import { HomeComponent } from 'src/app/home/home.component';
import { LoginComponent } from '../auth/login/login.component';
import { RouterModule, Routes } from '@angular/router';

const routes: Routes = [
  {path: 'home', component: HomeComponent},
  {path: '', component: HomeComponent },
  {path: 'login', component: LoginComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class RoutingModule { }
