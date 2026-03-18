import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { LoginComponent } from './login.component';
import { LoginService } from '../../service/login.service';
import { Router } from '@angular/router';

describe('LoginComponent (unit)', () => {

  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let mockLoginService: jasmine.SpyObj<LoginService>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {

    mockLoginService = jasmine.createSpyObj('LoginService', ['login']);
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        { provide: LoginService, useValue: mockLoginService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should call login service and navigate on success', () => {

    component.username = 'Victor';
    component.password = 'password123';

    mockLoginService.login.and.returnValue(of({ status: 'SUCCESS' }));

    component.login();

    expect(mockLoginService.login).toHaveBeenCalledWith('Victor', 'password123');
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });

  it('should NOT navigate when login service returns an error', () => {

    mockLoginService.login.and.returnValue(throwError(() => ({ error: 'Invalid credentials' })));

    component.username = 'Juan';
    component.password = 'wrong';

    component.login();

    expect(mockLoginService.login).toHaveBeenCalled();
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });


  it('should show backend error message when provided', () => {

    spyOn(window, 'alert');

    mockLoginService.login.and.returnValue(
      throwError(() => ({
        status: 403,
        error: { message: 'No puedes iniciar sesion: tu cuenta esta baneada.' }
      }))
    );

    component.username = 'German';
    component.password = 'password123';

    component.login();

    expect(window.alert).toHaveBeenCalledWith('No puedes iniciar sesion: tu cuenta esta baneada.');
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should show generic error message when backend message is missing', () => {

    spyOn(window, 'alert');

    mockLoginService.login.and.returnValue(
      throwError(() => ({
        status: 401,
        error: {}
      }))
    );

    component.username = 'User';
    component.password = 'wrong';

    component.login();

    expect(window.alert).toHaveBeenCalledWith('Credenciales incorrectas.');
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

});
