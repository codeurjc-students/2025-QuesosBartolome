import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { OrderPreviewComponent } from './orderPreview.component';
import { ActivatedRoute, convertToParamMap, Router } from '@angular/router';
import { OrderService } from '../../service/order.service';
import { InvoiceService } from '../../service/invoice.service';

describe('OrderPreviewComponent', () => {
  let component: OrderPreviewComponent;
  let fixture: ComponentFixture<OrderPreviewComponent>;

  let mockOrderService: jasmine.SpyObj<OrderService>;
  let mockInvoiceService: jasmine.SpyObj<InvoiceService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let routeId = '12';

  const mockOrder = {
    id: 12,
    user: { id: 3, name: 'Marta' },
    totalWeight: 4.5,
    totalPrice: 86,
    orderDate: '2026-03-18T10:30:00',
    items: [
      {
        id: 1,
        cheeseId: 10,
        cheeseName: 'Manchego Curado',
        cheesePrice: 20,
        boxes: [1, 1],
        weight: 2,
        totalPrice: 40
      }
    ]
  } as any;

  beforeEach(async () => {
    mockOrderService = jasmine.createSpyObj('OrderService', ['getOrderById', 'rejectOrder']);
    mockInvoiceService = jasmine.createSpyObj('InvoiceService', ['createInvoiceFromOrder']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    mockOrderService.getOrderById.and.returnValue(of(mockOrder));
    mockOrderService.rejectOrder.and.returnValue(of(mockOrder));
    mockInvoiceService.createInvoiceFromOrder.and.returnValue(of({ invNo: 'FACT-Q26/12' } as any));

    await TestBed.configureTestingModule({
      imports: [OrderPreviewComponent],
      providers: [
        { provide: OrderService, useValue: mockOrderService },
        { provide: InvoiceService, useValue: mockInvoiceService },
        { provide: Router, useValue: mockRouter },
        {
          provide: ActivatedRoute,
          useValue: {
            get snapshot() {
              return { paramMap: convertToParamMap({ id: routeId }) };
            }
          }
        }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(OrderPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load and render order by route id', () => {
    const compiled = fixture.nativeElement as HTMLElement;

    expect(mockOrderService.getOrderById).toHaveBeenCalledWith(12);
    expect(compiled.textContent).toContain('Marta');
    expect(compiled.textContent).toContain('Manchego Curado');
    expect(component.loading).toBeFalse();
  });

  it('should navigate to /orders when route id is invalid', () => {
    routeId = 'abc';
    mockRouter.navigate.calls.reset();
    mockOrderService.getOrderById.calls.reset();

    component.loading = true;
    component.ngOnInit();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
    expect(mockOrderService.getOrderById).not.toHaveBeenCalled();
  });

  it('should navigate to /orders when order load returns 404', () => {
    routeId = '12';
    mockOrderService.getOrderById.and.returnValue(throwError(() => ({ status: 404 })));
    mockRouter.navigate.calls.reset();

    component.loading = true;
    component.ngOnInit();

    expect(component.loading).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
  });

  it('should navigate to /error when order load returns 500', () => {
    routeId = '12';
    mockOrderService.getOrderById.and.returnValue(throwError(() => ({ status: 500 })));
    mockRouter.navigate.calls.reset();

    component.loading = true;
    component.ngOnInit();

    expect(component.loading).toBeFalse();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should navigate back to orders', () => {
    component.goBack();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
  });

  it('should confirm order and navigate on invoice success', () => {
    spyOn(window, 'alert');

    component.order = mockOrder;
    component.onConfirm();

    expect(mockInvoiceService.createInvoiceFromOrder).toHaveBeenCalledWith(mockOrder);
    expect(component.creatingInvoice).toBeFalse();
    expect(window.alert).toHaveBeenCalledWith('Factura creada correctamente: FACT-Q26/12');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
  });

  it('should not confirm when order is null', () => {
    component.order = null;
    component.onConfirm();
    expect(mockInvoiceService.createInvoiceFromOrder).not.toHaveBeenCalled();
  });

  it('should handle 409 when confirming invoice', () => {
    spyOn(window, 'alert');
    mockInvoiceService.createInvoiceFromOrder.and.returnValue(throwError(() => ({ status: 409 })));
    component.order = mockOrder;

    component.onConfirm();

    expect(window.alert).toHaveBeenCalledWith('Este pedido ya estaba procesado y ya tiene factura.');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
    expect(component.creatingInvoice).toBeFalse();
  });

  it('should navigate to error when confirming invoice fails with 500', () => {
    mockInvoiceService.createInvoiceFromOrder.and.returnValue(throwError(() => ({ status: 500 })));
    component.order = mockOrder;

    component.onConfirm();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    expect(component.creatingInvoice).toBeFalse();
  });

  it('should show generic alert when confirming invoice fails with non-409/non-500', () => {
    spyOn(window, 'alert');
    mockInvoiceService.createInvoiceFromOrder.and.returnValue(throwError(() => ({ status: 400 })));
    component.order = mockOrder;

    component.onConfirm();

    expect(window.alert).toHaveBeenCalledWith('No se ha podido crear la factura para este pedido.');
    expect(component.creatingInvoice).toBeFalse();
  });

  it('should reject order and navigate on success', () => {
    spyOn(window, 'alert');
    component.order = mockOrder;

    component.onCancel();

    expect(mockOrderService.rejectOrder).toHaveBeenCalledWith(12);
    expect(component.rejectingOrder).toBeFalse();
    expect(window.alert).toHaveBeenCalledWith('Pedido rechazado.');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
  });

  it('should not reject when order is null', () => {
    component.order = null;
    component.onCancel();
    expect(mockOrderService.rejectOrder).not.toHaveBeenCalled();
  });

  it('should not reject when invoice is being created', () => {
    component.order = mockOrder;
    component.creatingInvoice = true;

    component.onCancel();

    expect(mockOrderService.rejectOrder).not.toHaveBeenCalled();
  });

  it('should handle 409 when rejecting order', () => {
    spyOn(window, 'alert');
    mockOrderService.rejectOrder.and.returnValue(throwError(() => ({ status: 409 })));
    component.order = mockOrder;

    component.onCancel();

    expect(window.alert).toHaveBeenCalledWith('Este pedido ya estaba procesado.');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
  });

  it('should handle 404 when rejecting order', () => {
    spyOn(window, 'alert');
    mockOrderService.rejectOrder.and.returnValue(throwError(() => ({ status: 404 })));
    component.order = mockOrder;

    component.onCancel();

    expect(window.alert).toHaveBeenCalledWith('Pedido no encontrado.');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/orders']);
  });

  it('should navigate to /error when rejecting order fails with 500', () => {
    mockOrderService.rejectOrder.and.returnValue(throwError(() => ({ status: 500 })));
    component.order = mockOrder;

    component.onCancel();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should show generic alert when rejecting order fails with non-404/non-409/non-500', () => {
    spyOn(window, 'alert');
    mockOrderService.rejectOrder.and.returnValue(throwError(() => ({ status: 400 })));
    component.order = mockOrder;

    component.onCancel();

    expect(window.alert).toHaveBeenCalledWith('No se ha podido rechazar el pedido.');
  });
});
