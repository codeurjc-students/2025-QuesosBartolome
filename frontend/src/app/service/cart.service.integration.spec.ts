import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { CartService } from './cart.service';
import { LoginService } from './login.service';
import { CartDTO } from '../dto/cart.dto';

describe('CartService (integration with real login)', () => {

  let service: CartService;
  let loginService: LoginService;
  

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [CartService, LoginService]
    });

    service = TestBed.inject(CartService);
    loginService = TestBed.inject(LoginService);
  });

  it('should fetch my cart after real login', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {
        service.getMyCart().subscribe({
          next: (cart: CartDTO) => {
            expect(cart).toBeTruthy();
            expect(cart.items).toBeDefined();
            expect(Array.isArray(cart.items)).toBeTrue();
            done();
          },
          error: (err) => {
            fail('Failed to fetch cart after login: ' + err.message);
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

  it('should add cheese to cart after real login', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {
        service.addCheeseToOrder(1, 1, 1).subscribe({
          next: (cart: CartDTO) => {
            expect(cart).toBeTruthy();
            expect(cart.items.length).toBeGreaterThan(0);
            done();
          },
          error: (err) => {
            fail('Failed to add cheese after login: ' + err.message);
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

  it('should remove item from cart after real login', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {
        service.addCheeseToOrder(1, 1, 1).subscribe({
          next: (cartAfterAdd: CartDTO) => {
            const itemId = cartAfterAdd.items[0].id;

            service.removeItemFromCart(itemId).subscribe({
              next: (cartAfterRemove: CartDTO) => {
                const ids = cartAfterRemove.items.map(i => i.id);
                expect(ids).not.toContain(itemId);
                done();
              },
              error: (err) => {
                fail('Failed to remove item after login: ' + err.message);
                done();
              }
            });
          },
          error: (err) => {
            fail('Failed to add item before removing: ' + err.message);
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
