import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { CheeseService } from '../../service/cheese.service';
import { CheeseDTO } from '../../dto/cheese.dto';
import { Router } from '@angular/router';
import { UserService } from '../../service/user.service';
import { LoginService } from '../../service/login.service';
import { UserDTO } from '../../dto/user.dto';

@Component({
  selector: 'app-cheese-list',
  standalone: true,
  imports: [
    CommonModule,
    NgbDropdownModule,
    NgbTooltipModule
  ],
  templateUrl: './cheese-list.component.html',
  styleUrls: ['./cheese-list.component.css']
})
export class CheeseListComponent implements OnInit {
  cheeses: CheeseDTO[] = [];

  isLoggedIn: boolean = false;
  currentUser: UserDTO | null = null;

  constructor(
    private cheeseService: CheeseService, 
    private router: Router,
    private userService: UserService,
    private loginService: LoginService
  ) {}

  ngOnInit(): void {
    // Load cheeses
    this.cheeseService.getAllCheeses().subscribe({
      next: (list) => {
        this.cheeses = list;
      },
      error: (err) => console.error('Error loading cheeses', err)
    });

    // Check login status and get current user
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isLoggedIn = true;
      },
      error: (err) => {
        // If there is no valid token or an error occurs, consider that no user is logged in
        this.currentUser = null;
        this.isLoggedIn = false;
      }
    });
  }

  goToLogin(): void {
    this.router.navigate(['/auth/login']);
  }

  goToRegister(): void {
    this.router.navigate(['/auth/register']);
  }
  goToProfile(): void {
    this.router.navigate(['/user']);
  }
  goToAbout(): void {
    this.router.navigate(['/about-us']);
  }
  goToDetails(id: number): void {
    this.router.navigate(['/cheeses', id]);
  }

  logout(): void {
    this.loginService.logout().subscribe({
      next: () => {
        this.isLoggedIn = false;
        this.currentUser = null;
        console.log('Logged out successfully');
        this.router.navigate(['/']);
      },
      error: (err) => console.error('Error during logout', err)
    });
  }

  openNewCheeseModal(): void {
    alert('Open modal for creating a new cheese (not implemented)');
  }
  
  isAdmin(): boolean {
    return this.currentUser?.rols?.includes("ADMIN") ?? false;
  }

  isUser(): boolean {
    return this.currentUser?.rols?.includes("USER") ?? false;
  }



}
