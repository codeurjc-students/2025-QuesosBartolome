import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CheeseService } from '../service/cheese.service';
import { CheeseDTO } from '../dto/cheese.dto';

@Component({
  selector: 'app-cheese-list',
  imports: [CommonModule],
  templateUrl: './cheese-list.component.html'
})
export class CheeseListComponent implements OnInit {

  cheeses: CheeseDTO[] = [];

  constructor(private cheeseService: CheeseService) {}

  ngOnInit(): void {
    this.cheeseService.getAllCheeses().subscribe({
      next: (data) => {
        this.cheeses = data;
        console.log('Cheeses loaded:', this.cheeses);
      },
      error: (err) => console.error('Error loading cheeses', err)
    });
  }
}