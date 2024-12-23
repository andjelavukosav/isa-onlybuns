import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsersComponent } from './users/users.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { PostDetailsComponent } from './post-details/post-details.component';
import { HomeComponent } from './home/home.component';


const routes: Routes = [
  {path: 'create-post', component: CreatePostComponent},
  {path: 'users', component: UsersComponent},
  {path: 'post/:id', component: PostDetailsComponent},
  { path: '', component: HomeComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule,
  ]
})
export class AppRoutingModule { }
