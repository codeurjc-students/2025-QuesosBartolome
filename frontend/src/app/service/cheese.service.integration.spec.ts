import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { CheeseService } from './cheese.service';
import { CheeseDTO } from '../dto/cheese.dto';
import { HttpClientModule } from '@angular/common/http';

describe('CheeseService (integration)', () => {
  let service: CheeseService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientModule], 
      providers: [CheeseService]
    });

    service = TestBed.inject(CheeseService);
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
});
