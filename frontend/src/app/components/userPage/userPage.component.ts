import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { UserService } from '../../service/user.service';
import { ReviewService } from '../../service/review.service';
import { UserDTO } from '../../dto/user.dto';
import { ReviewDTO } from '../../dto/review.dto';
import { Page } from '../../dto/page.dto';

@Component({
  selector: 'app-userPage',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './userPage.component.html',
  styleUrls: ['./userPage.component.css']
})
export class UserPageComponent implements OnInit {

  user: UserDTO = {
    id: 0,
    name: '',
    password: '',
    gmail: '',
    direction: '',
    nif: '',
    rols: []
  };

  currentUser: UserDTO | null = null;
  isLoggedIn: boolean = false;
  isOwnProfile: boolean = false;
  isUserAdmin: boolean = false;

  imageUrl: string | null = null;

  reviews: ReviewDTO[] = [];
  currentPage: number = 0;
  totalPages: number = 0;
  totalReviews: number = 0;
  reviewsPerPage: number = 3;

  constructor(
    private userService: UserService,
    private reviewService: ReviewService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const userId = this.route.snapshot.paramMap.get('id');
    
    // Check if user is logged in
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isLoggedIn = true;
        
        if (userId) {
          // Load specific user profile
          this.loadUserById(Number(userId));
        } else {
          // Load own profile
          this.user = user;
          this.isOwnProfile = true;
          this.isUserAdmin = user.rols?.includes('ADMIN') ?? false;
          this.loadUserImage(user.id);
          if (!this.isUserAdmin) {
            this.loadReviews(user.id, 0);
          }
        }
      },
      error: (err) => {
        this.isLoggedIn = false;
        this.currentUser = null;
        
        if (userId) {
          this.loadUserById(Number(userId));
        } else {
          this.router.navigate(['/auth/login']);
        }
      }
    });
  }

  loadUserById(id: number) {
    this.userService.getUserById(id).subscribe({
      next: (data) => {
        this.user = data;
        this.isOwnProfile = this.isLoggedIn && this.currentUser?.id === id;
        this.isUserAdmin = data.rols?.includes('ADMIN') ?? false;
        this.loadUserImage(data.id);
        if (!this.isUserAdmin) {
          this.loadReviews(id, 0);
        }
      },
      error: (err) => {
        console.error("No se pudo cargar el usuario");
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        } else {
          this.router.navigate(['/cheeses']);
        }
      }
    });
  }

  loadUserImage(id: number) {
    this.userService.getUserImage(id).subscribe({
      next: (blob) => {
        if (blob.size > 0) {
          this.imageUrl = URL.createObjectURL(blob);
        } else {
          this.imageUrl = "assets/avatar-default.png";
        }
      },
      error: () => {
        this.imageUrl = "assets/avatar-default.png";
      }
    });
  }

  loadReviews(userId: number, page: number): void {
    this.reviewService.getReviewsByUserId(userId, page, this.reviewsPerPage).subscribe({
      next: (data: Page<ReviewDTO>) => {
        this.reviews = data.content;
        this.currentPage = data.number;
        this.totalPages = data.totalPages;
        this.totalReviews = data.totalElements;
      },
      error: err => console.error('Error loading reviews', err)
    });
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) {
      this.loadReviews(this.user.id, this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.loadReviews(this.user.id, this.currentPage - 1);
    }
  }

  getStars(rating: number): string[] {
    const stars: string[] = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= rating ? '★' : '☆');
    }
    return stars;
  }

  goToCheese(cheeseId: number): void {
    this.router.navigate(['/cheeses', cheeseId]);
  }

  deleteReview(reviewId: number): void {
    if (!confirm('¿Estás seguro de que quieres eliminar esta reseña?')) {
      return;
    }

    this.reviewService.deleteReview(reviewId).subscribe({
      next: () => {
        // Reload reviews after deletion
        this.loadReviews(this.user.id, this.currentPage);
      },
      error: (err) => {
        console.error('Error deleting review', err);
        alert('No se pudo eliminar la reseña');
      }
    });
  }

  changePassword() {
    alert("Abrir modal para cambiar contraseña");
  }

  editProfile() {
    alert("Editar perfil habilitado");
  }
}
