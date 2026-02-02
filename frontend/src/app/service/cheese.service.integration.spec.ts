import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CheeseService } from './cheese.service';
import { CheeseDTO } from '../dto/cheese.dto';
import { HttpClientModule } from '@angular/common/http';
import { LoginService } from './login.service';

describe('CheeseService (integration)', () => {
  let service: CheeseService;
  let loginService: LoginService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [CheeseService, LoginService]
    });

    service = TestBed.inject(CheeseService);
    loginService = TestBed.inject(LoginService);
  });

  it('should fetch cheeses from real API', (done) => {
    service.getAllCheeses().subscribe({
      next: (cheeses: CheeseDTO[]) => {

        expect(cheeses.length).toBeGreaterThan(0);

        const names = cheeses.map(c => c.name);
        expect(names).toContain('Semicurado');
        expect(names).toContain('Azul');

        done();
      },
      error: (err) => {
        fail(err.message);
        done();
      }
    });
  });
  it('should fetch cheese by id from real API', (done) => {
    service.getCheeseById(1).subscribe({
      next: (cheese: CheeseDTO) => {
        expect(cheese).toBeTruthy();
        expect(cheese.id).toEqual(1);
        expect(cheese.name).toEqual('Semicurado');
        done();
      },
      error: (err) => {
        fail(err.message);
        done();
      }
    });
  });

  it('should fetch cheese image from real API', (done) => {
    service.getCheeseImage(1).subscribe({
      next: (blob: Blob) => {
        expect(blob).toBeTruthy();
        expect(blob.size).toBeGreaterThan(0);
        expect(blob.type).toEqual('image/png');
        done();
      },
      error: (err) => {
        fail(err.message);
        done();
      }
    });
  });

  it('should create a new cheese in the real API', (done) => {

    loginService.login('German', 'password123').subscribe({
      next: () => {

        const newCheese: CheeseDTO = {
          name: 'TestCheese',
          price: 9.99,
          description: 'Cheese created in integration test',
          manufactureDate: '2024-01-24',
          expirationDate: '2025-01-25',
          type: 'Cremoso',
          boxes: []
        };

        service.createCheese(newCheese).subscribe({
          next: (created: CheeseDTO) => {
            expect(created).toBeTruthy();
            expect(created.id).toBeGreaterThan(0);
            expect(created.name).toBe('TestCheese');
            done();
          },
          error: (err) => {
            fail(err.message);
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


  it('should upload an image for a cheese in the real API', (done) => {

    loginService.login('German', 'password123').subscribe({
      next: () => {

        const newCheese: CheeseDTO = {
          name: 'CheeseWithImage',
          price: 10,
          description: 'Cheese for image upload test',
          manufactureDate: '2024-01-24',
          expirationDate: '2025-01-25',
          type: 'Cremoso',
          boxes: []
        };

        service.createCheese(newCheese).subscribe({
          next: (created: CheeseDTO) => {

            expect(created.id).toBeTruthy();

            const fakeFile = new File(
              [new Blob(['fake image content'], { type: 'image/png' })],
              'test.png',
              { type: 'image/png' }
            );

            service.uploadCheeseImage(created.id!, fakeFile).subscribe({
              next: (response) => {
                expect(response).toBeNull();
                done();
              },
              error: (err) => {
                fail(err.message);
                done();
              }
            });

          },
          error: (err) => {
            fail(err.message);
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

  it('should fail when creating an invalid cheese (400)', (done) => {

    loginService.login('German', 'password123').subscribe({
      next: () => {

        const invalidCheese: CheeseDTO = {
          name: '',
          price: -5,
          description: '',
          manufactureDate: '',
          expirationDate: '',
          type: '',
          boxes: []
        };

        service.createCheese(invalidCheese).subscribe({
          next: () => {
            fail('Expected API to return an error');
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


});
