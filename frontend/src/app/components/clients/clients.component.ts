import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';
import { Router } from '@angular/router';

@Component({
    selector: 'app-clients',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './clients.component.html',
    styleUrls: ['./clients.component.css']
})
export class ClientsComponent implements OnInit {

    users: UserDTO[] = [];

    imageUrl: string | null = null;

    currentPage = 0;
    pageSize = 10;

    constructor(private userService: UserService, private router: Router) { }

    ngOnInit(): void {
        this.loadUsers();
    }

    loadUsers() {
        this.userService.getAllUsers(this.currentPage, this.pageSize).subscribe({
            next: (data) => {
                this.users = data.content;
                this.users.forEach(user => this.loadUserImage(user));
            },
            error: (err) => {
                console.error('Error cargando usuarios', err);
                if (err.status >= 500) {
                    this.router.navigate(['/error']);
                }
            }
        });
    }

    loadUserImage(user: UserDTO) {
        this.userService.getUserImage(user.id).subscribe({
            next: (blob) => {
                console.log('Blob de imagen recibido para el usuario', blob);
                if (blob && blob.size > 0) {
                    this.imageUrl = URL.createObjectURL(blob);
                } else {
                    this.imageUrl = null;
                }
            },
            error: () => {
                this.imageUrl = null;
            }
        });
    }

    nextPage() {
        this.currentPage++;
        this.loadUsers();
    }

    prevPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadUsers();
        }
    }

    banUser(userId: number) {
        console.log('Banear usuario:', userId);

    }
}
