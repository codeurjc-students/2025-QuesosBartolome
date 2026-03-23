import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../service/order.service';
import { OrderDTO } from '../../dto/order.dto';
import { Router } from '@angular/router';

@Component({
  selector: 'app-orders',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './orders.component.html',
  styleUrls: ['./orders.component.css']
})
export class OrdersComponent implements OnInit {

  orders: OrderDTO[] = [];
  loading = true;

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  constructor(private orderService: OrderService, private router: Router) { }

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders() {
    this.orderService.getAllOrders(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.orders = data.content;
        this.totalPages = data.totalPages;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando pedidos', err);
        this.loading = false;
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  nextPage() {
    if (this.currentPage < this.totalPages - 1) {
      this.currentPage++;
      this.loadOrders();
    }
  }

  prevPage() {
    if (this.currentPage > 0) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  processOrder(orderId: number) {
    this.router.navigate(['/orders', orderId, 'preview']);
  }
}
