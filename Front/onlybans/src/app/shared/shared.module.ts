import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PostComponent } from '../post/post.component';
import { RouterModule } from '@angular/router';

@NgModule({
  declarations: [
    PostComponent,  
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule
  ],
  exports: [
    PostComponent,
    CommonModule,
    FormsModule,
    RouterModule
  ],
})
export class SharedModule {}
