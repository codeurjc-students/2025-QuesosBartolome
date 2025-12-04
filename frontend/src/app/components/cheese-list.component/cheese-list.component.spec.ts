import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CheeseListComponent } from './cheese-list.component';
import { CheeseService } from '../../service/cheese.service';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { UserService } from '../../service/user.service';
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';
import { UserDTO } from '../../dto/user.dto';

describe('CheeseListComponent (unit)', () => {
  let component: CheeseListComponent;
  let fixture: ComponentFixture<CheeseListComponent>;

  let mockCheeseService: jasmine.SpyObj<CheeseService>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockLoginService: jasmine.SpyObj<LoginService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockCheeseService = jasmine.createSpyObj('CheeseService', ['getAllCheeses']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockLoginService = jasmine.createSpyObj('LoginService', ['logout']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    mockCheeseService.getAllCheeses.and.returnValue(of([
      { id: 1, name: 'Semicurado', price: 10, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [] },
      { id: 2, name: 'Azul', price: 12, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [] }
    ]));

    // Default: not logged in
    mockUserService.getCurrentUser.and.returnValue(throwError(() => new Error()));

    await TestBed.configureTestingModule({
      imports: [CheeseListComponent],
      providers: [
        { provide: CheeseService, useValue: mockCheeseService },
        { provide: UserService, useValue: mockUserService },
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter }
      ]
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

  it('should set isLoggedIn=true when getCurrentUser() returns a user', () => {
    const mockUser: UserDTO = {
      id: 1,
      name: 'Pepito',
      password: '1234',
      gmail: 'pepito@gmail.com',
      direction: 'Calle Falsa 123',
      nif: '12345678A',
      rols: ['USER']
    };

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));

    fixture = TestBed.createComponent(CheeseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockUserService.getCurrentUser).toHaveBeenCalled();
    expect(component.isLoggedIn).toBeTrue();
    expect(component.currentUser?.name).toBe('Pepito');
  });


  it('should set isLoggedIn=false when getCurrentUser() fails (401)', () => {
    mockUserService.getCurrentUser.and.returnValue(
      throwError(() => ({ status: 401 }))
    );

    fixture = TestBed.createComponent(CheeseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(mockUserService.getCurrentUser).toHaveBeenCalled();
    expect(component.isLoggedIn).toBeFalse();
    expect(component.currentUser).toBeNull();
  });


  it('should logout and navigate to home', () => {
    mockLoginService.logout.and.returnValue(of(true));

    component.logout();

    expect(mockLoginService.logout).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
