import { ComponentFixture, TestBed } from '@angular/core/testing';
import { CheeseFormComponent } from './cheeseForm.component';
import { CheeseService } from '../../service/cheese.service';
import { UserService } from '../../service/user.service';
import { Router, ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { DialogService } from '../../service/dialog.service';

describe('CheeseFormComponent (unit)', () => {

    let component: CheeseFormComponent;
    let fixture: ComponentFixture<CheeseFormComponent>;

    let mockCheeseService: jasmine.SpyObj<CheeseService>;
    let mockUserService: jasmine.SpyObj<UserService>;
    let mockRouter: jasmine.SpyObj<Router>;
    let mockActivatedRoute: any;
    let mockDialogService: jasmine.SpyObj<DialogService>;

    beforeEach(async () => {

        mockCheeseService = jasmine.createSpyObj('CheeseService', [
            'createCheese',
            'uploadCheeseImage',
            'updateCheese',
            'updateCheeseImage',
            'getCheeseById'
        ]);

        mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);

        mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockDialogService = jasmine.createSpyObj('DialogService', ['alert']);

        mockActivatedRoute = {
            snapshot: { paramMap: new Map() }
        };

        mockUserService.getCurrentUser.and.returnValue(of({
            id: 1,
            name: 'Admin',
            password: '1234',
            gmail: 'admin@gmail.com',
            direction: 'Calle Falsa 123',
            nif: '12345678A',
            rols: ['ADMIN'],
            banned: false
        }));

        await TestBed.configureTestingModule({
            imports: [CheeseFormComponent],
            providers: [
                { provide: CheeseService, useValue: mockCheeseService },
                { provide: UserService, useValue: mockUserService },
                { provide: Router, useValue: mockRouter },
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: DialogService, useValue: mockDialogService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(CheeseFormComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

it('should redirect to /error if user is not admin', () => {
    mockUserService.getCurrentUser.and.returnValue(of({
        id: 2,
        name: 'User',
        password: '1234',
        gmail: 'user@gmail.com',
        direction: 'Fake Street 123',
        nif: '12345678Z',
        rols: ['USER'],
        banned: false
    }));

    component.ngOnInit();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
});


    it('should redirect to /error if getCurrentUser fails', () => {
        mockUserService.getCurrentUser.and.returnValue(throwError(() => new Error('fail')));

        component.ngOnInit();

        expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    });

    it('should load cheese data correctly', () => {
        mockCheeseService.getCheeseById.and.returnValue(of({
            id: 10,
            name: 'Curado',
            price: 15,
            description: 'desc',
            type: 'Cremoso',
            manufactureDate: '2024-01-01',
            expirationDate: '2025-01-01',
            boxes: []
        }));

        component.loadCheeseData(10);

        expect(component.name).toBe('Curado');
        expect(component.price).toBe(15);
        expect(component.type).toBe('Cremoso');
    });

    it('should alert and redirect when loadCheeseData fails', () => {
        mockCheeseService.getCheeseById.and.returnValue(throwError(() => new Error('fail')));

        component.loadCheeseData(10);

        expect(mockDialogService.alert).toHaveBeenCalledWith('Error al cargar los datos del queso');
        expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
    });

    it('should alert when manufactureDate format is invalid', () => {
        component.name = 'Queso';
        component.description = 'desc';
        component.type = 'Cremoso';
        component.price = 10;
        component.manufactureDate = '01-01-2024';
        component.expirationDate = '2025-01-01';

        component.createCheese();

        expect(mockDialogService.alert).toHaveBeenCalledWith(
            "La fecha de fabricación debe estar en formato YYYY-MM-DD"
        );
    });

    it('should alert when expirationDate format is invalid', () => {
        component.name = 'Queso';
        component.description = 'desc';
        component.type = 'Cremoso';
        component.price = 10;
        component.manufactureDate = '2024-01-01';
        component.expirationDate = '01-01-2025';

        component.createCheese();

        expect(mockDialogService.alert).toHaveBeenCalledWith(
            "La fecha de caducidad debe estar en formato YYYY-MM-DD"
        );
    });

    it('should store selected file', () => {
        const fakeFile = new File(['abc'], 'test.png', { type: 'image/png' });

        const event = {
            target: { files: [fakeFile] }
        };

        component.onFileSelected(event);

        expect(component.selectedFile).toBe(fakeFile);
    });

    it('should build cheese data correctly', () => {
        component.name = 'Queso';
        component.price = 10;
        component.description = 'desc';
        component.type = 'Cremoso';
        component.manufactureDate = '2024-01-01';
        component.expirationDate = '2025-01-01';

        const dto = component['buildCheeseData']();

        expect(dto.name).toBe('Queso');
        expect(dto.price).toBe(10);
        expect(dto.type).toBe('Cremoso');
        expect(dto.boxes).toEqual([]);
    });

    it('should call createCheese() and upload image', () => {
    component.name = 'Queso';
    component.description = 'desc';
    component.type = 'Cremoso';
    component.price = 10;
    component.manufactureDate = '2024-01-01';
    component.expirationDate = '2025-01-01';

    const fakeFile = new File(['abc'], 'img.png');
    component.selectedFile = fakeFile;

    mockCheeseService.createCheese.and.returnValue(of({
        id: 50,
        name: 'Queso',
        price: 10,
        description: 'desc',
        manufactureDate: '2024-01-01',
        expirationDate: '2025-01-01',
        type: 'Cremoso',
        boxes: []
    }));

    mockCheeseService.uploadCheeseImage.and.returnValue(of(true));

    component.createCheese();

    expect(mockCheeseService.createCheese).toHaveBeenCalled();
    expect(mockCheeseService.uploadCheeseImage).toHaveBeenCalledWith(50, fakeFile);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
});

    it('should update cheese successfully without image', () => {
    component.isEditMode = true;
    component.cheeseId = 10;

    component.name = 'Queso';
    component.description = 'desc';
    component.type = 'Cremoso';
    component.price = 10;
    component.manufactureDate = '2024-01-01';
    component.expirationDate = '2025-01-01';

    mockCheeseService.updateCheese.and.returnValue(of({
        id: 10,
        name: 'Queso',
        price: 10,
        description: 'desc',
        manufactureDate: '2024-01-01',
        expirationDate: '2025-01-01',
        type: 'Cremoso',
        boxes: []
    }));

    component.editCheese();

    expect(mockCheeseService.updateCheese).toHaveBeenCalled();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/cheeses', 10]);
});


    it('should update cheese and upload image', () => {
    component.isEditMode = true;
    component.cheeseId = 10;

    component.name = 'Queso';
    component.description = 'desc';
    component.type = 'Cremoso';
    component.price = 10;
    component.manufactureDate = '2024-01-01';
    component.expirationDate = '2025-01-01';

    const fakeFile = new File(['abc'], 'img.png');
    component.selectedFile = fakeFile;

    mockCheeseService.updateCheese.and.returnValue(of({
        id: 10,
        name: 'Queso',
        price: 10,
        description: 'desc',
        manufactureDate: '2024-01-01',
        expirationDate: '2025-01-01',
        type: 'Cremoso',
        boxes: []
    }));

    mockCheeseService.updateCheeseImage.and.returnValue(of(true));

    component.editCheese();

    expect(mockCheeseService.updateCheeseImage).toHaveBeenCalledWith(10, fakeFile);
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/cheeses', 10]);
});


    it('should alert error when updateCheese fails', () => {
        component.isEditMode = true;
        component.cheeseId = 10;

        component.name = 'Queso';
        component.description = 'desc';
        component.type = 'Cremoso';
        component.price = 10;
        component.manufactureDate = '2024-01-01';
        component.expirationDate = '2025-01-01';

        mockCheeseService.updateCheese.and.returnValue(
            throwError(() => new Error('fail'))
        );

        component.editCheese();

        expect(mockDialogService.alert).toHaveBeenCalledWith("Error al actualizar queso");
    });

});
