import { NgModule } from '@angular/core';
import { HomeComponent } from 'src/app/home/home.component';
import { LoginComponent } from '../auth/login/login.component';
import { RouterModule, Routes } from '@angular/router';
import { RegistrationComponent } from '../auth/registration/registration.component';
import { NavbarComponent } from 'src/app/navbar/navbar.component';
import { CreatePostComponent } from 'src/app/create-post/create-post.component';
import { UserProfileComponent } from 'src/app/user-profile/user-profile.component';
import { UserHomeComponent } from 'src/app/user-home/user-home.component';
import { ActivityTrendsComponent } from 'src/app/activity-trends/activity-trends.component';
import { ChatComponent } from 'src/app/chat/chat.component';
import { FollowedUserPostComponent } from 'src/app/followed-user-post/followed-user-post.component';
import { NearbyPostsMapComponent } from 'src/app/nearby-posts-map/nearby-posts-map.component';

const routes: Routes = [
  {path: 'home', component: HomeComponent},
  { 
    path: '',
    component: HomeComponent,  
    pathMatch: 'full'
  },
  {path: 'login', component: LoginComponent},
  {path: 'signup', component: RegistrationComponent},
  {path: 'create-post', component: CreatePostComponent},
  {path: 'profile/:userId', component: UserProfileComponent},
  {path: 'user-home', component: UserHomeComponent},
  {path: 'activity-trends', component: ActivityTrendsComponent},
  {path: 'chat', component: ChatComponent},
  {path: 'followed-user-post', component: FollowedUserPostComponent},
  {path: 'nearby-posts-map', component: NearbyPostsMapComponent}
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class RoutingModule { }
