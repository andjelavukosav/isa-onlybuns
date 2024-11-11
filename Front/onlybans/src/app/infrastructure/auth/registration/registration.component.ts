import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { AuthService } from 'src/app/service';
import { UserService } from 'src/app/service/user.service';

interface DisplayMessage {
  msgType: string;
  msgBody: string;
}

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.css']
})

export class RegistrationComponent implements OnInit {

  title = 'Sign up';
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
  ) {}

  ngOnInit() {
    this.route.params
      .pipe(takeUntil(this.ngUnsubscribe))
      .subscribe((params: any) => {
        this.notification = params as DisplayMessage;
      });

    // get return url from route parameters or default to '/'
    this.returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/';

    // Initialize the form group
    this.form = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(64)]],
      password: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(32)]],
      firstname: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: ['', [Validators.required]],
      address: this.formBuilder.group({
        country: ['', [Validators.required]],
        city: ['', [Validators.required]],
        street: ['', [Validators.required]],
        streetNumber: ['', [Validators.required]]
      })
    });
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  // Method to handle form submission
  onSubmit() {
    this.submitted = true;

  
    try {
      const formData = {
        username: this.form.value.username,
        password: this.form.value.password,
        firstname: this.form.value.firstname,
        lastname: this.form.value.lastname,
        email: this.form.value.email,
        address: {
          country: this.form.value.address.country,
          city: this.form.value.address.city,
          street: this.form.value.address.street,
          streetNumber: this.form.value.address.streetNumber
        }
      };
  
      console.log('Submitting form data:', formData);
  
      this.authService.signup(formData)
        .subscribe(
          data => {
            console.log('Signup successful:', data);
            this.notification = {
              msgType: 'success',
              msgBody: 'You have been successfully registered! Please log in.'
            };
  
            this.authService.login(formData).subscribe(() => {
              this.userService.getMyInfo().subscribe();
            });
  
            this.router.navigate([this.returnUrl]);
          },
          error => {
            this.submitted = false;
            console.log('Sign up error', error);
  
            if (error.status === 409) {
              this.notification = {
                msgType: 'error',
                msgBody: 'This email is already taken. Please use a different one.'
              };
            }
          }
        );
    } catch (error) {
      console.error('Error in onSubmit:', error);
    }
  }
  
}
