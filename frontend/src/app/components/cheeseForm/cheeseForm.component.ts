import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { CheeseService } from '../../service/cheese.service';
import { CheeseDTO } from '../../dto/cheese.dto';
import { UserDTO } from '../../dto/user.dto';
import { UserService } from '../../service/user.service';

@Component({
  selector: 'cheese-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './cheeseForm.component.html',
  styleUrls: ['./cheeseForm.component.css']
})
export class CheeseFormComponent {

  name: string = '';
  price: number = 0;
  description: string = '';
  type: string = '';
  manufactureDate: string = '';
  expirationDate: string = '';

  selectedFile: File | null = null;

  isLoggedIn: boolean = false;
  currentUser: UserDTO | null = null;

  constructor(
    private cheeseService: CheeseService,
    private userService: UserService,
    private router: Router
  ) { }

    ngOnInit(): void {
    // Check login status and get current user
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        // If not admin, redirect to error page
        if (!user.rols.includes('ADMIN')) {
          this.router.navigate(['/error']);
        }
      },
      error: (err) => {
        // If not logged in, redirect to error page
        this.router.navigate(['/error']);
      }
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  createCheese(): void {

    if (!this.name || !this.description || !this.type ||
      !this.manufactureDate || !this.expirationDate) {
      alert("Todos los campos son obligatorios");
      return;
    }

    if (this.price <= 0) {
      alert("El precio debe ser mayor que 0");
      return;
    }

    if (this.expirationDate < this.manufactureDate) {
      alert("La fecha de caducidad debe ser posterior a la de fabricación");
      return;
    }

    const cheeseData: CheeseDTO = {
      name: this.name,
      price: this.price,
      description: this.description,
      manufactureDate: this.manufactureDate,
      expirationDate: this.expirationDate,
      type: this.type,
      boxes: []
    };


    this.cheeseService.createCheese(cheeseData).subscribe({
      next: (createdCheese) => {

        alert("Queso creado correctamente");

        if (createdCheese.id) {

          this.cheeseService.uploadCheeseImage(
            createdCheese.id,
            this.selectedFile
          ).subscribe({
            next: () => {
              this.router.navigate(['/']);
            },
            error: () => {
              this.router.navigate(['/']);
            }
          });

        }
      },

      error: (err) => {
        console.error("Error creando queso", err);
        alert("Error al crear queso");
      }
    });


  }
}
