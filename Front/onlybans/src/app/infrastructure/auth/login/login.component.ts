import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {Subject} from 'rxjs';
import {takeUntil} from 'rxjs/operators';
import { AuthService } from 'src/app/service';
import { UserService } from 'src/app/service/user.service';

interface DisplayMessage {
  msgType: string;
  msgBody: string;
}

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  title = 'Login';
  form!: FormGroup;

  submitted = false;

  notification!: DisplayMessage;

  returnUrl!: string;
  private ngUnsubscribe: Subject<void> = new Subject<void>();


  constructor(
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private formBuilder: FormBuilder
  ) { }

  ngOnInit() {
    this.route.params
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe((params: any) => {
        this.notification = params as DisplayMessage;
      });
    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';
    this.form = this.formBuilder.group({
      email: ['', Validators.compose([Validators.required, Validators.minLength(3), Validators.maxLength(64)])],
      password: ['', Validators.compose([Validators.required, Validators.minLength(3), Validators.maxLength(32)])]
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }
/*
  onSubmit() {
    
    this.notification;
    this.submitted = true;

    this.authService.login(this.form.value)
      .subscribe(data => {
        console.log(data);
          this.userService.getMyInfo().subscribe();

          this.router.navigate([this.returnUrl]);
        },
        error => {
          console.log(error);
          this.submitted = false;
          this.notification = {msgType: 'error', msgBody: 'Incorrect email or password.'};
        });
  }
*/

onSubmit() {
  this.notification;
  this.submitted = true;

  this.authService.login(this.form.value).subscribe({
    next: (data) => {
      console.log(data); // Odgovor sa tokenom

      this.userService.getMyInfo().subscribe((userInfo) => {
        console.log(userInfo);

        // Izvlačenje uloge korisnika
        const userRole = userInfo.roles[0]?.name;

        // Preusmeravanje na odgovarajuću stranicu u zavisnosti od uloge
        if (userRole === 'ROLE_USER') {
          this.router.navigate(['/user-home']);
        } 
        /* else if (userRole === 'ROLE_ADMIN') {
          this.router.navigate(['/admin-home']);
        } */
        else {
          console.error('Unknown role:', userRole);
          this.router.navigate(['/home']);
        }
      });
    },
    error: (error) => {
      console.error(error);
      this.submitted = false;

      if (error.status === 429) {
        // Ograničenje premašeno
        this.notification = {
          msgType: 'error',
          msgBody: 'You have exceeded the allowed number of login attempts. Please try again later.',
        };
      } else {
        // Ostale greške
        this.notification = {
          msgType: 'error',
          msgBody: 'Incorrect email or password.',
        };
      }
    },
  });
}

  
}
