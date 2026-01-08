import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { UserService } from './user.service';
import { LoginService } from './login.service';
import { UserDTO } from '../dto/user.dto';
import { Page } from '../dto/page.dto';

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

  it('should return user image as Blob after login', (done) => {
    loginService.login('Victor', 'password123').subscribe({
      next: () => {
        service.getCurrentUser().subscribe({
          next: (user: UserDTO) => {
            service.getUserImage(user.id).subscribe({
              next: (blob: Blob) => {
                expect(blob).toBeTruthy();
                expect(blob instanceof Blob).toBeTrue();
                done();
              },
              error: (err) => {
                fail('Failed to get user image: ' + err.message);
                done();
              }
            });
          },
          error: (err) => {
            fail('Failed to get current user: ' + err.message);
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

  it('should return a paginated list of users after login', (done) => {
    loginService.login('German', 'password123').subscribe({
      next: () => {
        // Get first page with 10 users
        service.getAllUsers(0, 10).subscribe({
          next: (page: Page<UserDTO>) => {
            expect(page).toBeTruthy();
            expect(page.content).toBeInstanceOf(Array);
            expect(page.totalElements).toBeGreaterThanOrEqual(0);
            if (page.content.length > 0) {
              const firstUser = page.content[0];
              expect(firstUser.id).toBeDefined();
              expect(firstUser.name).toBeDefined();
              expect(firstUser.gmail).toContain('@');
            }
            done();
          },
          error: (err) => {
            fail('Failed to get all users: ' + err.message);
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
