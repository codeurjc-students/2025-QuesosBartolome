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

    imageUrls: Record<number, string> = {};

    currentPage = 0;
    pageSize = 10;
    totalPages = 0;

    constructor(private userService: UserService, private router: Router) { }

    ngOnInit(): void {
        this.loadUsers();
    }

    loadUsers() {
        this.userService.getAllUsers(this.currentPage, this.pageSize).subscribe({
            next: (data) => {
                this.users = data.content;
                this.totalPages = data.totalPages;
                this.imageUrls = {};
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
                if (blob && blob.size > 0) {
                    this.imageUrls[user.id] = URL.createObjectURL(blob);
                } else {
                    this.imageUrls[user.id] = 'assets/avatar-default.jpg';
                }
            },
            error: () => {
                this.imageUrls[user.id] = 'assets/avatar-default.jpg';
            }
        });
    }

    getUserImageUrl(userId: number): string {
        return this.imageUrls[userId] || 'assets/avatar-default.jpg';
    }

    nextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadUsers();
        }
    }

    prevPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadUsers();
        }
    }

    goToUserProfile(userId: number): void {
        this.router.navigate(['/user', userId]);
    }

    banUser(user: UserDTO) {
        const shouldBan = !user.banned;
        const action = shouldBan ? 'banear' : 'desbanear';

        if (!confirm(`¿Seguro que quieres ${action} a ${user.name}?`)) {
            return;
        }

        this.userService.toggleUserBan(user.id).subscribe({
            next: (updatedUser) => {
                user.banned = updatedUser.banned;
            },
            error: (err) => {
                alert('No se pudo actualizar el estado de baneo del usuario.');
                if (err.status >= 500) {
                    this.router.navigate(['/error']);
                }
            }
        });

    }
}
