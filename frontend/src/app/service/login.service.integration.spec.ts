import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';
import { LoginService } from './login.service';

describe('LoginService (integration)', () => {
  let service: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule], 
      providers: [LoginService]
    });

    service = TestBed.inject(LoginService);
  });

  it('should login successfully with valid credentials', (done) => {
    service.login('Victor', 'password123').subscribe({
      next: (response) => {

        expect(response.status).toBe('SUCCESS');
        expect(response.message).toContain('Auth successful');
        done();
      },
      error: (err) => {
        fail('Login failed: ' + err.message);
        done();
      }
    });
  });

  it('should register a new user successfully', (done) => {
    const userData = {
      name: 'JuanTest',
      password: 'password123',
      gmail: 'juantest@example.com',
      direction: 'Calle Falsa 123',
      nif: '87654321B'
    };

    service.register(userData).subscribe({
      next: (response) => {
        expect(response.message).toContain('User registered successfully');
        done();
      },
      error: (err) => {
        fail('Register failed: ' + (err.error?.error || err.message));
        done();
      }
    });
  });

  it('should fail login with invalid credentials', (done) => {
    service.login('usuario_incorrecto', 'wrongpass').subscribe({
      next: () => {
        fail('Login should not succeed with invalid credentials');
        done();
      },
      error: (err) => {
        expect(err.status).toBe(401); 
        done();
      }
    });
  });

  it('should fail to register with missing fields', (done) => {
    const invalidUserData = {
        name: '', 
        password: 'password123',
        gmail: 'juanfail@example.com',
        direction: 'Calle Falsa 123',
        nif: '12345678A'
    };

    service.register(invalidUserData).subscribe({
        next: () => {
        fail('Register should not succeed with missing fields');
        done();
        },
        error: (err) => {
        expect(err.status).toBe(400);
        expect(err.error?.error).toBe('Missing or blank fields');
        done();
        }
    });
    });
});
