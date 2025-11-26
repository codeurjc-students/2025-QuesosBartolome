import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

import { UserPageComponent } from './userPage.component';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';

describe('UserPageComponent (unit)', () => {
  let component: UserPageComponent;
  let fixture: ComponentFixture<UserPageComponent>;
  let mockUserService: jasmine.SpyObj<UserService>;

  beforeEach(async () => {
    mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser', 'getUserImage']);

    await TestBed.configureTestingModule({
      imports: [UserPageComponent],
      providers: [
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(UserPageComponent);
    component = fixture.componentInstance;
  });

  it('should load user and image correctly', () => {
    const mockUser: UserDTO = {
      id: 1,
      name: 'Juan',
      password: '12345678',
      gmail: 'juan@example.com',
      direction: 'Calle Falsa 123',
      nif: '12345678A'
    };

    const mockBlob = new Blob(['fake'], { type: 'image/png' });

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));
    mockUserService.getUserImage.and.returnValue(of(mockBlob));

    fixture.detectChanges(); // ngOnInit

    expect(mockUserService.getCurrentUser).toHaveBeenCalled();
    expect(component.user.name).toBe('Juan');
    expect(mockUserService.getUserImage).toHaveBeenCalledWith(1);
    expect(component.imageUrl).toContain('blob:'); // URL.createObjectURL
  });

  it('should fallback to default avatar when image is empty', () => {
    const mockUser: UserDTO = {
      id: 2,
      name: 'Ana',
      password: '12345678',
      gmail: 'ana@example.com',
      direction: 'Calle Luna 2',
      nif: '87654321B'
    };

    const emptyBlob = new Blob([], { type: 'image/png' });

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));
    mockUserService.getUserImage.and.returnValue(of(emptyBlob));

    fixture.detectChanges();

    expect(component.imageUrl).toBe('assets/avatar-default.png');
  });

  it('should fallback to default avatar when getUserImage fails', () => {
    const mockUser: UserDTO = {
      id: 3,
      name: 'Luis',
      password: '12345678',
      gmail: 'luis@example.com',
      direction: 'Calle Mayor 3',
      nif: '11223344C'
    };

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));
    mockUserService.getUserImage.and.returnValue(throwError(() => new Error('404')));

    fixture.detectChanges();

    expect(component.imageUrl).toBe('assets/avatar-default.png');
  });

});
