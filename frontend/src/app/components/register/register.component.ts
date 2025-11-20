import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; 
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';

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
export class RegisterComponent {
  nombre: string = '';
  password: string = '';
  confirmPassword: string = '';
  email: string = '';
  direccion: string = '';
  nif: string = '';

  constructor(private loginService: LoginService,private router: Router) {}

    register(): void {
        if (!this.nombre || !this.password || !this.confirmPassword || !this.email || !this.direccion || !this.nif) {
            alert('Todos los campos son obligatorios');
            return;
        }

        if (this.password !== this.confirmPassword) {
            alert('Las contraseñas no coinciden');
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
            alert('Registro exitoso');
            this.router.navigate(['/']);
            },
            error: (err) => {
            console.error('Error en registro', err);
            alert(err.error?.error || 'Error desconocido');
            }
        });
    }

}