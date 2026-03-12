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
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [UserService, LoginService]
    });

    service = TestBed.inject(UserService);
    loginService = TestBed.inject(LoginService);
  });

  function loginAs(username: string, password: string): Promise<void> {
    return new Promise((resolve, reject) => {
      loginService.login(username, password).subscribe({
        next: () => resolve(),
        error: (err) => reject('Login failed: ' + err.message)
      });
    });
  }

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
        // If logout fails, still test unauthenticated request
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
    loginAs('Victor', 'password123').then(() => {

      service.getCurrentUser().subscribe({
        next: (user: UserDTO) => {
          expect(user).toBeTruthy();
          expect(user.id).toBeGreaterThan(0);
          expect(user.name).toBeDefined();
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

    }).catch(err => {
      fail(err);
      done();
    });
  });

  it('should return user image as Blob after login', (done) => {
    loginAs('Victor', 'password123').then(() => {

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

    }).catch(err => {
      fail(err);
      done();
    });
  });

  it('should return user by ID after login', (done) => {
    loginAs('Victor', 'password123').then(() => {

      service.getUserById(1).subscribe({
        next: (user: UserDTO) => {
          expect(user).toBeTruthy();
          expect(user.id).toBe(1);
          expect(user.name).toBeDefined();
          expect(user.gmail).toContain('@');
          done();
        },
        error: (err) => {
          fail('Failed to get user by ID: ' + err.message);
          done();
        }
      });

    }).catch(err => {
      fail(err);
      done();
    });
  });

  it('should return a paginated list of users after login', (done) => {
    loginAs('German', 'password123').then(() => {

      service.getAllUsers(0, 10).subscribe({
        next: (page: Page<UserDTO>) => {
          expect(page).toBeTruthy();
          expect(Array.isArray(page.content)).toBeTrue();
          expect(page.number).toBe(0);
          expect(page.size).toBe(10);

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

    }).catch(err => {
      fail(err);
      done();
    });
  });

});
