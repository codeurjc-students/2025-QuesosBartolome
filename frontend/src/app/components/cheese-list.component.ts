import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NgbDropdownModule, NgbTooltipModule } from '@ng-bootstrap/ng-bootstrap';
import { CheeseService } from '../service/cheese.service';
import { CheeseDTO } from '../dto/cheese.dto';

@Component({
  selector: 'app-cheese-list',
  standalone: true,
  imports: [
    CommonModule,
    NgbDropdownModule,
    NgbTooltipModule
  ],
  templateUrl: './cheese-list.component.html',
  styleUrls: ['./cheese-list.component.css']
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

  openNewCheeseModal(): void {
    alert('Abrir modal para nuevo queso');
  }
}
