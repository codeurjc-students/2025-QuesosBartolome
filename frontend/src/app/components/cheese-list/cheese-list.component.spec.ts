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

  const singlePageResponse = (content: any[]) => of({
    content,
    totalPages: 1,
    totalElements: content.length,
    size: 5,
    number: 0,
    first: true,
    last: true,
    numberOfElements: content.length
  } as any);

  beforeEach(async () => {
    mockCheeseService = jasmine.createSpyObj('CheeseService', ['getAllCheeses']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockLoginService = jasmine.createSpyObj('LoginService', ['logout']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    mockCheeseService.getAllCheeses.and.returnValue(singlePageResponse([
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

  it('should render the add new cheese card when user is ADMIN', () => {
    const adminUser: UserDTO = {
      id: 1,
      name: 'Admin',
      password: '1234',
      gmail: 'admin@gmail.com',
      direction: 'Calle Falsa 123',
      nif: '12345678A',
      rols: ['ADMIN'],
      banned: false
    };

    mockUserService.getCurrentUser.and.returnValue(of(adminUser));

    fixture = TestBed.createComponent(CheeseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

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
      rols: ['USER'],
      banned: false
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

  it('should navigate to /error on logout error with status >= 500', () => {
    mockLoginService.logout.and.returnValue(throwError(() => ({ status: 500 })));

    component.logout();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should not navigate to /error on logout error with status < 500', () => {
    mockLoginService.logout.and.returnValue(throwError(() => ({ status: 404 })));

    component.logout();

    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should filter cheeses by type when selectType is called', () => {
    mockCheeseService.getAllCheeses.and.returnValue(singlePageResponse([
      { id: 1, name: 'Semicurado', price: 10, description: '', type: 'Pasta prensada', manufactureDate: '', expirationDate: '', boxes: [] },
      { id: 2, name: 'Azul', price: 12, description: '', type: 'Maduración fúngica', manufactureDate: '', expirationDate: '', boxes: [] },
      { id: 3, name: 'Chevrett', price: 20, description: '', type: 'Cremoso', manufactureDate: '', expirationDate: '', boxes: [] }
    ]));

    fixture = TestBed.createComponent(CheeseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.selectType('Cremoso');

    expect(component.selectedType).toBe('Cremoso');
    expect(component.filteredCheeses.length).toBe(1);
    expect(component.filteredCheeses[0].name).toBe('Chevrett');
  });

  it('should show all cheeses when "Todos" filter is selected', () => {
    mockCheeseService.getAllCheeses.and.returnValue(singlePageResponse([
      { id: 1, name: 'Semicurado', price: 10, description: '', type: 'Pasta prensada', manufactureDate: '', expirationDate: '', boxes: [] },
      { id: 2, name: 'Azul', price: 12, description: '', type: 'Maduración fúngica', manufactureDate: '', expirationDate: '', boxes: [] },
      { id: 3, name: 'Chevrett', price: 20, description: '', type: 'Cremoso', manufactureDate: '', expirationDate: '', boxes: [] }
    ]));

    fixture = TestBed.createComponent(CheeseListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    component.selectType('Todos');

    expect(component.selectedType).toBe('Todos');
    expect(component.filteredCheeses.length).toBe(3);
  });

  it('should set totalPages to 1 when filter returns no results', () => {
    component.allCheeses = [
      { id: 1, name: 'Semicurado', price: 10, description: '', type: 'Pasta prensada', manufactureDate: '', expirationDate: '', boxes: [] }
    ];

    component.selectType('Cremoso');

    expect(component.filteredCheeses.length).toBe(0);
    expect(component.totalPages).toBe(1);
  });

  it('should load multiple pages recursively when last is false', () => {
    component.allCheeses = [];

    mockCheeseService.getAllCheeses.and.callFake((page: number) => {
      if (page === 0) {
        return of({
          content: [{ id: 1, name: 'A', price: 1, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [] }],
          last: false, totalPages: 2, totalElements: 2, size: 5, number: 0, first: true, numberOfElements: 1
        } as any);
      }
      return of({
        content: [{ id: 2, name: 'B', price: 2, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [] }],
        last: true, totalPages: 2, totalElements: 2, size: 5, number: 1, first: false, numberOfElements: 1
      } as any);
    });

    component.loadAllPages(0);

    expect(component.allCheeses.length).toBe(2);
    expect(mockCheeseService.getAllCheeses).toHaveBeenCalledWith(0, 5);
    expect(mockCheeseService.getAllCheeses).toHaveBeenCalledWith(1, 5);
  });

  it('should navigate to /error when loadAllPages fails with status >= 500', () => {
    mockCheeseService.getAllCheeses.and.returnValue(throwError(() => ({ status: 500 })));

    component.loadAllPages(0);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should not navigate on loadAllPages error with status < 500', () => {
    mockCheeseService.getAllCheeses.and.returnValue(throwError(() => ({ status: 404 })));

    component.loadAllPages(0);

    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should increment currentPage when nextPage is called and not at last page', () => {
    component.currentPage = 1;
    component.totalPages = 3;

    component.nextPage();

    expect(component.currentPage).toBe(2);
  });

  it('should not increment currentPage when nextPage is called at last page', () => {
    component.currentPage = 3;
    component.totalPages = 3;

    component.nextPage();

    expect(component.currentPage).toBe(3);
  });

  it('should decrement currentPage when previousPage is called and not at first page', () => {
    component.currentPage = 3;
    component.totalPages = 3;

    component.previousPage();

    expect(component.currentPage).toBe(2);
  });

  it('should not decrement currentPage when previousPage is called at first page', () => {
    component.currentPage = 1;
    component.totalPages = 3;

    component.previousPage();

    expect(component.currentPage).toBe(1);
  });

  it('should set currentPage when goToPage is called with a valid page', () => {
    component.totalPages = 5;

    component.goToPage(3);

    expect(component.currentPage).toBe(3);
  });

  it('should not set currentPage when goToPage is called with an invalid page', () => {
    component.currentPage = 2;
    component.totalPages = 5;

    component.goToPage(0);
    expect(component.currentPage).toBe(2);

    component.goToPage(6);
    expect(component.currentPage).toBe(2);
  });

  it('should return 0 for firstDisplayedIndex when filteredCheeses is empty', () => {
    component.filteredCheeses = [];

    expect(component.firstDisplayedIndex).toBe(0);
  });

  it('should return correct firstDisplayedIndex when filteredCheeses has items', () => {
    component.filteredCheeses = [
      { id: 1, name: 'A', price: 1, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [] },
      { id: 2, name: 'B', price: 2, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [] }
    ];
    component.currentPage = 1;
    component.itemsPerPage = 10;

    expect(component.firstDisplayedIndex).toBe(1);
  });

  it('should return correct page numbers array', () => {
    component.totalPages = 3;

    expect(component.getPageNumbers()).toEqual([1, 2, 3]);
  });

  it('isAdmin should return true when user has ADMIN role', () => {
    component.currentUser = { id: 1, name: 'Admin', password: '', gmail: '', direction: '', nif: '', rols: ['ADMIN'], banned: false };

    expect(component.isAdmin()).toBeTrue();
  });

  it('isAdmin should return false when currentUser is null', () => {
    component.currentUser = null;

    expect(component.isAdmin()).toBeFalse();
  });

  it('isUser should return true when user has USER role', () => {
    component.currentUser = { id: 1, name: 'User', password: '', gmail: '', direction: '', nif: '', rols: ['USER'], banned: false };

    expect(component.isUser()).toBeTrue();
  });

  it('isUser should return false when currentUser is null', () => {
    component.currentUser = null;

    expect(component.isUser()).toBeFalse();
  });
});
