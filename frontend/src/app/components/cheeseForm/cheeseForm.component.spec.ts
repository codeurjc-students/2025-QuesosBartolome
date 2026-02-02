import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheeseFormComponent } from './cheeseForm.component';
import { CheeseService } from '../../service/cheese.service';
import { UserService } from '../../service/user.service';
import { Router } from '@angular/router';
import { of, throwError } from 'rxjs';

describe('CheeseFormComponent (unit)', () => {

    let component: CheeseFormComponent;
    let fixture: ComponentFixture<CheeseFormComponent>;

    let mockCheeseService: jasmine.SpyObj<CheeseService>;
    let mockUserService: jasmine.SpyObj<UserService>;
    let mockRouter: jasmine.SpyObj<Router>;

    beforeEach(async () => {

        mockCheeseService = jasmine.createSpyObj('CheeseService', ['createCheese', 'uploadCheeseImage']);
        mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
        mockRouter = jasmine.createSpyObj('Router', ['navigate']);

        mockUserService.getCurrentUser.and.returnValue(of({
            id: 1,
            name: 'Admin',
            password: '1234',
            gmail: 'admin@gmail.com',
            direction: 'Calle Falsa 123',
            nif: '12345678A',
            rols: ['ADMIN']
        }));


        await TestBed.configureTestingModule({
            imports: [CheeseFormComponent],
            providers: [
                { provide: CheeseService, useValue: mockCheeseService },
                { provide: UserService, useValue: mockUserService },
                { provide: Router, useValue: mockRouter }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(CheeseFormComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should alert when required fields are missing', () => {
        spyOn(window, 'alert');

        component.name = '';
        component.description = '';
        component.type = '';
        component.manufactureDate = '';
        component.expirationDate = '';

        component.createCheese();

        expect(window.alert).toHaveBeenCalledWith("Todos los campos son obligatorios");
    });

    it('should alert when price is <= 0', () => {
        spyOn(window, 'alert');

        component.name = 'Queso';
        component.description = 'desc';
        component.type = 'Cremoso';
        component.manufactureDate = '2024-01-01';
        component.expirationDate = '2025-01-01';
        component.price = 0;

        component.createCheese();

        expect(window.alert).toHaveBeenCalledWith("El precio debe ser mayor que 0");
    });

    it('should alert when expiration < manufacture', () => {
        spyOn(window, 'alert');

        component.name = 'Queso';
        component.description = 'desc';
        component.type = 'Cremoso';
        component.price = 10;
        component.manufactureDate = '2024-01-10';
        component.expirationDate = '2024-01-01';

        component.createCheese();

        expect(window.alert).toHaveBeenCalledWith(
            "La fecha de caducidad debe ser posterior a la de fabricación"
        );
    });

    it('should call createCheese() and show success alert', () => {
        spyOn(window, 'alert');

        component.name = 'Queso';
        component.description = 'desc';
        component.type = 'Cremoso';
        component.price = 10;
        component.manufactureDate = '2024-01-01';
        component.expirationDate = '2025-01-01';

        mockCheeseService.createCheese.and.returnValue(of({
            id: 99,
            name: 'Nuevo Queso',
            price: 12.50,
            description: 'Queso creado',
            manufactureDate: '2024-01-24',
            expirationDate: '2025-01-25',
            type: 'Cremoso',
            boxes: []
        }));


        mockCheeseService.uploadCheeseImage.and.returnValue(of(true));

        component.createCheese();

        expect(mockCheeseService.createCheese).toHaveBeenCalled();
        expect(window.alert).toHaveBeenCalledWith("Queso creado correctamente");
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should alert error when createCheese() fails', () => {
        spyOn(window, 'alert');

        component.name = 'Queso';
        component.description = 'desc';
        component.type = 'Cremoso';
        component.price = 10;
        component.manufactureDate = '2024-01-01';
        component.expirationDate = '2025-01-01';

        mockCheeseService.createCheese.and.returnValue(
            throwError(() => new Error("fail"))
        );

        component.createCheese();

        expect(window.alert).toHaveBeenCalledWith("Error al crear queso");
    });

});
