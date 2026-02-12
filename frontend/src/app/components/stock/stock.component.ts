import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CheeseService } from '../../service/cheese.service';
import { CheeseDTO } from '../../dto/cheese.dto';
import { Router } from '@angular/router';

interface CheeseStock {
  cheese: CheeseDTO;
  imageUrl: string | null;
  currentBoxPage: number;
  newBoxValue: number | null;
}

@Component({
  selector: 'app-stock',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './stock.component.html',
  styleUrls: ['./stock.component.css']
})
export class StockComponent implements OnInit {

  cheeses: CheeseStock[] = [];
  loading = true;

  currentCheesePage = 0;
  cheesePageSize = 3;
  totalCheeses = 0;

  boxPageSize = 5;

  constructor(
    private cheeseService: CheeseService,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.loadCheeses();
  }

  loadCheeses() {
    this.loading = true;
    this.cheeseService.getAllCheeses().subscribe({
      next: (data) => {
        this.totalCheeses = data.length;
        const startIndex = this.currentCheesePage * this.cheesePageSize;
        const endIndex = startIndex + this.cheesePageSize;
        const pageData = data.slice(startIndex, endIndex);

        this.cheeses = pageData.map(cheese => ({
          cheese: cheese,
          imageUrl: null,
          currentBoxPage: 0,
          newBoxValue: null
        }));

        this.cheeses.forEach(item => this.loadCheeseImage(item));
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando quesos', err);
        this.loading = false;
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  loadCheeseImage(cheeseStock: CheeseStock) {
    if (!cheeseStock.cheese.id) return;

    this.cheeseService.getCheeseImage(cheeseStock.cheese.id).subscribe({
      next: (blob) => {
        if (blob && blob.size > 0) {
          cheeseStock.imageUrl = URL.createObjectURL(blob);
        }
      },
      error: () => {
        cheeseStock.imageUrl = null;
      }
    });
  }

  nextCheesePage() {
    const maxPage = Math.ceil(this.totalCheeses / this.cheesePageSize) - 1;
    if (this.currentCheesePage < maxPage) {
      this.currentCheesePage++;
      this.loadCheeses();
    }
  }

  prevCheesePage() {
    if (this.currentCheesePage > 0) {
      this.currentCheesePage--;
      this.loadCheeses();
    }
  }

  isNextCheesePageDisabled(): boolean {
    const maxPage = Math.ceil(this.totalCheeses / this.cheesePageSize) - 1;
    return this.currentCheesePage >= maxPage;
  }

  isPrevCheesePageDisabled(): boolean {
    return this.currentCheesePage === 0;
  }

  getPagedBoxes(cheeseStock: CheeseStock): number[] {
    const boxes = cheeseStock.cheese.boxes || [];
    const startIndex = cheeseStock.currentBoxPage * this.boxPageSize;
    const endIndex = startIndex + this.boxPageSize;
    return boxes.slice(startIndex, endIndex);
  }

  nextBoxPage(cheeseStock: CheeseStock) {
    const boxes = cheeseStock.cheese.boxes || [];
    const maxPage = Math.ceil(boxes.length / this.boxPageSize) - 1;
    if (cheeseStock.currentBoxPage < maxPage) {
      cheeseStock.currentBoxPage++;
    }
  }

  prevBoxPage(cheeseStock: CheeseStock) {
    if (cheeseStock.currentBoxPage > 0) {
      cheeseStock.currentBoxPage--;
    }
  }

  isNextBoxPageDisabled(cheeseStock: CheeseStock): boolean {
    const boxes = cheeseStock.cheese.boxes || [];
    const maxPage = Math.ceil(boxes.length / this.boxPageSize) - 1;
    return cheeseStock.currentBoxPage >= maxPage || boxes.length === 0;
  }

  isPrevBoxPageDisabled(cheeseStock: CheeseStock): boolean {
    return cheeseStock.currentBoxPage === 0;
  }

  getCurrentBoxPageNumber(cheeseStock: CheeseStock): number {
    return cheeseStock.currentBoxPage + 1;
  }

  addBox(cheeseStock: CheeseStock) {
    if (!cheeseStock.newBoxValue || cheeseStock.newBoxValue <= 0 || !cheeseStock.cheese.id) {
      return;
    }

    this.cheeseService.addBox(cheeseStock.cheese.id, cheeseStock.newBoxValue).subscribe({
      next: (updatedCheese) => {
        cheeseStock.cheese.boxes = updatedCheese.boxes;
        cheeseStock.newBoxValue = null;
        const maxPage = Math.ceil(cheeseStock.cheese.boxes.length / this.boxPageSize) - 1;
        if (cheeseStock.currentBoxPage > maxPage) {
          cheeseStock.currentBoxPage = Math.max(0, maxPage);
        }
      },
      error: (err) => {
        console.error('Error añadiendo caja', err);
      }
    });
  }

  removeBox(cheeseStock: CheeseStock, boxIndex: number) {
    if (!cheeseStock.cheese.id) return;

    const realIndex = cheeseStock.currentBoxPage * this.boxPageSize + boxIndex;

    this.cheeseService.removeBox(cheeseStock.cheese.id, realIndex).subscribe({
      next: (updatedCheese) => {
        cheeseStock.cheese.boxes = updatedCheese.boxes;
        const maxPage = Math.ceil(cheeseStock.cheese.boxes.length / this.boxPageSize) - 1;
        if (cheeseStock.currentBoxPage > maxPage && cheeseStock.currentBoxPage > 0) {
          cheeseStock.currentBoxPage--;
        }
      },
      error: (err) => {
        console.error('Error eliminando caja', err);
      }
    });
  }

}
