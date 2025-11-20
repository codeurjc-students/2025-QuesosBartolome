import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { RegisterComponent } from './register.component';
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';


describe('RegisterComponent (unit)', () => {

  let component: RegisterComponent;
  let fixture: ComponentFixture<RegisterComponent>;
  let mockLoginService: jasmine.SpyObj<LoginService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {

    mockLoginService = jasmine.createSpyObj('LoginService', ['register']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [RegisterComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter }
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
    spyOn(window, 'alert');

    component.nombre = '';
    component.password = '123';
    component.confirmPassword = '123';
    component.email = 'test@test.com';
    component.direccion = 'dir';
    component.nif = '1234';

    component.register();

    expect(window.alert).toHaveBeenCalledWith('Todos los campos son obligatorios');
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

  it('should alert if passwords do not match', () => {
    spyOn(window, 'alert');

    component.nombre = 'Juan';
    component.password = '123';
    component.confirmPassword = '456';
    component.email = 'test@test.com';
    component.direccion = 'dir';
    component.nif = '1234';

    component.register();

    expect(window.alert).toHaveBeenCalledWith('Las contraseñas no coinciden');
    expect(mockLoginService.register).not.toHaveBeenCalled();
  });

});
