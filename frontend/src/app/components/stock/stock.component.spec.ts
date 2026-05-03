import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { StockComponent } from './stock.component';
import { CheeseService } from '../../service/cheese.service';
import { Router } from '@angular/router';
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
            of({
                content: JSON.parse(JSON.stringify(cheesesMock)),
                totalPages: 1,
                totalElements: cheesesMock.length,
                size: 10,
                number: 0,
                first: true,
                last: true,
                numberOfElements: cheesesMock.length
            } as any)
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

    it('should set imageUrl to null when image blob is empty', () => {
        const item = component.cheeses[0];
        item.imageUrl = null;

        mockCheeseService.getCheeseImage.and.returnValue(of(new Blob([])));
        component.loadCheeseImage(item);

        expect(item.imageUrl).toBeNull();
    });

    it('should set imageUrl to null on image load error', () => {
        const item = component.cheeses[0];
        item.imageUrl = 'some-url';

        mockCheeseService.getCheeseImage.and.returnValue(throwError(() => new Error('fail')));
        component.loadCheeseImage(item);

        expect(item.imageUrl).toBeNull();
    });

    it('should not call getCheeseImage when cheese has no id', () => {
        const noIdStock = {
            cheese: { id: 0, name: 'X', price: 1, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [] },
            imageUrl: null,
            currentBoxPage: 0,
            newBoxValue: null
        };

        mockCheeseService.getCheeseImage.calls.reset();
        component.loadCheeseImage(noIdStock);

        expect(mockCheeseService.getCheeseImage).not.toHaveBeenCalled();
    });

    it('should paginate boxes correctly', () => {
        const item = component.cheeses[0];

        const page1 = component.getPagedBoxes(item);
        expect(page1).toEqual([1, 2, 3, 4, 5]);

        component.nextBoxPage(item);
        const page2 = component.getPagedBoxes(item);
        expect(page2).toEqual([6]);
    });

    it('should not go past last box page', () => {
        const item = component.cheeses[0];
        item.currentBoxPage = 1;

        component.nextBoxPage(item);

        expect(item.currentBoxPage).toBe(1);
    });

    it('should go to previous box page', () => {
        const item = component.cheeses[0];
        item.currentBoxPage = 1;

        component.prevBoxPage(item);

        expect(item.currentBoxPage).toBe(0);
    });

    it('should not go below first box page', () => {
        const item = component.cheeses[0];
        item.currentBoxPage = 0;

        component.prevBoxPage(item);

        expect(item.currentBoxPage).toBe(0);
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

    it('should not call addBox when newBoxValue is null', () => {
        const item = component.cheeses[0];
        item.newBoxValue = null;

        component.addBox(item);

        expect(mockCheeseService.addBox).not.toHaveBeenCalled();
    });

    it('should not call addBox when newBoxValue is 0 or negative', () => {
        const item = component.cheeses[0];
        item.newBoxValue = 0;

        component.addBox(item);

        expect(mockCheeseService.addBox).not.toHaveBeenCalled();
    });

    it('should adjust currentBoxPage when it exceeds max after addBox', fakeAsync(() => {
        const item = component.cheeses[0];
        item.newBoxValue = 7;
        item.currentBoxPage = 10;

        mockCheeseService.addBox.and.returnValue(
            of({ ...item.cheese, boxes: [1, 2, 3, 4, 5, 6, 7] })
        );

        component.addBox(item);
        tick();

        expect(item.currentBoxPage).toBeLessThanOrEqual(1);
    }));

    it('should navigate to /auth/login when addBox returns 401', () => {
        const item = component.cheeses[0];
        item.newBoxValue = 5;

        mockCheeseService.addBox.and.returnValue(throwError(() => ({ status: 401 })));

        component.addBox(item);

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
    });

    it('should navigate to /error when addBox returns 403', () => {
        const item = component.cheeses[0];
        item.newBoxValue = 5;

        mockCheeseService.addBox.and.returnValue(throwError(() => ({ status: 403 })));

        component.addBox(item);

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should navigate to /error when addBox returns 500', () => {
        const item = component.cheeses[0];
        item.newBoxValue = 5;

        mockCheeseService.addBox.and.returnValue(throwError(() => ({ status: 500 })));

        component.addBox(item);

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

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

    it('should not call removeBox when cheese has no id', () => {
        const noIdStock = {
            cheese: { id: 0, name: 'X', price: 1, description: '', type: '', manufactureDate: '', expirationDate: '', boxes: [1] },
            imageUrl: null,
            currentBoxPage: 0,
            newBoxValue: null
        };

        component.removeBox(noIdStock, 0);

        expect(mockCheeseService.removeBox).not.toHaveBeenCalled();
    });

    it('should decrement currentBoxPage when it exceeds max after removeBox', fakeAsync(() => {
        const item = component.cheeses[0];
        item.currentBoxPage = 1;

        mockCheeseService.removeBox.and.returnValue(
            of({ ...item.cheese, boxes: [] })
        );

        component.removeBox(item, 0);
        tick();

        expect(item.currentBoxPage).toBe(0);
    }));

    it('should navigate to /auth/login when removeBox returns 401', () => {
        const item = component.cheeses[0];

        mockCheeseService.removeBox.and.returnValue(throwError(() => ({ status: 401 })));

        component.removeBox(item, 0);

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
    });

    it('should navigate to /error when removeBox returns 403', () => {
        const item = component.cheeses[0];

        mockCheeseService.removeBox.and.returnValue(throwError(() => ({ status: 403 })));

        component.removeBox(item, 0);

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should navigate to /error when removeBox returns 500', () => {
        const item = component.cheeses[0];

        mockCheeseService.removeBox.and.returnValue(throwError(() => ({ status: 500 })));

        component.removeBox(item, 0);

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

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

    it('should not navigate past last cheese page', () => {
        component.totalCheeses = 6;
        component.cheesePageSize = 3;
        component.currentCheesePage = 1;

        spyOn(component, 'loadCheeses');

        component.nextCheesePage();

        expect(component.currentCheesePage).toBe(1);
        expect(component.loadCheeses).not.toHaveBeenCalled();
        expect(component.isNextCheesePageDisabled()).toBeTrue();
    });

    it('should navigate to previous cheese page', () => {
        component.totalCheeses = 6;
        component.cheesePageSize = 3;
        component.currentCheesePage = 1;
        mockCheeseService.getAllCheeses.calls.reset();
        mockCheeseService.getAllCheeses.and.returnValue(of({
            content: JSON.parse(JSON.stringify(cheesesMock)),
            totalPages: 2, totalElements: 6, size: 3, number: 0, first: true, last: false, numberOfElements: 3
        } as any));

        component.prevCheesePage();

        expect(component.currentCheesePage).toBe(0);
    });

    it('should not navigate to negative cheese page', () => {
        component.currentCheesePage = 0;
        mockCheeseService.getAllCheeses.calls.reset();

        component.prevCheesePage();

        expect(component.currentCheesePage).toBe(0);
        expect(mockCheeseService.getAllCheeses).not.toHaveBeenCalled();
    });

    it('should compute totalCheesePages correctly', () => {
        component.totalCheeses = 7;
        component.cheesePageSize = 3;

        expect(component.totalCheesePages).toBe(3);
    });

    it('should navigate to /error when server fails with 500', () => {
        mockCheeseService.getAllCheeses.and.returnValue(
            throwError(() => ({ status: 500 }))
        );

        fixture = TestBed.createComponent(StockComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should navigate to /auth/login when loadCheeses fails with 401', () => {
        mockCheeseService.getAllCheeses.and.returnValue(throwError(() => ({ status: 401 })));

        component.loadCheeses();

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
    });

    it('should navigate to /error when loadCheeses fails with 403', () => {
        mockCheeseService.getAllCheeses.and.returnValue(throwError(() => ({ status: 403 })));

        component.loadCheeses();

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should not navigate when loadCheeses fails with 404', () => {
        mockCheeseService.getAllCheeses.and.returnValue(throwError(() => ({ status: 404 })));
        mockRouter.navigate.calls.reset();

        component.loadCheeses();

        expect(mockRouter.navigate).not.toHaveBeenCalled();
    });

});
