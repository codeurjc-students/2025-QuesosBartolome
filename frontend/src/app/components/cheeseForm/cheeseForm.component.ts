import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
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
export class CheeseFormComponent implements OnInit {

  name: string = '';
  price: number = 0;
  description: string = '';
  type: string = '';
  manufactureDate: string = '';
  expirationDate: string = '';

  selectedFile: File | null = null;

  isLoggedIn: boolean = false;
  currentUser: UserDTO | null = null;

  isEditMode: boolean = false;
  cheeseId: number | null = null;

  constructor(
    private cheeseService: CheeseService,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    // Check if we're in edit mode
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.cheeseId = Number(id);
      this.loadCheeseData(this.cheeseId);
    }

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

  loadCheeseData(id: number): void {
    this.cheeseService.getCheeseById(id).subscribe({
      next: (cheese) => {
        this.name = cheese.name;
        this.price = cheese.price;
        this.description = cheese.description;
        this.type = cheese.type;
        this.manufactureDate = cheese.manufactureDate;
        this.expirationDate = cheese.expirationDate;
      },
      error: (err) => {
        console.error('Error loading cheese data', err);
        alert('Error al cargar los datos del queso');
        this.router.navigate(['/']);
      }
    });
  }

  onFileSelected(event: any) {
    this.selectedFile = event.target.files[0];
  }

  createCheese(): void {
    if (!this.validateForm()) {
      return;
    }

    const cheeseData: CheeseDTO = this.buildCheeseData();

    this.cheeseService.createCheese(cheeseData).subscribe({
      next: (createdCheese) => {
        alert("Queso creado correctamente");

        // If there's a file to upload, upload it first
        if (createdCheese.id && this.selectedFile) {
          this.cheeseService.uploadCheeseImage(
            createdCheese.id,
            this.selectedFile
          ).subscribe({
            next: () => {
              this.router.navigate(['/cheeses']);
            },
            error: () => {
              this.router.navigate(['/cheeses']);
            }
          });
        } else {
          // No file to upload, navigate directly
          this.router.navigate(['/cheeses']);
        }
      },
      error: (err) => {
        console.error("Error creando queso", err);
        if (err.status === 400) {
          alert("Error al crear queso. Verifica que no exista otro queso con el mismo nombre.");
        } else {
          alert("Error al crear queso");
        }
      }
    });
  }

  editCheese(): void {
    if (!this.validateForm()) {
      return;
    }

    const cheeseData: CheeseDTO = this.buildCheeseData();
    cheeseData.id = this.cheeseId!;

    this.cheeseService.updateCheese(this.cheeseId!, cheeseData).subscribe({
      next: (updatedCheese) => {
        alert("Queso actualizado correctamente");

        if (this.selectedFile) {
          this.cheeseService.updateCheeseImage(
            this.cheeseId!,
            this.selectedFile
          ).subscribe({
            next: () => {
              this.router.navigate(['/cheeses', this.cheeseId]);
            },
            error: () => {
              this.router.navigate(['/cheeses', this.cheeseId]);
            }
          });
        } else {
          this.router.navigate(['/cheeses', this.cheeseId]);
        }
      },
      error: (err) => {
        console.error("Error actualizando queso", err);
        if (err.status === 400) {
          alert("Error al actualizar queso. Verifica que no exista otro queso con el mismo nombre.");
        } else {
          alert("Error al actualizar queso");
        }
      }
    });
  }

  private validateForm(): boolean {
    // Trim strings to detect empty fields properly
    const trimmedName = this.name?.trim() || '';
    const trimmedDescription = this.description?.trim() || '';
    const trimmedType = this.type?.trim() || '';
    const trimmedManufactureDate = this.manufactureDate?.trim() || '';
    const trimmedExpirationDate = this.expirationDate?.trim() || '';

    if (!trimmedName || !trimmedDescription || !trimmedType ||
      !trimmedManufactureDate || !trimmedExpirationDate) {
      alert("Todos los campos son obligatorios");
      return false;
    }

    if (!this.price || this.price <= 0) {
      alert("El precio debe ser mayor que 0");
      return false;
    }

    const datePattern = /^\d{4}-\d{2}-\d{2}$/;
    if (!datePattern.test(this.manufactureDate)) {
      alert("La fecha de fabricación debe estar en formato YYYY-MM-DD");
      return false;
    }

    if (!datePattern.test(this.expirationDate)) {
      alert("La fecha de caducidad debe estar en formato YYYY-MM-DD");
      return false;
    }

    if (this.expirationDate < this.manufactureDate) {
      alert("La fecha de caducidad debe ser posterior a la de fabricación");
      return false;
    }

    return true;
  }

  private buildCheeseData(): CheeseDTO {
    return {
      name: this.name,
      price: this.price,
      description: this.description,
      manufactureDate: this.manufactureDate,
      expirationDate: this.expirationDate,
      type: this.type,
      boxes: []
    };
  }
}
