import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../service/order.service';
import { OrderDTO } from '../../dto/order.dto';

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

  constructor(private orderService: OrderService) {}

  ngOnInit(): void {
    this.loadOrders();
  }

  loadOrders() {
    this.orderService.getAllOrders(this.currentPage, this.pageSize).subscribe({
      next: (data) => {
        this.orders = data.content;
        console.log('Pedidos cargados:', this.orders);
        console.log(data.content);
        console.log(data);
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando pedidos', err);
        this.loading = false;
      }
    });
  }

  nextPage() {
    this.currentPage++;
    this.loadOrders();
  }

  prevPage() {
    if (this.currentPage >= 1) {
      this.currentPage--;
      this.loadOrders();
    }
  }

  processOrder(orderId: number) {
    console.log('Procesar pedido:', orderId);
  }
}
