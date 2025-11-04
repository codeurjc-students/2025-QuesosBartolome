import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { CheeseListComponent } from './cheese-list.component';
import { CheeseService } from '../service/cheese.service';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

describe('CheeseListComponent (unit)', () => {
  let component: CheeseListComponent;
  let fixture: ComponentFixture<CheeseListComponent>;
  let mockService: jasmine.SpyObj<CheeseService>;

  beforeEach(async () => {

    mockService = jasmine.createSpyObj('CheeseService', ['getAllCheeses']);


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

  it('should render cheeses from service in the card grid', () => {
    const debug: DebugElement = fixture.debugElement;

    const cards = debug.queryAll(By.css('.card-grid .card:not(.add-card)'));
    expect(cards.length).toBe(2); 

    const names = cards.map(card =>
      card.query(By.css('.card-body p')).nativeElement.textContent.trim()
    );

    expect(names).toContain('Semicurado');
    expect(names).toContain('Azul');
  });

  it('should render the add new cheese card', () => {
    const addCard = fixture.debugElement.query(By.css('.card.add-card'));
    expect(addCard).toBeTruthy();

    const text = addCard.nativeElement.textContent;
    expect(text).toContain('Nuevo Queso');
  });
});
