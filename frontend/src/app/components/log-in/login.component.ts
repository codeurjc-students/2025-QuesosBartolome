import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';
import { DialogService } from '../../service/dialog.service';

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

  constructor(private loginService: LoginService, private router: Router, private dialogService: DialogService) { }

  login(): void {
    this.loginService.login(this.username, this.password).subscribe({
      next: (response) => {
        console.log("Login ok:", response);
        this.dialogService.alert('Inicio de sesión correcto');
        this.router.navigate(['/']);
      },
      error: (err) => {
        console.error("Error en login:", err);
        const backendMessage = err?.error?.message;
        if (backendMessage) {
          this.dialogService.alert(backendMessage);
        } else {
          this.dialogService.alert('Credenciales incorrectas.');
        }
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }
}