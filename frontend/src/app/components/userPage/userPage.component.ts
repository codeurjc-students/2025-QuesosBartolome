import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';

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
    nif: ''
  };

  imageUrl: string | null = null;

  placeholderRatings = [
  ];

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUser();
  }

  loadUser() {
    this.userService.getCurrentUser().subscribe({
      next: (data) => {
        this.user = data;
        this.loadUserImage(data.id);
      },
      error: () => console.warn("No se pudo cargar el usuario")
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

  changePassword() {
    alert("Abrir modal para cambiar contraseña");
  }

  editProfile() {
    alert("Editar perfil habilitado");
  }
}
