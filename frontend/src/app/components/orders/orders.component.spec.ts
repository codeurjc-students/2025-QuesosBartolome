import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { OrdersComponent } from './orders.component';
import { OrderService } from '../../service/order.service';
import { OrderDTO } from '../../dto/order.dto';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

describe('OrdersComponent (unit)', () => {

  let component: OrdersComponent;
  let fixture: ComponentFixture<OrdersComponent>;

  let mockOrderService: jasmine.SpyObj<OrderService>;

  beforeEach(async () => {

    mockOrderService = jasmine.createSpyObj('OrderService', [
      'getAllOrders'
    ]);

    const mockOrders: OrderDTO[] = [
      {
        id: 1,
        user: { id: 1, name: 'Victor' },
        totalWeight: 6.32,
        totalPrice: 110.6,
        orderDate: '2025-12-21T03:26:53.824654',
        items: [
          {
            id: 2,
            cheese: { id: 1, name: 'Semicurado', price: 17.5 },
            weight: 6.32,
            price: 110.6
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

    await TestBed.configureTestingModule({
      imports: [OrdersComponent],
      providers: [
        { provide: OrderService, useValue: mockOrderService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OrdersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load and render orders', () => {
    const debug: DebugElement = fixture.debugElement;

    const rows = debug.queryAll(By.css('.orders-row'));
    expect(rows.length).toBe(1);

    const userName = rows[0].query(By.css('span:nth-child(2)'))
      .nativeElement.textContent.trim();

    expect(userName).toBe('Victor');
  });

  it('should call service with correct page when nextPage is called', () => {
    component.nextPage();

    expect(mockOrderService.getAllOrders)
      .toHaveBeenCalledWith(1, component.pageSize);
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

    component.prevPage();

    expect(component.currentPage).toBe(0);
  });

  it('should handle error when service fails', () => {
    mockOrderService.getAllOrders.and.returnValue(
      throwError(() => new Error('Error'))
    );

    component.loadOrders();

    expect(component.loading).toBeFalse();
  });

});
