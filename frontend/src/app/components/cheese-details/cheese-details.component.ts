import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { CheeseService } from '../../service/cheese.service';
import { CheeseDTO } from '../../dto/cheese.dto';
import { UserDTO } from '../../dto/user.dto';
import { UserService } from '../../service/user.service';
import { CartService } from '../../service/cart.service';
import { ReviewService } from '../../service/review.service';
import { ReviewDTO } from '../../dto/review.dto';
import { Page } from '../../dto/page.dto';

@Component({
  selector: 'app-cheese-details',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cheese-details.component.html',
  styleUrls: ['./cheese-details.component.css']
})
export class CheeseDetailsComponent implements OnInit {

  cheese!: CheeseDTO;
  imageUrl: string | null = null;

  isLoggedIn: boolean = false;
  currentUser: UserDTO | null = null;

  reviews: ReviewDTO[] = [];
  currentPage: number = 0;
  totalPages: number = 0;
  totalReviews: number = 0;
  reviewsPerPage: number = 3;

  showReviewForm: boolean = false;
  newRating: number = 5;
  newComment: string = '';

  constructor(
    private route: ActivatedRoute,
    private cheeseService: CheeseService,
    private userService: UserService,
    private router: Router,
    private cartService: CartService,
    private reviewService: ReviewService
  ) { }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.loadReviews(id, 0);
    this.loadCheese(id);

    // Check if user is logged in
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isLoggedIn = true;
      },
      error: (err) => {
        // If token is invalid or request fails, consider user as logged out
        this.currentUser = null;
        this.isLoggedIn = false;
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  loadCheese(id: number): void {
    this.cheeseService.getCheeseById(id).subscribe({
      next: (data) => {
        this.cheese = data;
        this.loadCheeseImage(data.id!);
      },
      error: err => console.error('Error obteniendo queso', err)
    });
  }

  loadCheeseImage(id: number) {
    this.cheeseService.getCheeseImage(id).subscribe({
      next: (blob) => {
        if (blob.size > 0) {
          this.imageUrl = URL.createObjectURL(blob);
        } else {
          this.imageUrl = "assets/cheese-default.png";
        }
      },
      error: () => {
        this.imageUrl = "assets/cheese-default.png";
      }
    });
  }

  addToOrder(boxesValue: string) {

    // Validate input
    if (!boxesValue) {
      alert('Debes introducir una cantidad');
      return;
    }

    const boxes = Number(boxesValue);

    if (isNaN(boxes) || boxes <= 0 || boxes > this.cajasDisponibles) {
      alert('Ingrese una cantidad correcta');
      return;
    }

    if (!this.currentUser) {
      alert('Debes estar logueado');
      return;
    }

    const userId = this.currentUser.id;
    const cheeseId = this.cheese.id;

    this.cartService.addCheeseToOrder(userId, cheeseId!, boxes)
      .subscribe({
        next: () => {
          alert('Producto añadido al pedido');
          this.loadCheese(cheeseId!);
        },
        error: (err) => {
          console.error('FULL ERROR:', err);
          alert('Error al añadir el producto');
          if (err.status >= 500) {
            this.router.navigate(['/error']);
          }
        }
      });
  }

  get cajasDisponibles(): number {
    return this.cheese?.boxes?.length ?? 0;
  }

  get isUser(): boolean {
    return this.currentUser?.rols?.includes('USER') ?? false;
  }

  get isAdmin(): boolean {
    return this.currentUser?.rols?.includes('ADMIN') ?? false;
  }

  editCheese(): void {
    if (this.cheese && this.cheese.id) {
      this.router.navigate(['/cheeses', this.cheese.id, 'edit']);
    }
  }

  deleteCheese(): void {
    if (!this.cheese || !this.cheese.id) {
      return;
    }

    if (confirm(`¿Estás seguro de que quieres eliminar el queso "${this.cheese.name}"?`)) {
      this.cheeseService.deleteCheese(this.cheese.id).subscribe({
        next: () => {
          alert('Queso eliminado correctamente');
          this.router.navigate(['/cheeses']);
        },
        error: (err) => {
          console.error('Error al eliminar el queso:', err);
          alert('Error al eliminar el queso');
          if (err.status >= 500) {
            this.router.navigate(['/error']);
          }
        }
      });
    }
  }
  loadReviews(cheeseId: number, page: number): void {
    this.reviewService.getReviewsByCheeseId(cheeseId, page, this.reviewsPerPage).subscribe({
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
      this.loadReviews(this.cheese.id!, this.currentPage + 1);
    }
  }

  previousPage(): void {
    if (this.currentPage > 0) {
      this.loadReviews(this.cheese.id!, this.currentPage - 1);
    }
  }

  getStars(rating: number): string[] {
    const stars: string[] = [];
    for (let i = 1; i <= 5; i++) {
      stars.push(i <= rating ? '★' : '☆');
    }
    return stars;
  }

  getUserImageUrl(userId: number): string {
    return `https://localhost:443/api/v1/users/${userId}/image`;
  }

  goToUserProfile(userId: number): void {
    this.router.navigate(['/user', userId]);
  }

  toggleReviewForm(): void {
    this.showReviewForm = !this.showReviewForm;
    if (!this.showReviewForm) {
      // Reset form when closing
      this.newRating = 5;
      this.newComment = '';
    }
  }

  submitReview(): void {
    // Validate rating
    if (this.newRating < 0 || this.newRating > 5) {
      alert('La puntuación debe estar entre 0 y 5');
      return;
    }

    // Validate comment
    if (!this.newComment || this.newComment.trim() === '') {
      alert('El comentario es obligatorio');
      return;
    }

    // Create review
    this.reviewService.createReview(this.newRating, this.newComment.trim(), this.cheese.id!).subscribe({
      next: () => {
        alert('Reseña creada correctamente');
        this.showReviewForm = false;
        this.newRating = 5;
        this.newComment = '';
        // Reload reviews
        this.loadReviews(this.cheese.id!, 0);
      },
      error: (err) => {
        console.error('Error creating review:', err);
        alert('Error al crear la reseña');
      }
    });
  }

  cancelReview(): void {
    this.showReviewForm = false;
    this.newRating = 5;
    this.newComment = '';
  }

  deleteReviewFromCheese(reviewId: number): void {
    if (!confirm('¿Estás seguro de que quieres eliminar esta reseña?')) {
      return;
    }

    this.reviewService.deleteReview(reviewId).subscribe({
      next: () => {
        alert('Reseña eliminada correctamente');
        // Reload reviews after deletion
        this.loadReviews(this.cheese.id!, this.currentPage);
      },
      error: (err) => {
        console.error('Error deleting review', err);
        alert('No se pudo eliminar la reseña');
      }
    });
  }

}