import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule
  ],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  username: string = '';
  password: string = '';

  constructor(private loginService: LoginService, private router: Router) { }

  login(): void {
    this.loginService.login(this.username, this.password).subscribe({
      next: (response) => {
        console.log("Login ok:", response);
        alert("¡Login correcto! Los tokens están en las cookies.");
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error("Error en login:", err);
        alert("Credenciales incorrectas.");
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }
}