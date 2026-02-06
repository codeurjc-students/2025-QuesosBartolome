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

  it('should edit an existing cheese in the real API', (done) => {

    loginService.login('German', 'password123').subscribe({
      next: () => {

        const updatedCheese: CheeseDTO = {
          id: 1,
          name: 'Semicurado Editado',
          price: 18.50,
          description: 'Queso semicurado actualizado',
          manufactureDate: '2024-01-01',
          expirationDate: '2025-06-01',
          type: 'Pasta prensada',
          boxes: [6.32, 5.87]
        };

        service.updateCheese(1, updatedCheese).subscribe({
          next: (edited: CheeseDTO) => {
            expect(edited).toBeTruthy();
            expect(edited.id).toBe(1);
            expect(edited.name).toBe('Semicurado Editado');
            expect(edited.price).toBe(18.50);

            // Restore original data to avoid affecting other tests
            const original: CheeseDTO = {
              id: 1,
              name: 'Semicurado',
              price: 17.50,
              description: 'Queso de pasta prensada madurado durante 21 días, con un sabor que comienza suave y cremoso pero se intensifica al final. Aromático sin resultar demasiado fuerte, ofrece un equilibrio agradable que lo hace fácil de disfrutar solo o acompañado.',
              manufactureDate: '2024-01-01',
              expirationDate: '2025-01-01',
              type: 'Pasta prensada',
              boxes: [6.32, 5.87, 5.82, 6.56, 5.98, 6.34, 6.41, 6.03, 5.79, 6.22]
            };

            service.updateCheese(1, original).subscribe({
              next: () => {
                done();
              },
              error: (err) => {
                fail('Failed to restore original: ' + err.message);
                done();
              }
            });
          },
          error: (err) => {
            fail('Edit failed: ' + err.message);
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

  it('should delete a cheese in the real API', (done) => {

    loginService.login('German', 'password123').subscribe({
      next: () => {

        // first create a cheese to delete
        const cheeseToDelete: CheeseDTO = {
          name: 'CheeseToDelete',
          price: 15,
          description: 'Este queso será eliminado',
          manufactureDate: '2024-01-24',
          expirationDate: '2025-01-25',
          type: 'Cremoso',
          boxes: []
        };

        service.createCheese(cheeseToDelete).subscribe({
          next: (created: CheeseDTO) => {
            expect(created.id).toBeTruthy();

            // Now delete the cheese
            service.deleteCheese(created.id!).subscribe({
              next: () => {
                // Verify it no longer exists
                service.getCheeseById(created.id!).subscribe({
                  next: () => {
                    fail('Cheese should have been deleted');
                    done();
                  },
                  error: (err) => {
                    expect(err.status).toBe(404);
                    done();
                  }
                });
              },
              error: (err) => {
                fail('Delete failed: ' + err.message);
                done();
              }
            });
          },
          error: (err) => {
            fail('Create failed: ' + err.message);
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
