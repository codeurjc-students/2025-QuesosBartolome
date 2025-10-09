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
        console.log(cheeses);
        
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
});
