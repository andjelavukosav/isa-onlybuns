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
      confirmPassword: ['', [Validators.required]],
      firstname: ['', [Validators.required]],
      lastname: ['', [Validators.required]],
      email: ['', [Validators.required]],
      address: this.formBuilder.group({
        country: ['', [Validators.required]],
        city: ['', [Validators.required]],
        street: ['', [Validators.required]],
        streetNumber: ['', [Validators.required]]
      })
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(group: FormGroup) {
    const password = group.get('password')?.value;
    const confirmPassword = group.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { passwordMismatch: true };
  }

  ngOnDestroy() {
    this.ngUnsubscribe.next();
    this.ngUnsubscribe.complete();
  }

  // Method to handle form submission
  onSubmit() {
    this.submitted = true;

    if (this.form.invalid) {
        this.submitted = false;
        alert('Passwords do not match. Please correct the passwords.');
        return;
    }

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

    this.authService.signup(formData).subscribe(
        data => {
            alert('Signup successful: You have been successfully registered! Please verify your account via email.');
            this.router.navigate(['/login']);
        },
        error => {
            let errorMessage = 'An error occurred';

            if (error.status === 409) {
                errorMessage = 'This email or username is already taken. Please use a different one.';
            } else if (error.status === 400) {
                errorMessage = 'Invalid password format. Please ensure your password meets the requirements.';
            } else if (error.status === 500) {
                errorMessage = 'Server error. Please try again later.';
            } else {
              errorMessage = 'Signup successful: You have been successfully registered! Please verify your account via email.';
              this.router.navigate(['/login']);
            }
            

            alert(errorMessage);
            this.submitted = false;
        }
    );
}

  
}
