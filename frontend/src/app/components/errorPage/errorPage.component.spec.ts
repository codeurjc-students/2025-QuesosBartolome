import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ErrorPageComponent } from './errorPage.component';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';

describe('ErrorPageComponent (unit)', () => {
  let component: ErrorPageComponent;
  let fixture: ComponentFixture<ErrorPageComponent>;
  let mockRouter: jasmine.SpyObj<Router>;

  beforeEach(async () => {
    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      imports: [ErrorPageComponent],
      providers: [
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ErrorPageComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should display the error message', () => {
    const messageEl = fixture.debugElement.query(By.css('.error-message')).nativeElement;
    expect(messageEl.textContent.trim()).toBe('Se ha producido un error en la página web.');
  });

  it('should have a button to go home', () => {
    const button = fixture.debugElement.query(By.css('.btn-home')).nativeElement;
    expect(button).toBeTruthy();
    expect(button.textContent.trim()).toBe('Volver a inicio');
  });

  it('should navigate home when button is clicked', () => {
    const button = fixture.debugElement.query(By.css('.btn-home'));
    button.triggerEventHandler('click');

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
  });
});
