import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
<<<<<<< HEAD
import { CreatePostComponent } from './create-post/create-post.component';

const routes: Routes = [
  {path: 'create-post', component: CreatePostComponent},
=======
import { UsersComponent } from './users/users.component';

const routes: Routes = [
  {path: 'users', component: UsersComponent}
>>>>>>> Displaying-registered-users
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule,
  ]
})
export class AppRoutingModule { }
