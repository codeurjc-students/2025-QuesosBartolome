import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { CheeseDetailsComponent } from './cheese-details.component';
import { CheeseService } from '../../service/cheese.service';
import { UserService } from '../../service/user.service';
import { ActivatedRoute, Router } from '@angular/router';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { CheeseDTO } from '../../dto/cheese.dto';
import { UserDTO } from '../../dto/user.dto';
import { CartService } from '../../service/cart.service';
import { CartDTO } from '../../dto/cart.dto';

describe('CheeseDetailsComponent (unit)', () => {

  let component: CheeseDetailsComponent;
  let fixture: ComponentFixture<CheeseDetailsComponent>;

  let mockCheeseService: jasmine.SpyObj<CheeseService>;
  let mockUserService: jasmine.SpyObj<UserService>;
  let mockCartService: jasmine.SpyObj<CartService>;
  let mockRoute: any;
  let mockRouter: jasmine.SpyObj<Router>;

  const CHEESE_ID = 999; 

  const baseCheese: CheeseDTO = {
    id: CHEESE_ID,
    name: 'Semicurado',
    price: 10,
    description: 'Queso delicioso',
    type: 'Curado',
    manufactureDate: '2024-01-01',
    expirationDate: '2025-01-01',
    boxes: [6.01, 7.02]
  };

  const baseUser: UserDTO = {
    id: 1,
    name: 'Pepito',
    password: '1234',
    gmail: 'pepito@gmail.com',
    direction: 'Calle Falsa 123',
    nif: '12345678A',
    rols: ['USER']
  };

  beforeEach(async () => {

    mockCheeseService = jasmine.createSpyObj('CheeseService', [
      'getCheeseById',
      'getCheeseImage',
      'deleteCheese'
    ]);

    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockCartService = jasmine.createSpyObj('CartService', ['addCheeseToOrder']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    mockRoute = { snapshot: { paramMap: new Map([['id', CHEESE_ID.toString()]]) } };

    mockCheeseService.getCheeseById.and.returnValue(of(baseCheese));
    mockCheeseService.getCheeseImage.and.returnValue(of(new Blob(['fake'], { type: 'image/png' })));
    mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));

    await TestBed.configureTestingModule({
      imports: [CheeseDetailsComponent],
      providers: [
        { provide: CheeseService, useValue: mockCheeseService },
        { provide: UserService, useValue: mockUserService },
        { provide: CartService, useValue: mockCartService },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should render cheese details', () => {
    const debug: DebugElement = fixture.debugElement;

    expect(debug.query(By.css('.cheese-title')).nativeElement.textContent.trim())
      .toBe('Semicurado');

    expect(debug.query(By.css('.cheese-price')).nativeElement.textContent.trim())
      .toContain('10');

    expect(debug.query(By.css('.cheese-description')).nativeElement.textContent.trim())
      .toBe('Queso delicioso');
  });

  it('should load cheese image from service', () => {
    expect(mockCheeseService.getCheeseImage).toHaveBeenCalledWith(CHEESE_ID);
    expect(component.imageUrl).toContain('blob:');
  });

  it('should fallback to default image when blob is empty', () => {
    mockCheeseService.getCheeseImage.and.returnValue(of(new Blob([], { type: 'image/png' })));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.imageUrl).toBe('assets/cheese-default.png');
  });

  it('should fallback to default image when loadCheeseImage errors', () => {
    mockCheeseService.getCheeseImage.and.returnValue(
      throwError(() => new Error('fail'))
    );

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.imageUrl).toBe('assets/cheese-default.png');
  });

  it('should set isLoggedIn=true when user service returns a user', () => {
    mockUserService.getCurrentUser.and.returnValue(of(baseUser));

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
    mockUserService.getCurrentUser.and.returnValue(of(baseUser));
    mockCheeseService.getCheeseById.and.returnValue(of(baseCheese));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const stockInfo = fixture.debugElement.query(By.css('.stock-info')).nativeElement.textContent.trim();
    expect(stockInfo).toContain('2 disponibles');
  });

  it('should show "Sin stock" when cajasDisponibles === 0', () => {
    mockUserService.getCurrentUser.and.returnValue(of(baseUser));

    const noStockCheese = { ...baseCheese, boxes: [] };
    mockCheeseService.getCheeseById.and.returnValue(of(noStockCheese));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const stockInfo = fixture.debugElement.query(By.css('.stock-info-out')).nativeElement.textContent.trim();
    expect(stockInfo).toBe('Sin stock');
  });

  it('should show loading state when cheese is not yet loaded', () => {
    mockCheeseService.getCheeseById.and.returnValue(
      throwError(() => new Error('not found'))
    );

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    const loading = fixture.debugElement.query(By.css('.loading')).nativeElement.textContent.trim();
    expect(loading).toBe('Cargando queso...');
  });

  it('should show edit button for ADMIN and hide cajas input', () => {
    const mockAdmin: UserDTO = { ...baseUser, rols: ['ADMIN'] };
    mockUserService.getCurrentUser.and.returnValue(of(mockAdmin));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('.edit-btn'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('.cajas-input'))).toBeNull();
  });

  it('should alert when boxesValue is empty', () => {
    spyOn(window, 'alert');

    component.addToOrder("");

    expect(window.alert).toHaveBeenCalledWith('Debes introducir una cantidad');
  });

  it('should alert when boxesValue is invalid', () => {
    spyOn(window, 'alert');

    component.cheese = { ...baseCheese };

    component.addToOrder("0");

    expect(window.alert).toHaveBeenCalledWith('Ingrese una cantidad correcta');
  });

  it('should alert when user is not logged in', () => {
    spyOn(window, 'alert');

    component.currentUser = null;
    component.cheese = { ...baseCheese };

    component.addToOrder("1");

    expect(window.alert).toHaveBeenCalledWith('Debes estar logueado');
  });

  it('should alert when addCheeseToOrder fails', () => {
    spyOn(window, 'alert');

    component.currentUser = baseUser;
    component.cheese = { ...baseCheese };

    mockCartService.addCheeseToOrder.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.addToOrder("1");

    expect(window.alert).toHaveBeenCalledWith('Error al añadir el producto');
  });

  it('should add item to order when valid boxes are provided', fakeAsync(() => {
  spyOn(window, 'alert');

  mockUserService.getCurrentUser.and.returnValue(of(baseUser));
  mockCheeseService.getCheeseById.and.returnValue(of(baseCheese));
  mockCheeseService.getCheeseImage.and.returnValue(of(new Blob(['fake'])));

  const mockCart: CartDTO = {
    id: 1,
    user: { id: 1, name: "Victor" },
    totalWeight: 12.38,
    totalPrice: 216.65,
    items: [
      {
        id: 4,
        cheeseId: 1,
        cheeseName: "Semicurado",
        cheesePrice: 17.5,
        boxes: [5.82],
        weight: 5.82,
        totalPrice: 101.85
      }
    ]
  };

  mockCartService.addCheeseToOrder.and.returnValue(of(mockCart));

  fixture = TestBed.createComponent(CheeseDetailsComponent);
  component = fixture.componentInstance;
  fixture.detectChanges();

  component.addToOrder("1");
  tick();

  expect(mockCartService.addCheeseToOrder).toHaveBeenCalledWith(1, 999, 1);
  expect(window.alert).toHaveBeenCalledWith('Producto añadido al pedido');
}));

  it('should delete cheese successfully', fakeAsync(() => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');

    mockUserService.getCurrentUser.and.returnValue(of(baseUser));
    mockCheeseService.getCheeseById.and.returnValue(of(baseCheese));
    mockCheeseService.getCheeseImage.and.returnValue(of(new Blob(['fake'])));
    mockCheeseService.deleteCheese.and.returnValue(of(void 0));

    fixture = TestBed.createComponent(CheeseDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.deleteCheese();
    tick();

    expect(window.alert).toHaveBeenCalledWith('Queso eliminado correctamente');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/cheeses']);
  }));

  it('should alert when deleteCheese fails', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');

    component.cheese = { ...baseCheese };

    mockCheeseService.deleteCheese.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.deleteCheese();

    expect(window.alert).toHaveBeenCalledWith('Error al eliminar el queso');
  });

  it('should navigate to edit page when editCheese is called', () => {
    component.cheese = { ...baseCheese };

    component.editCheese();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/cheeses', CHEESE_ID, 'edit']);
  });

});
