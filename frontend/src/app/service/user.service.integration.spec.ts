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

  function registerUser(userData: any): Promise<void> {
    return new Promise((resolve, reject) => {
      loginService.register(userData).subscribe({
        next: () => resolve(),
        error: (err) => reject('Register failed: ' + err.message)
      });
    });
  }

  async function createAndLoginTempUser(base: string, password: string): Promise<{ username: string; password: string }> {
    const suffix = Date.now().toString().slice(-6);
    const username = `${base}${suffix}`;

    const userData = {
      name: username,
      password,
      gmail: `${username.toLowerCase()}@example.com`,
      direction: 'Calle Test 1',
      nif: '12345678A'
    };

    await registerUser(userData);
    await loginAs(username, password);
    return { username, password };
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

  it('should update user profile after login', (done) => {
    createAndLoginTempUser('UpdateUser', 'password123').then(() => {

      service.getCurrentUser().subscribe({
        next: (user) => {

          const updatePayload = {
            name: 'VictorUpdated',
            gmail: 'victor.updated@example.com',
            direction: 'Nueva dirección 123',
            nif: '99999999Z'
          };

          service.updateUser(user.id, updatePayload).subscribe({
            next: (updated) => {
              expect(updated).toBeTruthy();
              expect(updated.name).toBe('VictorUpdated');
              expect(updated.gmail).toBe('victor.updated@example.com');
              expect(updated.direction).toBe('Nueva dirección 123');
              expect(updated.nif).toBe('99999999Z');
              done();
            },
            error: (err) => {
              fail('Failed to update user: ' + err.message);
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

  it('should update user image after login', (done) => {
    createAndLoginTempUser('ImageUser', 'password123').then(() => {

      service.getCurrentUser().subscribe({
        next: (user) => {

          const fakeFile = new File(['fakeImageData'], 'avatar.png', { type: 'image/png' });

          service.updateUserImage(user.id, fakeFile).subscribe({
            next: () => {

              // Verify image was updated by fetching it again
              service.getUserImage(user.id).subscribe({
                next: (blob) => {
                  expect(blob).toBeTruthy();
                  expect(blob.size).toBeGreaterThan(0);
                  done();
                },
                error: (err) => {
                  fail('Failed to fetch updated image: ' + err.message);
                  done();
                }
              });

            },
            error: (err) => {
              fail('Failed to update user image: ' + err.message);
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

  it('should change password successfully', (done) => {
    createAndLoginTempUser('PassUser', 'password123').then(({ username }) => {

      service.getCurrentUser().subscribe({
        next: (user) => {

          const payload = {
            currentPassword: 'password123',
            newPassword: 'newPassword123',
            confirmPassword: 'newPassword123'
          };

          service.changePassword(user.id, payload).subscribe({
            next: () => {

              // Try logging in with the new password
              loginAs(username, 'newPassword123').then(() => {
                done();
              }).catch(err => {
                fail('Login with new password failed: ' + err);
                done();
              });

            },
            error: (err) => {
              fail('Failed to change password: ' + err.message);
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

  it('should return error when current password is incorrect', (done) => {
    createAndLoginTempUser('WrongPass', 'password123').then(() => {

      service.getCurrentUser().subscribe({
        next: (user) => {

          const payload = {
            currentPassword: 'wrongPassword',
            newPassword: 'newPassword123',
            confirmPassword: 'newPassword123'
          };

          service.changePassword(user.id, payload).subscribe({
            next: () => {
              fail('Password change should not succeed with wrong current password');
              done();
            },
            error: (err) => {
              expect(err.status).toBe(400);
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



});
