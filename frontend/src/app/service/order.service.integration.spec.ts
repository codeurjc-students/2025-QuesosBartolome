import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { OrderService } from './order.service';
import { CartService } from './cart.service';
import { LoginService } from './login.service';
import { OrderDTO } from '../dto/order.dto';
import { Page } from '../dto/page.dto';

describe('OrderService (integration with real login)', () => {

  let service: OrderService;
  let cartService: CartService;
  let loginService: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [OrderService, CartService, LoginService]
    });

    service = TestBed.inject(OrderService);
    cartService = TestBed.inject(CartService);
    loginService = TestBed.inject(LoginService);
  });

  it('should confirm order using real API after login', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {

        cartService.addCheeseToOrder(1, 1, 1).subscribe({
          next: () => {

            service.confirmOrder().subscribe({
              next: (order: OrderDTO) => {
                expect(order).toBeTruthy();
                expect(order.id).toBeDefined();
                expect(Array.isArray(order.items)).toBeTrue();
                done();
              },
              error: (err) => {
                fail('Failed to confirm order: ' + err.message);
                done();
              }
            });

          },
          error: (err) => {
            fail('Failed to add item before confirming: ' + err.message);
            done();
          }
        });

      },
      error: (err) => {
        fail('Login failed: ' + err.message);
        done();
      }
    });
  });

  it('should fail to confirm empty order after login', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {

        service.confirmOrder().subscribe({
          next: () => {
            fail('Expected backend to reject empty order');
            done();
          },
          error: (err) => {
            expect(err.status).toBe(400);
            done();
          }
        });

      },
      error: (err) => {
        fail('Login failed: ' + err.message);
        done();
      }
    });
  });

  it('should retrieve paginated orders after login', (done) => {
    loginService.login('German', 'password123').subscribe({ //Admin Login
      next: () => {

        service.getAllOrders(0, 10).subscribe({
          next: (page: Page<OrderDTO>) => {
            expect(page).toBeTruthy();
            expect(Array.isArray(page.content)).toBeTrue();
            expect(page.size).toBe(10);
            expect(page.number).toBe(0);
            done();
          },
          error: (err) => {
            fail('Failed to fetch orders: ' + err.message);
            done();
          }
        });

      },
      error: (err) => {
        fail('Login failed: ' + err.message);
        done();
      }
    });
  });

});
