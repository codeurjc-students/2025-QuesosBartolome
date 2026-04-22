import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { OrdersComponent } from './orders.component';
import { OrderService } from '../../service/order.service';
import { OrderDTO } from '../../dto/order.dto';
import { By } from '@angular/platform-browser';
import { Router } from '@angular/router';
import { UserService } from '../../service/user.service';

describe('OrdersComponent (unit)', () => {

  let component: OrdersComponent;
  let fixture: ComponentFixture<OrdersComponent>;

  let mockOrderService: jasmine.SpyObj<OrderService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockUserService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {

    mockOrderService = jasmine.createSpyObj('OrderService', [
      'getAllOrders'
    ]);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser', 'getMyOrders']);
    mockUserService.getCurrentUser.and.returnValue(of({ id: 1, rols: ['ADMIN'] } as any));

    const mockOrders: OrderDTO[] = [
      {
        id: 1,
        user: { id: 1, name: 'Victor' },
        totalWeight: 6.32,
        totalPrice: 110.6,
        orderDate: '2025-12-21T03:26:53.824654',
        processed: false,
        items: [
          {
            id: 2,
            cheeseId: 1,
            cheeseName: 'Semicurado',
            cheesePrice: 17.5,
            boxes: [6.32],
            weight: 6.32,
            totalPrice: 110.6
          }
        ]
      }
    ];

    mockOrderService.getAllOrders.and.returnValue(
      of({
        content: mockOrders,
        totalPages: 1,
        totalElements: 1,
        number: 0,
        size: 10
      } as any)
    );

    mockUserService.getMyOrders.and.returnValue(
      of({
        content: mockOrders,
        totalPages: 1,
        totalElements: 1,
        number: 0,
        size: 10
      } as any)
    );

    await TestBed.configureTestingModule({
      imports: [OrdersComponent],
      providers: [
        { provide: OrderService, useValue: mockOrderService },
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load and render orders', () => {
    const rows = fixture.debugElement.queryAll(By.css('.orders-row'));
    expect(rows.length).toBe(1);

    const userName = rows[0].query(By.css('span:nth-child(2)'))
      .nativeElement.textContent.trim();

    expect(userName).toBe('Victor');
  });

  it('should load user orders when current user is USER', () => {
    mockUserService.getCurrentUser.and.returnValue(of({ id: 7, rols: ['USER'] } as any));
    mockOrderService.getAllOrders.calls.reset();
    mockUserService.getMyOrders.calls.reset();

    component.ngOnInit();

    expect(mockUserService.getMyOrders).toHaveBeenCalledWith(7, 0, 10);
    expect(mockOrderService.getAllOrders).not.toHaveBeenCalled();
    expect(component.orders.length).toBe(1);
  });

  it('should navigate to login when user is not admin and has no id', () => {
    component.currentUser = { rols: ['USER'] } as any;
    mockRouter.navigate.calls.reset();
    mockOrderService.getAllOrders.calls.reset();
    mockUserService.getMyOrders.calls.reset();

    component.loadOrders();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
    expect(mockOrderService.getAllOrders).not.toHaveBeenCalled();
    expect(mockUserService.getMyOrders).not.toHaveBeenCalled();
  });

  it('should call service with correct page when nextPage is called and there are more pages', () => {
    component.currentPage = 0;
    component.totalPages = 3;
    mockOrderService.getAllOrders.calls.reset();

    component.nextPage();

    expect(component.currentPage).toBe(1);
    expect(mockOrderService.getAllOrders)
      .toHaveBeenCalledWith(1, component.pageSize);
  });

  it('should not go past last page', () => {
    component.currentPage = 0;
    component.totalPages = 1;
    mockOrderService.getAllOrders.calls.reset();

    component.nextPage();

    expect(component.currentPage).toBe(0);
    expect(mockOrderService.getAllOrders).not.toHaveBeenCalled();
  });

  it('should decrease page when prevPage is called', () => {
    component.currentPage = 1;

    component.prevPage();

    expect(component.currentPage).toBe(0);
    expect(mockOrderService.getAllOrders)
      .toHaveBeenCalledWith(0, component.pageSize);
  });

  it('should not go to negative page', () => {
    component.currentPage = 0;
    mockOrderService.getAllOrders.calls.reset();

    component.prevPage();

    expect(component.currentPage).toBe(0);
    expect(mockOrderService.getAllOrders).not.toHaveBeenCalled();
  });

  it('should handle error when service fails', () => {
    mockOrderService.getAllOrders.and.returnValue(
      throwError(() => new Error('Error'))
    );

    component.loadOrders();

    expect(component.loading).toBeFalse();
  });

  it('should navigate to /error when service fails with 500', () => {
    mockOrderService.getAllOrders.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.loadOrders();

    expect(component.loading).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should navigate to preview route when processing an order', () => {
    component.processOrder(1);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders', 1, 'preview']);
  });

  it('should process order from onOrderAction and stop event propagation', () => {
    const event = jasmine.createSpyObj<Event>('event', ['stopPropagation']);
    const processSpy = spyOn(component, 'processOrder');

    component.onOrderAction({ id: 99 } as any, event);

    expect(event.stopPropagation).toHaveBeenCalled();
    expect(processSpy).toHaveBeenCalledWith(99);
  });

  it('should process order from onOrderAction without event', () => {
    const processSpy = spyOn(component, 'processOrder');

    component.onOrderAction({ id: 77 } as any);

    expect(processSpy).toHaveBeenCalledWith(77);
  });

  it('should return true in isAdmin when user has ADMIN role', () => {
    component.currentUser = { rols: ['ADMIN'] } as any;

    expect(component.isAdmin()).toBeTrue();
  });

  it('should return false in isAdmin when user has USER role', () => {
    component.currentUser = { rols: ['USER'] } as any;

    expect(component.isAdmin()).toBeFalse();
  });

  it('should return status labels correctly', () => {
    expect(component.getOrderStatus({ processed: true } as any)).toBe('Procesado');
    expect(component.getOrderStatus({ processed: false } as any)).toBe('Pendiente');
  });

  it('should return status css classes correctly', () => {
    expect(component.getOrderStatusClass({ processed: true } as any)).toBe('status-processed');
    expect(component.getOrderStatusClass({ processed: false } as any)).toBe('status-pending');
  });

});
