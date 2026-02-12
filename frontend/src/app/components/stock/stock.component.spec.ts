import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { StockComponent } from './stock.component';
import { CheeseService } from '../../service/cheese.service';
import { Router } from '@angular/router';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';
import { CheeseDTO } from '../../dto/cheese.dto';

describe('StockComponent (unit)', () => {

    let component: StockComponent;
    let fixture: ComponentFixture<StockComponent>;

    let mockCheeseService: jasmine.SpyObj<CheeseService>;
    let mockRouter: jasmine.SpyObj<Router>;

    const cheesesMock: CheeseDTO[] = [
        {
            id: 1,
            name: 'Curado',
            price: 10,
            description: 'desc',
            type: 'tipo',
            manufactureDate: '2024-01-01',
            expirationDate: '2025-01-01',
            boxes: [1, 2, 3, 4, 5, 6]
        },
        {
            id: 2,
            name: 'Tierno',
            price: 12,
            description: 'desc2',
            type: 'tipo2',
            manufactureDate: '2024-02-01',
            expirationDate: '2025-02-01',
            boxes: []
        }
    ];

    beforeEach(async () => {

        mockCheeseService = jasmine.createSpyObj('CheeseService', [
            'getAllCheeses',
            'getCheeseImage',
            'addBox',
            'removeBox'
        ]);

        mockRouter = jasmine.createSpyObj('Router', ['navigate']);

        mockCheeseService.getAllCheeses.and.returnValue(
            of(JSON.parse(JSON.stringify(cheesesMock)))
        );

        mockCheeseService.getCheeseImage.and.returnValue(of(new Blob(['fake'])));

        await TestBed.configureTestingModule({
            imports: [StockComponent],
            providers: [
                { provide: CheeseService, useValue: mockCheeseService },
                { provide: Router, useValue: mockRouter }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(StockComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should render stock title', () => {
        const title = fixture.debugElement.query(By.css('.section-title')).nativeElement;
        expect(title.textContent.trim()).toBe('Stock');
    });

    it('should load cheeses on init', () => {
        expect(mockCheeseService.getAllCheeses).toHaveBeenCalled();
        expect(component.cheeses.length).toBe(2);
    });

    it('should load cheese images', () => {
        expect(mockCheeseService.getCheeseImage).toHaveBeenCalledWith(1);
        expect(mockCheeseService.getCheeseImage).toHaveBeenCalledWith(2);
    });

    it('should paginate boxes correctly', () => {
        const item = component.cheeses[0];

        const page1 = component.getPagedBoxes(item);
        expect(page1).toEqual([1, 2, 3, 4, 5]);

        component.nextBoxPage(item);
        const page2 = component.getPagedBoxes(item);
        expect(page2).toEqual([6]);
    });

    it('should add a box', fakeAsync(() => {
        const item = component.cheeses[0];
        item.newBoxValue = 7;

        mockCheeseService.addBox.and.returnValue(
            of({ ...item.cheese, boxes: [...item.cheese.boxes, 7] })
        );

        component.addBox(item);
        tick();

        expect(mockCheeseService.addBox).toHaveBeenCalledWith(1, 7);
        expect(item.cheese.boxes.includes(7)).toBeTrue();
        expect(item.newBoxValue).toBeNull();
    }));

    it('should remove a box', fakeAsync(() => {
        const item = component.cheeses[0];

        mockCheeseService.removeBox.and.returnValue(
            of({ ...item.cheese, boxes: item.cheese.boxes.slice(1) })
        );

        component.removeBox(item, 0);
        tick();

        expect(mockCheeseService.removeBox).toHaveBeenCalledWith(1, 0);
        expect(item.cheese.boxes.length).toBe(5);
    }));

    it('should show "No hay cajas" when cheese has no boxes', () => {
        const debug = fixture.debugElement;
        const noBoxes = debug.queryAll(By.css('.no-boxes'));

        expect(noBoxes.length).toBe(1);
        expect(noBoxes[0].nativeElement.textContent.trim()).toBe('No hay cajas');
    });

    it('should disable prev cheese page at start', () => {
        expect(component.isPrevCheesePageDisabled()).toBeTrue();
    });

    it('should navigate to next cheese page', () => {
        component.totalCheeses = 6;
        component.cheesePageSize = 3;

        expect(component.isNextCheesePageDisabled()).toBeFalse();

        component.nextCheesePage();
        expect(component.currentCheesePage).toBe(1);
    });

    it('should navigate to /error when server fails', () => {
        mockCheeseService.getAllCheeses.and.returnValue(
            throwError(() => ({ status: 500 }))
        );

        fixture = TestBed.createComponent(StockComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

});
