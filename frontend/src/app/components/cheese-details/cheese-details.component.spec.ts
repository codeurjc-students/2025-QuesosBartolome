import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CheeseDetailsComponent } from './cheese-details.component';
import { CheeseService } from '../../service/cheese.service';
import { UserService } from '../../service/user.service';
import { ActivatedRoute } from '@angular/router';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { CheeseDTO } from '../../dto/cheese.dto';
import { UserDTO } from '../../dto/user.dto';
import { CartService } from '../../service/cart.service';

describe('CheeseDetailsComponent (unit)', () => {
  let component: CheeseDetailsComponent;
  let fixture: ComponentFixture<CheeseDetailsComponent>;

  let mockCheeseService: jasmine.SpyObj<CheeseService>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockCartService: jasmine.SpyObj<CartService>;
  let mockRoute: any;

  beforeEach(async () => {
    mockCheeseService = jasmine.createSpyObj('CheeseService', ['getCheeseById', 'getCheeseImage']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockCartService = jasmine.createSpyObj('CartService', ['addCheeseToOrder']);
    mockRoute = { snapshot: { paramMap: new Map([['id', '1']]) } };

    const mockCheese: CheeseDTO = {
      id: 1,
      name: 'Semicurado',
      price: 10,
      description: 'Queso delicioso',
      type: 'Curado',
      manufactureDate: '2024-01-01',
      expirationDate: '2025-01-01',
      boxes: [6.01, 7.02] 
    };

    mockCheeseService.getCheeseById.and.returnValue(of(mockCheese));
    mockCheeseService.getCheeseImage.and.returnValue(of(new Blob(['fake'], { type: 'image/png' })));

    // Default: not logged in
    mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));

    await TestBed.configureTestingModule({
      imports: [CheeseDetailsComponent],
      providers: [
        { provide: CheeseService, useValue: mockCheeseService },
        { provide: UserService, useValue: mockUserService },
        { provide: CartService, useValue: mockCartService },
        { provide: ActivatedRoute, useValue: mockRoute }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render cheese details', () => {
    const debug: DebugElement = fixture.debugElement;

    const title = debug.query(By.css('.cheese-title')).nativeElement.textContent.trim();
    expect(title).toBe('Semicurado');

    const price = debug.query(By.css('.cheese-price')).nativeElement.textContent.trim();
    expect(price).toContain('10'); 

    const description = debug.query(By.css('.cheese-description')).nativeElement.textContent.trim();
    expect(description).toBe('Queso delicioso');
  });

  it('should load cheese image from service', () => {
    expect(mockCheeseService.getCheeseImage).toHaveBeenCalledWith(1);
    expect(component.imageUrl).toContain('blob:'); 
  });

  it('should fallback to default image when blob is empty', () => {
    mockCheeseService.getCheeseImage.and.returnValue(of(new Blob([], { type: 'image/png' })));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.imageUrl).toBe('assets/cheese-default.png');
  });

  it('should set isLoggedIn=true when user service returns a user', () => {
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

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.isLoggedIn).toBeTrue();
    expect(component.currentUser?.name).toBe('Pepito');
  });

  it('should set isLoggedIn=false when getCurrentUser fails', () => {
    mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.isLoggedIn).toBeFalse();
    expect(component.currentUser).toBeNull();
  });

  it('should show stock info when cajasDisponibles > 0', () => {
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

    const mockCheese: CheeseDTO = {
        id: 1,
        name: 'Semicurado',
        price: 10,
        description: 'Queso delicioso',
        type: 'Curado',
        manufactureDate: '2024-01-01',
        expirationDate: '2025-01-01',
        boxes: [6.01, 6.02] 
    };
    mockCheeseService.getCheeseById.and.returnValue(of(mockCheese));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const debug: DebugElement = fixture.debugElement;
    const stockInfo = debug.query(By.css('.stock-info')).nativeElement.textContent.trim();
    expect(stockInfo).toContain('2 disponibles');
    });

    it('should show "Sin stock" when cajasDisponibles === 0', () => {
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

    const mockCheese: CheeseDTO = {
        id: 2,
        name: 'Azul',
        price: 12,
        description: 'Otro queso',
        type: 'Azul',
        manufactureDate: '2024-02-01',
        expirationDate: '2025-02-01',
        boxes: [] // out of stock
    };
    mockCheeseService.getCheeseById.and.returnValue(of(mockCheese));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const debug: DebugElement = fixture.debugElement;
    const stockInfo = debug.query(By.css('.stock-info-out')).nativeElement.textContent.trim();
    expect(stockInfo).toBe('Sin stock');
    });

  it('should show loading state when cheese is not yet loaded', () => {
    // Simulate getCheeseById failure
    mockCheeseService.getCheeseById.and.returnValue(throwError(() => new Error('not found')));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const debug: DebugElement = fixture.debugElement;
    const loading = debug.query(By.css('.loading')).nativeElement.textContent.trim();
    expect(loading).toBe('Cargando queso...');
  });

  it('should show edit button for ADMIN and hide cajas input', () => {
    const mockAdmin: UserDTO = {
      id: 2,
      name: 'AdminUser',
      password: 'admin',
      gmail: 'admin@example.com',
      direction: 'Admin Street',
      nif: '87654321B',
      rols: ['ADMIN']
    };

    mockUserService.getCurrentUser.and.returnValue(of(mockAdmin));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const debug: DebugElement = fixture.debugElement;

    // Botón de editar debe existir
    const editButton = debug.query(By.css('.edit-btn'));
    expect(editButton).toBeTruthy();

    // Input de cajas NO debe existir
    const cajasInput = debug.query(By.css('.cajas-input'));
    expect(cajasInput).toBeNull();

    // Stock info NO debe existir
    const stockInfo = debug.query(By.css('.stock-info'));
    expect(stockInfo).toBeNull();
  });

});