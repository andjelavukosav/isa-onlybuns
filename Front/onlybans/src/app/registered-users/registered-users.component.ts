import { Component, ViewChild } from '@angular/core';
import { AuthService, UserService } from '../service';
import { AuthUser, UserDTO, UserSearchCriteria } from '../model/registered-user';
import { Page } from '../model/pagination.model';
import { MatPaginator, PageEvent } from '@angular/material/paginator';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-registered-users',
  templateUrl: './registered-users.component.html',
  styleUrls: ['./registered-users.component.css']
})
export class RegisteredUsersComponent {
  admin: AuthUser | null = null;
  usersPage: Page<UserDTO> | undefined ;

  pageSize: number = 1;
  currentPage : number = 1;
  totalElements: number = 0;
  totalPages: number = 0;

  sortBy: string = 'email';
  previousSortBy: string = 'email';
  sortDirection:'asc' | 'desc' = 'asc';
  previousSortDirection: 'asc'| 'desc' = 'asc';

  sortByEmail: boolean = true;
  sortByFollowing: boolean = false; 

  search : UserSearchCriteria = {
    firstName: '',
    lastName: '',
    email: '',
    minPostsCount: null,
    maxPostsCount: null
  }

  isSearching: boolean = false;
  

  @ViewChild(MatPaginator) paginator! : MatPaginator;

  get visiblePages(): number[]{
    const maxVisible = 2;
    let startPage: number, endPage: number;

    if(this.totalPages <= maxVisible){
      startPage = 1;
      endPage = this.totalPages;
    }else{

      const halfVisible = Math.floor(maxVisible/2);

      if(this.currentPage <= halfVisible){
        startPage = 1;
        endPage = maxVisible;
      }else if(this.currentPage + halfVisible >= this.totalPages){
        startPage = this.totalPages - maxVisible + 1;
        endPage = this.totalPages;
      }else {
        startPage = this.currentPage - halfVisible;
        endPage = this.currentPage + halfVisible;
      }
        
    }
    return Array.from({ length: endPage - startPage + 1 }, (_, i) => startPage + i);
  }

  constructor(private authService: AuthService,
    private userService: UserService,
    private snackBar: MatSnackBar){}

  ngOnInit(): void{

    this.authService.user$.subscribe({
      next: (user) => {
        this.admin = user;
        console.log('Admin is logged in, ', this.admin);
      },
      error: (err) => {
        console.log('An error occurred while trying to capture the logged in user, ', err);  
      }
    });

    this.fetchUsers();

  }

  fetchUsers(): void{
    
    this.userService.getUsersPage(this.currentPage - 1, this.pageSize, this.sortBy, this.sortDirection).subscribe({
      next: (response: Page<UserDTO>) => {
        console.log('Page info: ', response)
        this.usersPage = response;
        this.totalElements = response.totalElements;
        this.totalPages = response.totalPages;
      },
      error: (err) => {
        console.log('An error occurred while fetching users for selected page, ', err);
      }
    })
  }

  onPageChange(event: PageEvent): void{
    console.log('Event emmited from MatPaginator: ', event);

    this.currentPage = event.pageIndex + 1;
    this.pageSize = event.pageSize;

    if(this.isSearching && !this.isSearchCriteriaEmpty()){
      this.onSearchChange();
    }else{
      this.fetchUsers();
    }
  }

  onManualPageChange(page: number): void{
    if (page !== this.currentPage) {
      
      const event: PageEvent = {
        pageIndex: page - 1,
        pageSize: this.pageSize,
        length: this.totalElements
      }
      this.paginator.page.emit(event);
    }
  }


  onSortChange(): void{
    if(!this.sortByEmail){
      this.sortBy = 'followingCount';
    }

    if(this.previousSortBy === this.sortBy && this.previousSortDirection === this.sortDirection){
      return;
    }

    this.previousSortBy = this.sortBy;
    this.previousSortDirection = this.sortDirection;

    this.paginator.page.emit({
      pageIndex: 0,
      length: this.totalElements,
      pageSize: this.pageSize
    });

  }

  isSearchCriteriaEmpty(): boolean{
    return this.search.firstName === '' && this.search.lastName === '' && this.search.email === ''
            && this.search.minPostsCount === null && this.search.maxPostsCount === null;
  }

  onSearchClick(): void{

    if(this.search.minPostsCount !== null && this.search.maxPostsCount != null){
      if(this.search.minPostsCount > this.search.maxPostsCount){
        this.showToast('warning', 'The min posts count cannot be greater than the max posts count.');
        return;
      }
    }
    this.isSearching = true;

    if(this.isSearching && !this.isSearchCriteriaEmpty()){
      this.currentPage = 1;
      this.onSearchChange();
    }
    else{
      this.isSearching = false;
      this.fetchUsers();
    }

  }

  onSearchChange(): void{

    this.userService.searchUserss(this.search, this.currentPage - 1, this.pageSize, this.sortBy, this.sortDirection)
    .subscribe({
      next: (results: Page<UserDTO>) => {
        this.usersPage = results;
        this.totalElements = results.totalElements;
        this.totalPages = results.totalPages;
        console.log('Searching data: ', results.content);
      },
      error: (err) => {
        console.log('An error occured while searching users: ', err);
      }
    });

  }

  showToast(type: string, message: string): void {
    this.snackBar.open(message, type, {
      duration: 4000, // Trajanje obavještenja
      horizontalPosition: 'center',
      verticalPosition: 'top',
      panelClass: type, 
    });
  }
 
}
