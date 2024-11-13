import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { UsersComponent } from './users/users.component';
import { CreatePostComponent } from './create-post/create-post.component';
<<<<<<< HEAD
import { PostDetailsComponent } from './post-details/post-details.component';
=======
import { HomeComponent } from './home/home.component';
>>>>>>> development


const routes: Routes = [
  {path: 'create-post', component: CreatePostComponent},
  {path: 'users', component: UsersComponent},
<<<<<<< HEAD
  {path: 'post/:id', component: PostDetailsComponent}
=======
  { path: '', component: HomeComponent },
>>>>>>> development
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule,
  ]
})
export class AppRoutingModule { }
