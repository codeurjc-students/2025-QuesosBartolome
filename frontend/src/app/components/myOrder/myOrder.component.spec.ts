import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MyOrderComponent } from './myOrder.component';
import { CartService } from '../../service/cart.service';
import { OrderService } from '../../service/order.service';
import { CartDTO } from '../../dto/cart.dto';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { DialogService } from '../../service/dialog.service';
import { Router } from '@angular/router';

describe('MyOrderComponent (unit)', () => {

    let component: MyOrderComponent;
    let fixture: ComponentFixture<MyOrderComponent>;

    let mockCartService: jasmine.SpyObj<CartService>;
    let mockOrderService: jasmine.SpyObj<OrderService>;
    let mockDialogService: jasmine.SpyObj<DialogService>;
    let mockRouter: jasmine.SpyObj<Router>;

    beforeEach(async () => {

        mockCartService = jasmine.createSpyObj('CartService', [
            'getMyCart',
            'removeItemFromCart'
        ]);

        mockOrderService = jasmine.createSpyObj('OrderService', [
            'confirmOrder'
        ]);
        mockDialogService = jasmine.createSpyObj('DialogService', ['alert']);
        mockRouter = jasmine.createSpyObj('Router', ['navigate']);

        const mockCart: CartDTO = {
            id: 1,
            user: { id: 1, name: 'User1' },
            totalWeight: 2.5,
            totalPrice: 30,
            items: [
                {
                    id: 10,
                    cheeseId: 2,
                    cheeseName: 'Azul',
                    cheesePrice: 12,
                    boxes: [1.25],
                    weight: 1.25,
                    totalPrice: 15
                }
            ]
        };

        mockCartService.getMyCart.and.returnValue(of(mockCart));

        await TestBed.configureTestingModule({
            imports: [MyOrderComponent],
            providers: [
                { provide: CartService, useValue: mockCartService },
                { provide: OrderService, useValue: mockOrderService },
                { provide: DialogService, useValue: mockDialogService },
                { provide: Router, useValue: mockRouter }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(MyOrderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should render order items', () => {
        const debug: DebugElement = fixture.debugElement;

        const items = debug.queryAll(By.css('.order-item:not(.order-total)'));
        expect(items.length).toBe(1);

        const name = items[0].query(By.css('.col-name')).nativeElement.textContent.trim();
        expect(name).toBe('Azul');

        const totalPrice = debug.query(By.css('.order-total .col-price')).nativeElement.textContent.trim();
        expect(totalPrice).toContain('30');
    });

    it('should remove an item when removeItem is called', () => {
        const updatedCart: CartDTO = {
            id: 1,
            user: { id: 1, name: 'User1' },
            totalWeight: 0,
            totalPrice: 0,
            items: []
        };

        mockCartService.removeItemFromCart.and.returnValue(of(updatedCart));

        component.removeItem(10);
        fixture.detectChanges();

        expect(mockCartService.removeItemFromCart).toHaveBeenCalledWith(10);
        expect(component.order.items.length).toBe(0);
    });

    it('should show success alert when order is confirmed', () => {
        mockOrderService.confirmOrder.and.returnValue(of({} as any));

        component.makeOrder();

        expect(mockOrderService.confirmOrder).toHaveBeenCalled();
        expect(mockDialogService.alert).toHaveBeenCalledWith('Pedido realizado correctamente');
    });

    it('should show error alert when order confirmation fails', () => {
        mockOrderService.confirmOrder.and.returnValue(throwError(() => new Error('fail')));

        component.makeOrder();

        expect(mockOrderService.confirmOrder).toHaveBeenCalled();
        expect(mockDialogService.alert).toHaveBeenCalledWith('Error al hacer el pedido');
    });

    // --- ngOnInit error branches ---

    it('should navigate to /auth/login when getMyCart returns 401', () => {
        mockCartService.getMyCart.and.returnValue(throwError(() => ({ status: 401 })));
        component.ngOnInit();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
    });

    it('should navigate to /error when getMyCart returns 403', () => {
        mockCartService.getMyCart.and.returnValue(throwError(() => ({ status: 403 })));
        component.ngOnInit();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should navigate to /error when getMyCart returns 500', () => {
        mockCartService.getMyCart.and.returnValue(throwError(() => ({ status: 500 })));
        component.ngOnInit();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should set loading=false and not navigate when getMyCart returns 404', () => {
        mockCartService.getMyCart.and.returnValue(throwError(() => ({ status: 404 })));
        component.ngOnInit();
        expect(component.loading).toBeFalse();
        expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    // --- removeItem error branches ---

    it('should navigate to /auth/login when removeItem returns 401', () => {
        mockCartService.removeItemFromCart.and.returnValue(throwError(() => ({ status: 401 })));
        component.removeItem(10);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
    });

    it('should navigate to /error when removeItem returns 403', () => {
        mockCartService.removeItemFromCart.and.returnValue(throwError(() => ({ status: 403 })));
        component.removeItem(10);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should navigate to /error when removeItem returns 500', () => {
        mockCartService.removeItemFromCart.and.returnValue(throwError(() => ({ status: 500 })));
        component.removeItem(10);
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should not navigate when removeItem returns 404', () => {
        mockCartService.removeItemFromCart.and.returnValue(throwError(() => ({ status: 404 })));
        component.removeItem(10);
        expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

    // --- makeOrder error branches ---

    it('should navigate to /auth/login when makeOrder returns 401', () => {
        mockOrderService.confirmOrder.and.returnValue(throwError(() => ({ status: 401 })));
        component.makeOrder();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
        expect(mockDialogService.alert).not.toHaveBeenCalled();
    });

    it('should navigate to /error when makeOrder returns 403', () => {
        mockOrderService.confirmOrder.and.returnValue(throwError(() => ({ status: 403 })));
        component.makeOrder();
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
        expect(mockDialogService.alert).not.toHaveBeenCalled();
    });

    it('should alert and navigate to /error when makeOrder returns 500', () => {
        mockOrderService.confirmOrder.and.returnValue(throwError(() => ({ status: 500 })));
        component.makeOrder();
        expect(mockDialogService.alert).toHaveBeenCalledWith('Error al hacer el pedido');
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

});
