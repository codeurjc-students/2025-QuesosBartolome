import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';
import { DialogService } from '../../service/dialog.service';
import { UserService } from '../../service/user.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css']
})
export class RegisterComponent implements OnInit {
  nombre: string = '';
  password: string = '';
  confirmPassword: string = '';
  email: string = '';
  direccion: string = '';
  nif: string = '';

  constructor(
    private loginService: LoginService,
    private userService: UserService,
    private router: Router,
    private dialogService: DialogService
  ) { }

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: () => {
        this.router.navigate(['/']);
      },
      error: (err) => {
        // Unauthenticated users are allowed to stay on the register page.
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  register(): void {
    if (!this.nombre || !this.password || !this.confirmPassword || !this.email || !this.direccion || !this.nif) {
      this.dialogService.alert('Todos los campos son obligatorios');
      return;
    }

    if (this.password.length < 8) {
      this.dialogService.alert('La contraseña debe tener al menos 8 caracteres');
      return;
    }

    if (this.password !== this.confirmPassword) {
      this.dialogService.alert('Las contraseñas no coinciden');
      return;
    }

    const userData = {
      name: this.nombre,
      gmail: this.email,
      password: this.password,
      direction: this.direccion,
      nif: this.nif
    };

    this.loginService.register(userData).subscribe({
      next: () => {
        this.dialogService.alert('Registro exitoso');
        this.router.navigate(['/']);
      },
      error: (err) => {
        // Avoid console logging in UI code; show user-friendly message
        this.dialogService.alert(err.error?.error || 'Error desconocido');
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

}