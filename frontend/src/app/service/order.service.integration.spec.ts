import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { OrderService } from './order.service';
import { CartService } from './cart.service';
import { LoginService } from './login.service';
import { OrderDTO } from '../dto/order.dto';
import { Page } from '../dto/page.dto';

describe('OrderService (integration)', () => {

  let service: OrderService;
  let cartService: CartService;
  let loginService: LoginService;

beforeEach(() => {
  TestBed.resetTestingModule();

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

  it('should retrieve order by id after creating order (admin access)', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {
        cartService.addCheeseToOrder(1, 1, 1).subscribe({
          next: () => {
            service.confirmOrder().subscribe({
              next: (createdOrder: OrderDTO) => {
                loginService.login('German', 'password123').subscribe({
                  next: () => {
                    service.getOrderById(createdOrder.id).subscribe({
                      next: (fetchedOrder: OrderDTO) => {
                        expect(fetchedOrder).toBeTruthy();
                        expect(fetchedOrder.id).toBe(createdOrder.id);
                        expect(Array.isArray(fetchedOrder.items)).toBeTrue();
                        done();
                      },
                      error: (err) => {
                        fail('Failed to fetch order by id: ' + err.message);
                        done();
                      }
                    });
                  },
                  error: (err) => {
                    fail('Admin login failed before fetching by id: ' + err.message);
                    done();
                  }
                });
              },
              error: (err) => {
                fail('Failed to confirm order before getOrderById: ' + err.message);
                done();
              }
            });
          },
          error: (err) => {
            fail('Failed to add item before getOrderById test: ' + err.message);
            done();
          }
        });
      },
      error: (err) => {
        fail('User login failed before getOrderById test: ' + err.message);
        done();
      }
    });
  });

  it('should reject order by id after creating order (admin access)', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {
        cartService.addCheeseToOrder(1, 1, 1).subscribe({
          next: () => {
            service.confirmOrder().subscribe({
              next: (createdOrder: OrderDTO) => {
                loginService.login('German', 'password123').subscribe({
                  next: () => {
                    service.rejectOrder(createdOrder.id).subscribe({
                      next: (rejectedOrder: OrderDTO) => {
                        expect(rejectedOrder).toBeTruthy();
                        expect(rejectedOrder.id).toBe(createdOrder.id);
                        expect(Array.isArray(rejectedOrder.items)).toBeTrue();
                        done();
                      },
                      error: (err) => {
                        fail('Failed to reject order by id: ' + err.message);
                        done();
                      }
                    });
                  },
                  error: (err) => {
                    fail('Admin login failed before rejectOrder: ' + err.message);
                    done();
                  }
                });
              },
              error: (err) => {
                fail('Failed to confirm order before rejectOrder: ' + err.message);
                done();
              }
            });
          },
          error: (err) => {
            fail('Failed to add item before rejectOrder test: ' + err.message);
            done();
          }
        });
      },
      error: (err) => {
        fail('User login failed before rejectOrder test: ' + err.message);
        done();
      }
    });
  });

});
