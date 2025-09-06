import { RouterModule, Routes } from "@angular/router";
import { AdminHomeComponent } from "./components/admin-home/admin-home.component";
import { roleGuard } from "src/app/guards/role.guard";
import { AdminAnalyticsComponent } from "./components/admin-analytics/admin-analytics.component";
import { NgModule } from "@angular/core";
import { RegisteredUsersComponent } from "src/app/admin/components/registered-users/registered-users.component";
import { ActivityTrendsComponent } from "../activity-trends/activity-trends.component";
import { UsersPostsComponent } from "./components/users-posts/users-posts.component";

const routes: Routes = [
    {
        path: '',
        component: AdminHomeComponent,
        canActivate: [roleGuard],
        data: { roles: ['ROLE_ADMIN']},
        children: [
            { path: 'analytics', component: AdminAnalyticsComponent},
            { path: 'activity-trends', component: ActivityTrendsComponent},
            { path: 'registered-users', component: RegisteredUsersComponent },
            { path: 'users-posts', component: UsersPostsComponent},
            { path: '', redirectTo: 'analytics', pathMatch: 'full' },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class AdminRoutingModule {}