import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../service/order.service';
import { OrderDTO } from '../../dto/order.dto';
import { Router } from '@angular/router';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';

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
  currentUser: UserDTO | null = null;

  currentPage = 0;
  pageSize = 10;
  totalPages = 0;

  constructor(private orderService: OrderService, private userService: UserService, private router: Router) { }

  ngOnInit(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.loadOrders();
      },
      error: () => {
        this.currentUser = null;
        this.loadOrders();
      }
    });
  }

  loadOrders() {
    if (!this.isAdmin() && !this.currentUser?.id) {
      this.router.navigate(['/auth/login']);
      return;
    }

    const request = this.isAdmin()
      ? this.orderService.getAllOrders(this.currentPage, this.pageSize)
      : this.userService.getMyOrders(this.currentUser!.id, this.currentPage, this.pageSize);

    request.subscribe({
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

  onOrderAction(order: OrderDTO, event?: Event): void {
    event?.stopPropagation();
    this.processOrder(order.id);
  }

  isAdmin(): boolean {
    return this.currentUser?.rols?.includes('ADMIN') ?? false;
  }

  getOrderStatus(order: OrderDTO): string {
    return order.processed ? 'Procesado' : 'Pendiente';
  }

  getOrderStatusClass(order: OrderDTO): string {
    return order.processed ? 'status-processed' : 'status-pending';
  }
}
