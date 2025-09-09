import { NgModule } from "@angular/core";
import { AdminHomeComponent } from "./components/admin-home/admin-home.component";
import { AdminAnalyticsComponent } from "./components/admin-analytics/admin-analytics.component";
import { CommonModule } from "@angular/common";
import { AdminRoutingModule } from "./admin-routing.module";
import { RegisteredUsersComponent } from "src/app/admin/components/registered-users/registered-users.component";
import { FormsModule, ReactiveFormsModule } from "@angular/forms";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatButtonToggleModule } from "@angular/material/button-toggle";
import { MatCardModule } from "@angular/material/card";
import { BaseChartDirective } from "ng2-charts";
import { UsersPostsComponent } from './components/users-posts/users-posts.component';
import { SharedModule } from "../shared/shared.module";

@NgModule({
    declarations: [
        AdminHomeComponent,
        RegisteredUsersComponent,
        UsersPostsComponent,
    ],
    imports: [
        SharedModule,
        CommonModule,
        AdminRoutingModule,
        FormsModule,
        ReactiveFormsModule,
        MatPaginatorModule,
        MatButtonToggleModule,
        MatCardModule,
        AdminAnalyticsComponent,

    ],
    
})
export class AdminModule {}