import { Component, OnInit } from '@angular/core';
import { AdminServiceService } from '../../services/admin-service.service';
import { PostCommentAnalytics } from '../../model/post-comment-analytics.model';
import { ArcElement, ChartData, ChartOptions, ChartType, DoughnutController } from 'chart.js';
import { BaseChartDirective } from 'ng2-charts';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatCardModule } from '@angular/material/card';
import { Chart, BarController, BarElement, CategoryScale, LinearScale, Legend, Title, Tooltip } from 'chart.js';
import { UsersActivityPercentagesDTO } from '../../model/user-activity-percentages.model';

// registracija potrebnih elemenata za bar chart
Chart.register(BarController, BarElement, CategoryScale, LinearScale, Legend, Title, Tooltip, ArcElement, DoughnutController);


@Component({
  selector: 'app-admin-analytics',
  templateUrl: './admin-analytics.component.html',
  styleUrls: ['./admin-analytics.component.css'],
  standalone: true,
  imports: [
    BaseChartDirective,
    MatButtonToggleModule,
    MatCardModule,
  ]
})
export class AdminAnalyticsComponent implements OnInit {

  selectedPeriod: 'week' | 'month' | 'year' = 'week';
  analyticsData!: PostCommentAnalytics;

  // Podaci za KPI kartice
  currentStats = {
    posts: 0,
    comments: 0
  };

public barChartType: ChartType = 'bar';
  public barChartData: ChartData<'bar'> = {
    labels: [], // linijski labeli
    datasets: [
      { data: [], label: 'Posts' },
      { data: [], label: 'Comments' }
    ]
  };

  // Radijalni dijagram
  public radarChartLabels: string[] = ['Users with Posts', 'Users with Comments Only', 'Inactive Users'];
  public radarChartData: any;
  public radarChartType: ChartType = 'doughnut';
  public radarChartOptions: ChartOptions = {
    responsive: true,
    plugins: {
      legend: {
        position: 'top',
      }
    }
  };

  constructor(private adminService: AdminServiceService) {}

  ngOnInit(): void {
    this.fetchAnalytics();
    this.loadUserActivityPercentages();
  }

   fetchAnalytics(): void {
    this.adminService.getPostsAndCommentsAnalytics().subscribe({
      next: (data: PostCommentAnalytics) => {
        this.analyticsData = data;
        this.updateView();
      },
      error: (err) => {
        console.error('Error fetching analytics', err);
      }
    });
  }

  changePeriod(period: 'week' | 'month' | 'year'): void {
    this.selectedPeriod = period;
    this.updateView();
  }

  updateView(): void {
  if (!this.analyticsData) return;

  let labels: string[] = [];
  let postsData: number[] = [];
  let commentsData: number[] = [];

  switch(this.selectedPeriod) {
    case 'week':
      labels = ['Mon','Tue','Wed','Thu','Fri','Sat','Sun'];
      postsData = this.analyticsData.weeklyPosts;
      commentsData = this.analyticsData.weeklyComments;
      break;

    case 'month':
      const weeks = this.analyticsData.monthlyPosts.length;
      labels = Array.from({ length: weeks }, (_, i) => `Week ${i+1}`);
      postsData = this.analyticsData.monthlyPosts;
      commentsData = this.analyticsData.monthlyComments;
      break;

    case 'year':
      labels = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
      postsData = this.analyticsData.yearlyPosts;
      commentsData = this.analyticsData.yearlyComments;
      break;
  }

  this.currentStats.posts = postsData.reduce((a, b) => a + b, 0);
  console.log(postsData);
  this.currentStats.comments = commentsData.reduce((a, b) => a + b, 0);

  this.barChartData = {
    labels: labels,
    datasets: [
      { data: postsData, label: 'Posts', backgroundColor: 'rgba(54, 162, 235, 0.6)' },
      { data: commentsData, label: 'Comments', backgroundColor: 'rgba(255, 99, 132, 0.6)' }
    ]
  };
  console.log(this.barChartData);

}

loadUserActivityPercentages(): void {
    this.adminService.getUserActivityPercentages().subscribe(
      (data: UsersActivityPercentagesDTO) => {
        this.radarChartData = {
          labels: this.radarChartLabels,
          datasets: [
            {
              data: [
                data.postsPercentage,
                data.commentsOnlyPercentage,
                data.inactivePercentage
              ],
              backgroundColor: ['#36A2EB', '#FF6384', '#CCCCCC'] // plava, roze, siva           
            }
          ]
        };
        console.log('RadaChartData: ', this.radarChartData)
      }
    );
  }


}
