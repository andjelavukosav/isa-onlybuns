import { NgModule, NO_ERRORS_SCHEMA } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import {HttpClientModule} from '@angular/common/http';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { NavbarComponent } from './navbar/navbar.component';
import { HomeComponent } from './home/home.component';
import { MaterialModule } from './infrastructure/material/material.module';
import { LoginComponent } from './infrastructure/auth/login/login.component';
import { RoutingModule } from './infrastructure/routing/routing.module';
import { CardComponent } from './card/card.component';
import { RegistrationComponent } from './infrastructure/auth/registration/registration.component';

import {FormsModule, ReactiveFormsModule} from '@angular/forms';

import { ApiService } from './service';
import { FooService } from './service';
import { AuthService } from './service';
import { UserService } from './service';
import { ConfigService } from './service';
import { PostService } from './service/post.service';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { TokenInterceptor } from './interceptor/TokenInterceptor';
import { PostComponent } from './post/post.component';
import { CreatePostComponent } from './create-post/create-post.component';
import { LeafletModule } from '@asymmetrik/ngx-leaflet';

import { UsersComponent } from './users/users.component';
import { UserProfileComponent } from './user-profile/user-profile.component';
import { PostDetailsComponent } from './post-details/post-details.component';
import { UserHomeComponent } from './user-home/user-home.component';
import { FollowedUserPostComponent } from './followed-user-post/followed-user-post.component';
import { ActivityTrendsComponent } from './activity-trends/activity-trends.component';
import { NearbyPostsMapComponent } from './nearby-posts-map/nearby-posts-map.component';
import { ChatComponent } from './chat/chat.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    NavbarComponent,
    HomeComponent,
    CardComponent,
    RegistrationComponent,
    PostComponent,
    CreatePostComponent,
    RegistrationComponent,
    UsersComponent,
    UserProfileComponent,
    PostDetailsComponent,
    UserHomeComponent,
    FollowedUserPostComponent,
    ActivityTrendsComponent,
    NearbyPostsMapComponent,
    ChatComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    MaterialModule,
    RoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule,
    NoopAnimationsModule,
    LeafletModule
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: TokenInterceptor,
      multi: true
    },
    FooService,
    AuthService,
    ApiService,
    UserService,
    ConfigService,
    PostService
  ],
  bootstrap: [AppComponent],
  schemas: [NO_ERRORS_SCHEMA]
})
export class AppModule { }
