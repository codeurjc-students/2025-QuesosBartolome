import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MyOrderComponent } from './myOrder.component';
import { CartService } from '../../service/cart.service';
import { OrderService } from '../../service/order.service';
import { CartDTO } from '../../dto/cart.dto';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

describe('MyOrderComponent (unit)', () => {

    let component: MyOrderComponent;
    let fixture: ComponentFixture<MyOrderComponent>;

    let mockCartService: jasmine.SpyObj<CartService>;
    let mockOrderService: jasmine.SpyObj<OrderService>;

    beforeEach(async () => {

        mockCartService = jasmine.createSpyObj('CartService', [
            'getMyCart',
            'removeItemFromCart'
        ]);

        mockOrderService = jasmine.createSpyObj('OrderService', [
            'confirmOrder'
        ]);

        const mockCart: CartDTO = {
            id: 1,
            user: { id: 1, name: 'Victor' },
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
                { provide: OrderService, useValue: mockOrderService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(MyOrderComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should render order items', () => {
        const debug: DebugElement = fixture.debugElement;

        const items = debug.queryAll(By.css('.order-item'));
        expect(items.length).toBe(1);

        const name = items[0].query(By.css('.col-name')).nativeElement.textContent.trim();
        expect(name).toBe('Azul');

        const totalPrice = debug.query(By.css('.order-total .col-price')).nativeElement.textContent.trim();
        expect(totalPrice).toContain('30');
    });

    it('should remove an item when removeItem is called', () => {
        const updatedCart: CartDTO = {
            id: 1,
            user: { id: 1, name: 'Victor' },
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
        spyOn(window, 'alert');
        mockOrderService.confirmOrder.and.returnValue(of({} as any));

        component.makeOrder();

        expect(mockOrderService.confirmOrder).toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith('Pedido realizado correctamente');
    });

    it('should show error alert when order confirmation fails', () => {
        spyOn(window, 'alert');
        mockOrderService.confirmOrder.and.returnValue(throwError(() => new Error('fail')));

        component.makeOrder();

        expect(mockOrderService.confirmOrder).toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith('Error al hacer el pedido');
    });

});
