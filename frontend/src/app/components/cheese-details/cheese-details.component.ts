import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute } from '@angular/router';
import { CheeseService } from '../../service/cheese.service';
import { CheeseDTO } from '../../dto/cheese.dto';
import { UserDTO } from '../../dto/user.dto';
import { UserService } from '../../service/user.service';

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
    private userService: UserService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.cheeseService.getCheeseById(id).subscribe({
      next: (data)=>{ 
        this.cheese = data; 
        this.loadCheeseImage(data.id);
      },
      error: err => console.error('Error obteniendo queso', err)
    });

    // Check if user is logged in
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isLoggedIn = true;
      },
      error: () => {
        // If token is invalid or request fails, consider user as logged out
        this.currentUser = null;
        this.isLoggedIn = false;
      }
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

  get cajasDisponibles(): number {
    return this.cheese?.boxes?.length ?? 0;
  }

  get isUser(): boolean {
  return this.currentUser?.rols?.includes('USER') ?? false;
  }

  get isAdmin(): boolean {
    return this.currentUser?.rols?.includes('ADMIN') ?? false;
  }

}