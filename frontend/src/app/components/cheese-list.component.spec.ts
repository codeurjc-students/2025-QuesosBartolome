import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { CheeseListComponent } from './cheese-list.component';
import { CheeseService } from '../service/cheese.service'; // 👈 cambia al path correcto
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

describe('CheeseListComponent (unit)', () => {
  let component: CheeseListComponent;
  let fixture: ComponentFixture<CheeseListComponent>;
  let mockService: jasmine.SpyObj<CheeseService>;

  beforeEach(async () => {
    // 🔸 Creamos un mock del servicio
    mockService = jasmine.createSpyObj('CheeseService', ['getAllCheeses']);

    // 🔸 Simulamos la respuesta del servicio (observable con dos quesos)
    mockService.getAllCheeses.and.returnValue(of([
      { id: 1, name: 'Semicurado', price: 10, description: '', type: '', manufactureDate: '', expirationDate: '' },
      { id: 2, name: 'Azul', price: 12, description: '', type: '', manufactureDate: '', expirationDate: '' }
    ]));

    
    await TestBed.configureTestingModule({
      imports: [CheeseListComponent], 
      providers: [{ provide: CheeseService, useValue: mockService }]
    }).compileComponents();

    
    fixture = TestBed.createComponent(CheeseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); 
  });

  it('should render Semicurado and Azul', () => {
    const debug: DebugElement = fixture.debugElement;

    const names = debug
      .queryAll(By.css('.cheese-name'))
      .map(de => de.nativeElement.textContent.trim());

    expect(names).toContain('Semicurado');
    expect(names).toContain('Azul');
    expect(names.length).toBe(2); // opcional: asegura que solo hay 2 quesos
  });
});