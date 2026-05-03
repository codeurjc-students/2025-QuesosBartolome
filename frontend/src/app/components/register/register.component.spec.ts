import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RegisterComponent } from './register.component';
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';
import { DialogService } from '../../service/dialog.service';
import { UserService } from '../../service/user.service';


describe('RegisterComponent (unit)', () => {

  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let mockLoginService: jasmine.SpyObj<LoginService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockDialogService: jasmine.SpyObj<DialogService>;
  let mockUserService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {

    mockLoginService = jasmine.createSpyObj('LoginService', ['register']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockDialogService = jasmine.createSpyObj('DialogService', ['alert']);
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser']);
    mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter },
        { provide: DialogService, useValue: mockDialogService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegisterComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });


  it('should call register service and navigate on success', () => {
    component.nombre = 'Juan';
    component.password = 'password123';
    component.confirmPassword = 'password123';
    component.email = 'juan@example.com';
    component.direccion = 'Calle Falsa 123';
    component.nif = '12345678A';

    mockLoginService.register.and.returnValue(of({ message: 'User registered successfully' }));

    component.register();

    expect(mockLoginService.register).toHaveBeenCalledWith({
      name: 'Juan',
      password: 'password123',
      gmail: 'juan@example.com',
      direction: 'Calle Falsa 123',
      nif: '12345678A'
    });
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should redirect to home if user is already logged in', () => {
    mockUserService.getCurrentUser.and.returnValue(of({ id: 1, rols: ['USER'] } as any));
    mockRouter.navigate.calls.reset();

    component.ngOnInit();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should not navigate when register service returns an error', () => {
    component.nombre = 'Juan';
    component.password = 'password123';
    component.confirmPassword = 'password123';
    component.email = 'juan@example.com';
    component.direccion = 'Calle Falsa 123';
    component.nif = '12345678A';

    mockLoginService.register.and.returnValue(throwError(() => ({ error: { error: 'Error de prueba' } })));

    component.register();

    expect(mockLoginService.register).toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });


  it('should alert if any required field is empty', () => {
    component.nombre = '';
    component.password = '123';
    component.confirmPassword = '123';
    component.email = 'test@test.com';
    component.direccion = 'dir';
    component.nif = '1234';

    component.register();

    expect(mockDialogService.alert).toHaveBeenCalledWith('Todos los campos son obligatorios');
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

  it('should alert if passwords do not match', () => {
    component.nombre = 'Juan';
    component.password = 'password123';
    component.confirmPassword = 'password456';
    component.email = 'test@test.com';
    component.direccion = 'dir';
    component.nif = '12345678A';

    component.register();

    expect(mockDialogService.alert).toHaveBeenCalledWith('Las contraseñas no coinciden');
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

  it('should alert if password has fewer than 8 characters', () => {
    component.nombre = 'Juan';
    component.password = '1234567';
    component.confirmPassword = '1234567';
    component.email = 'test@test.com';
    component.direccion = 'dir';
    component.nif = '1234';

    component.register();

    expect(mockDialogService.alert).toHaveBeenCalledWith('La contraseña debe tener al menos 8 caracteres');
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

});
