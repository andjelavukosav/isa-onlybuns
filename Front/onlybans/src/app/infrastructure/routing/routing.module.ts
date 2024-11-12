import { NgModule } from '@angular/core';
import { HomeComponent } from 'src/app/home/home.component';
import { LoginComponent } from '../auth/login/login.component';
import { RouterModule, Routes } from '@angular/router';
import { RegistrationComponent } from '../auth/registration/registration.component';
import { NavbarComponent } from 'src/app/navbar/navbar.component';
import { CreatePostComponent } from 'src/app/create-post/create-post.component';

const routes: Routes = [
  {path: 'home', component: HomeComponent},
  { 
    path: '',
    component: HomeComponent,  
    pathMatch: 'full'
  },
  {path: 'login', component: LoginComponent},
  {path: 'signup', component: RegistrationComponent},
  {path: 'create-post', component: CreatePostComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class RoutingModule { }
