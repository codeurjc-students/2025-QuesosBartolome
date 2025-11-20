import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from './user.service';
import { LoginService } from './login.service';
import { UserDTO } from '../dto/user.dto';

describe('UserService (integration with real login)', () => {
  let service: UserService;
  let loginService: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [UserService, LoginService]
    });

    service = TestBed.inject(UserService);
    loginService = TestBed.inject(LoginService);
  });

  it('should return 401 when user is not authenticated', (done) => {
    loginService.logout().subscribe({
      next: () => {
        service.getCurrentUser().subscribe({
          next: () => {
            fail('Request should not succeed without authentication');
            done();
          },
          error: (err) => {
            expect(err.status).toBe(401);
            done();
          }
        });
      },
      error: () => {
        service.getCurrentUser().subscribe({
          next: () => {
            fail('Request should not succeed without authentication');
            done();
          },
          error: (err) => {
            expect(err.status).toBe(401);
            done();
          }
        });
      }
    });
  });

  it('should return current user after real login', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {
        service.getCurrentUser().subscribe({
          next: (user: UserDTO) => {
            expect(user).toBeTruthy();
            expect(user.id).toBeGreaterThan(0);
            expect(user.name).toBeDefined();
            expect(user.password).toBeDefined();
            expect(user.gmail).toContain('@');
            expect(user.direction).toBeDefined();
            expect(user.nif).toBeDefined();
            expect(user.image).toBeDefined();
            done();
          },
          error: (err) => {
            fail('Failed to get current user after login: ' + err.message);
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
