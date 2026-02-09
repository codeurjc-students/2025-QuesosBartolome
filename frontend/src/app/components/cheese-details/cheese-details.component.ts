import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { CheeseService } from '../../service/cheese.service';
import { CheeseDTO } from '../../dto/cheese.dto';
import { UserDTO } from '../../dto/user.dto';
import { UserService } from '../../service/user.service';
import { CartService } from '../../service/cart.service';

@Component({
  selector: 'app-cheese-details',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './cheese-details.component.html',
  styleUrls: ['./cheese-details.component.css']
})
export class CheeseDetailsComponent implements OnInit {

  cheese!: CheeseDTO;
  imageUrl: string | null = null;

  isLoggedIn: boolean = false;
  currentUser: UserDTO | null = null;

  constructor(
    private route: ActivatedRoute,
    private cheeseService: CheeseService,
    private userService: UserService,
    private router: Router,
    private cartService: CartService
  ) { }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

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

}