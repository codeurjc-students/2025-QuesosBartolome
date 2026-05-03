import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { CheeseService } from '../../service/cheese.service';
import { CheeseDTO } from '../../dto/cheese.dto';
import { Router } from '@angular/router';
import { UserService } from '../../service/user.service';
import { LoginService } from '../../service/login.service';
import { UserDTO } from '../../dto/user.dto';
import { DialogService } from '../../service/dialog.service';

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
  allCheeses: CheeseDTO[] = []; // ALL cheeses from server (all pages combined)
  filteredCheeses: CheeseDTO[] = [];
  
  // Filtrado
  selectedType: string = 'Todos';
  cheeseTypes: string[] = ['Todos', 'Pasta prensada', 'Cremoso', 'Maduración fúngica'];
  
  // Paginación (local, client-side)
  currentPage: number = 1; // 1-based for the UI
  itemsPerPage: number = 10;
  totalPages: number = 1;

  isLoggedIn: boolean = false;
  currentUser: UserDTO | null = null;

  constructor(
    private cheeseService: CheeseService,
    private router: Router,
    private userService: UserService,
    private loginService: LoginService,
    private dialogService: DialogService
  ) { }

  ngOnInit(): void {
    // Load ALL pages of cheeses (combine all server pages)
    this.loadAllPages(0);

    // Check login status and get current user
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isLoggedIn = true;
      },
      error: (err) => {
        this.currentUser = null;
        this.isLoggedIn = false;
      }
    });
  }

  loadAllPages(pageIndex: number): void {
    this.cheeseService.getAllCheeses(pageIndex, this.itemsPerPage).subscribe({
      next: (p) => {
        // Add current page to allCheeses
        this.allCheeses.push(...p.content);
        
        // If there are more pages, load them recursively
        if (!p.last) {
          this.loadAllPages(pageIndex + 1);
        } else {
          // All pages loaded, apply filter
          this.applyFilter();
        }
      },
      error: (err) => {
        console.error('Error loading cheeses', err);
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
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
  goToMyOrder(): void {
    this.router.navigate(['/myorder']);
  }
  goToOrders(): void {
    this.router.navigate(['/orders']);
  }
  goToInvoices(): void {
    this.router.navigate(['/invoices']);
  }
  goToClients(): void {
    this.router.navigate(['/users']);
  }
  goToNewCheese(): void {
    this.router.navigate(['/newCheese']);
  }
  goToStock(): void {
    this.router.navigate(['/stock']);
  }

  goToCharts(): void {
    this.router.navigate(['/charts']);
  }

  logout(): void {
    this.loginService.logout().subscribe({
      next: () => {
        this.isLoggedIn = false;
        this.currentUser = null;
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error('Error during logout', err)
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  isAdmin(): boolean {
    return this.currentUser?.rols?.includes("ADMIN") ?? false;
  }

  isUser(): boolean {
    return this.currentUser?.rols?.includes("USER") ?? false;
  }

  // Filtrado
  selectType(type: string): void {
    this.selectedType = type;
    this.currentPage = 1; // Reset to first page
    this.applyFilter();
  }

  applyFilter(): void {
    // Filter based on selected type from ALL cheeses (not just current page)
    if (this.selectedType === 'Todos') {
      this.filteredCheeses = [...this.allCheeses];
    } else {
      this.filteredCheeses = this.allCheeses.filter(cheese => cheese.type === this.selectedType);
    }
    // Recalculate totalPages based on filtered results
    this.totalPages = Math.ceil(this.filteredCheeses.length / this.itemsPerPage);
    // Ensure totalPages is at least 1
    if (this.totalPages === 0) {
      this.totalPages = 1;
    }
  }

  // Get displayed cheeses for current page (local client-side pagination)
  get displayedCheeses(): CheeseDTO[] {
    const startIdx = (this.currentPage - 1) * this.itemsPerPage;
    const endIdx = startIdx + this.itemsPerPage;
    return this.filteredCheeses.slice(startIdx, endIdx);
  }

  get firstDisplayedIndex(): number {
    return this.filteredCheeses.length === 0 ? 0 : (this.currentPage - 1) * this.itemsPerPage + 1;
  }

  get lastDisplayedIndex(): number {
    const endIdx = this.currentPage * this.itemsPerPage;
    return Math.min(endIdx, this.filteredCheeses.length);
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage(): void {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) {
      this.currentPage = page;
    }
  }

  getPageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

}
