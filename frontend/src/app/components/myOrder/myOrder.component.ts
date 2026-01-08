import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { OrderService } from '../../service/order.service';
import { CartDTO } from '../../dto/cart.dto';
import { CartService } from '../../service/cart.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-myOrder',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './myOrder.component.html',
  styleUrls: ['./myOrder.component.css']
})
export class MyOrderComponent implements OnInit {

  order: CartDTO = {
    id: 0,
    user: { id: 0, name: '' },
    totalWeight: 0,
    totalPrice: 0,
    items: []
  };

  loading = true;

  constructor(private orderService: OrderService, private cartService: CartService, private router: Router) { }

  ngOnInit(): void {
    this.cartService.getMyCart().subscribe({
      next: (data) => {
        this.order = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error cargando pedido', err);
        this.loading = false;
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  goToEdit() {
    console.log('Ir a editar');
  }

  removeItem(itemId: number) {
    this.cartService.removeItemFromCart(itemId).subscribe({
      next: (order) => this.order = order,
      error: err => {
        console.error('Error eliminando item', err)
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }

  makeOrder() {
    this.orderService.confirmOrder().subscribe({
      next: () => {
        alert('Pedido realizado correctamente');
        this.ngOnInit();
      },
      error: err => {
        console.error('Error al hacer pedido', err)
        alert('Error al hacer el pedido');
        if (err.status >= 500) {
          this.router.navigate(['/error']);
        }
      }
    });
  }
}
