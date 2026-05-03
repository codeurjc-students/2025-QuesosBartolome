import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { DebugElement } from '@angular/core';
import { By } from '@angular/platform-browser';

import { UserPageComponent } from './userPage.component';
import { UserService } from '../../service/user.service';
import { ReviewService } from '../../service/review.service';
import { UserDTO } from '../../dto/user.dto';
import { ReviewDTO } from '../../dto/review.dto';
import { UserBasicDTO } from '../../dto/userBasic.dto';
import { CheeseBasicDTO } from '../../dto/cheeseBasic.dto';
import { Page } from '../../dto/page.dto';
import { ActivatedRoute, Router } from '@angular/router';
import { DialogService } from '../../service/dialog.service';

describe('UserPageComponent (unit)', () => {

  let component: UserPageComponent;
  let fixture: ComponentFixture<UserPageComponent>;

  let mockUserService: jasmine.SpyObj<UserService>;
  let mockReviewService: jasmine.SpyObj<ReviewService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockRoute: any;
  let mockDialogService: jasmine.SpyObj<DialogService>;

  const mockUserBasic: UserBasicDTO = { id: 1, name: 'Juan' };
  const mockCheeseBasic: CheeseBasicDTO = { id: 10, name: 'Semicurado', price: 17.5 };

  const mockReview: ReviewDTO = {
    id: 1,
    rating: 5,
    comment: "Muy bueno",
    user: mockUserBasic,
    cheese: mockCheeseBasic
  };

  function mockPage<T>(content: T[]): Page<T> {
    return {
      content,
      number: 0,
      size: content.length,
      totalPages: 1,
      totalElements: content.length,
      first: true,
      last: true,
      numberOfElements: content.length
    };
  }

  beforeEach(async () => {
    mockUserService = jasmine.createSpyObj('UserService', [
      'getCurrentUser',
      'getUserById',
      'getUserImage'
    ]);

    mockReviewService = jasmine.createSpyObj('ReviewService', [
      'getReviewsByUserId',
      'deleteReview'
    ]);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockDialogService = jasmine.createSpyObj('DialogService', ['alert', 'confirm']);
    mockDialogService.confirm.and.callFake((_message, onConfirm) => onConfirm());

    mockRoute = { snapshot: { paramMap: new Map() } };
    mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));
    mockReviewService.getReviewsByUserId.and.returnValue(of(mockPage([])));

    await TestBed.configureTestingModule({
      imports: [UserPageComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: ReviewService, useValue: mockReviewService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute },
        { provide: DialogService, useValue: mockDialogService }
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
      nif: '12345678A',
      rols: ['USER'],
      banned: false
    };

    const mockBlob = new Blob(['fake'], { type: 'image/png' });

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));
    mockUserService.getUserImage.and.returnValue(of(mockBlob));

    fixture.detectChanges();

    expect(mockUserService.getCurrentUser).toHaveBeenCalled();
    expect(component.user.name).toBe('Juan');
    expect(mockUserService.getUserImage).toHaveBeenCalledWith(1);
    expect(component.imageUrl).toContain('blob:');
  });

  it('should fallback to default avatar when image is empty', () => {
    const mockUser: UserDTO = {
      id: 2,
      name: 'Ana',
      password: '12345678',
      gmail: 'ana@example.com',
      direction: 'Calle Luna 2',
      nif: '87654321B',
      rols: ['USER'],
      banned: false
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
      nif: '11223344C',
      rols: ['USER'],
      banned: false
    };

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));
    mockUserService.getUserImage.and.returnValue(throwError(() => new Error('404')));

    fixture.detectChanges();

    expect(component.imageUrl).toBe('assets/avatar-default.png');
  });

  it('should load reviews when user is not admin', () => {
    const mockUser: UserDTO = {
      id: 1,
      name: 'Juan',
      password: '123',
      gmail: 'a@a.com',
      direction: 'x',
      nif: 'y',
      rols: ['USER'],
      banned: false
    };

    mockUserService.getCurrentUser.and.returnValue(of(mockUser));
    mockUserService.getUserImage.and.returnValue(of(new Blob(['fake'])));

    mockReviewService.getReviewsByUserId.and.returnValue(of(mockPage([mockReview])));

    fixture.detectChanges();

    expect(component.reviews.length).toBe(1);
    expect(component.totalReviews).toBe(1);
  });

  it('should NOT load reviews when user is admin', () => {
    const mockAdmin: UserDTO = {
      id: 1,
      name: 'Admin',
      password: '123',
      gmail: 'a@a.com',
      direction: 'x',
      nif: 'y',
      rols: ['ADMIN'],
      banned: false
    };

    mockUserService.getCurrentUser.and.returnValue(of(mockAdmin));
    mockUserService.getUserImage.and.returnValue(of(new Blob(['fake'])));

    fixture.detectChanges();

    expect(mockReviewService.getReviewsByUserId).not.toHaveBeenCalled();
  });

  it('should show delete button only for own profile', () => {
    component.isOwnProfile = true;
    component.reviews = [mockReview];

    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('.btn-delete'))).toBeTruthy();

    component.isOwnProfile = false;
    fixture.detectChanges();

    expect(fixture.debugElement.query(By.css('.btn-delete'))).toBeNull();
  });

  it('should delete review successfully', () => {
    component.user = { ...component.user, id: 1 };
    component.currentUser = { ...component.user };
    component.reviews = [mockReview];

    mockReviewService.deleteReview.and.returnValue(of(void 0));
    mockReviewService.getReviewsByUserId.and.returnValue(
      of({ content: [], number: 0, size: 0, totalPages: 0, totalElements: 0, first: true, last: true, numberOfElements: 0 })
    );

    component.deleteReview(1);

    expect(mockReviewService.deleteReview).toHaveBeenCalledWith(1);
  });

  it('should alert when deleteReview fails', () => {
    component.user = { ...component.user, id: 1 };
    component.currentUser = { ...component.user };

    mockReviewService.deleteReview.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.deleteReview(1);

    expect(mockDialogService.alert).toHaveBeenCalledWith('No se pudo eliminar la reseña');
  });

  it('should navigate to cheese when clicking a review cheese', () => {
    component.reviews = [mockReview];

    component.goToCheese(10);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/cheeses', 10]);
  });

  it('should enter edit mode when editProfile() is called', () => {
    component.user = {
      id: 1, name: 'Juan', password: '', gmail: 'a@a.com',
      direction: 'x', nif: 'y', rols: [], banned: false
    };

    component.editProfile();

    expect(component.isEditMode).toBeTrue();
    expect(component.editSnapshot).toEqual(component.user);
  });

  it('should restore snapshot when cancelEdit() is called', () => {
    component.user = {
      id: 1, name: 'Juan', password: '', gmail: 'a@a.com',
      direction: 'x', nif: 'y', rols: [], banned: false
    };

    component.editProfile();
    component.user.name = 'NuevoNombre';

    component.cancelEdit();

    expect(component.user.name).toBe('Juan');
    expect(component.isEditMode).toBeFalse();
  });

  it('should call updateUser when confirmEdit() is executed', () => {
    mockUserService.updateUser = jasmine.createSpy().and.returnValue(of({
      id: 1, name: 'Juan', gmail: 'nuevo@mail.com', direction: 'x', nif: 'y', password: '', rols: [], banned: false
    }));

    mockUserService.updateUserImage = jasmine.createSpy().and.returnValue(of(void 0));
    spyOn(component, 'reloadAfterEdit');

    component.user = {
      id: 1, name: 'Juan', password: '', gmail: 'nuevo@mail.com',
      direction: 'x', nif: 'y', rols: [], banned: false
    };

    component.confirmEdit();

    expect(mockUserService.updateUser).toHaveBeenCalledWith(1, {
      name: 'Juan',
      gmail: 'nuevo@mail.com',
      direction: 'x',
      nif: 'y'
    });
    expect(component.reloadAfterEdit).toHaveBeenCalled();
  });

  it('should call updateUserImage if a new image is selected', () => {
    const fakeFile = new File(['123'], 'avatar.png', { type: 'image/png' });
    component.selectedImageFile = fakeFile;

    mockUserService.updateUser = jasmine.createSpy().and.returnValue(of(component.user));
    mockUserService.updateUserImage = jasmine.createSpy().and.returnValue(of(void 0));
    spyOn(component, 'reloadAfterEdit');

    component.user = {
      id: 1, name: 'Juan', password: '', gmail: 'a@a.com',
      direction: 'x', nif: 'y', rols: [], banned: false
    };

    component.confirmEdit();

    expect(mockUserService.updateUserImage).toHaveBeenCalledWith(1, fakeFile);
    expect(component.reloadAfterEdit).toHaveBeenCalled();
  });

  it('should alert when updateUser fails', () => {
    mockUserService.updateUser = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.user = {
      id: 1, name: 'Juan', password: '', gmail: 'a@a.com',
      direction: 'x', nif: 'y', rols: [], banned: false
    };

    component.confirmEdit();

    expect(mockDialogService.alert).toHaveBeenCalledWith('No se pudo guardar el perfil. Inténtalo de nuevo.');
  });

  it('should enter password mode when changePassword() is called', () => {
    component.changePassword();

    expect(component.isPasswordMode).toBeTrue();
    expect(component.passwordForm.currentPassword).toBe('');
  });

  it('should show error if password fields are incomplete', () => {
    component.passwordForm = {
      currentPassword: '',
      newPassword: '',
      confirmPassword: ''
    };

    component.confirmPasswordChange();

    expect(component.passwordError).toBe('Debes completar todos los campos.');
  });

  it('should show error if new passwords do not match', () => {
    component.passwordForm = {
      currentPassword: 'old',
      newPassword: '12345678',
      confirmPassword: '87654321'
    };

    component.confirmPasswordChange();

    expect(component.passwordError).toBe('Las nuevas contraseñas no coinciden.');
  });

  it('should show error if new password is too short', () => {
    component.passwordForm = {
      currentPassword: 'old',
      newPassword: '123',
      confirmPassword: '123'
    };

    component.confirmPasswordChange();

    expect(component.passwordError).toBe('La nueva contraseña debe tener al menos 8 caracteres.');
  });

  it('should call changePassword when confirmPasswordChange() is valid', () => {
    mockUserService.changePassword = jasmine.createSpy().and.returnValue(of(void 0));

    component.user.id = 1;
    const expectedPayload = {
      currentPassword: 'oldPassword',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123'
    };
    component.passwordForm = { ...expectedPayload };

    component.confirmPasswordChange();

    expect(mockUserService.changePassword).toHaveBeenCalledWith(1, expectedPayload);
    expect(mockDialogService.alert).toHaveBeenCalledWith('Contraseña actualizada correctamente.');
  });

  it('should show error if changePassword fails', () => {
    mockUserService.changePassword = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 400 }))
    );

    component.user.id = 1;
    component.passwordForm = {
      currentPassword: 'wrong',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123'
    };

    component.confirmPasswordChange();

    expect(component.passwordError).toBe('No se pudo cambiar la contraseña. Revisa la contraseña actual.');
  });

  it('should display banned badge and notice when user is banned', () => {
    const bannedUser: UserDTO = {
      id: 1,
      name: 'Juan',
      password: '',
      gmail: 'juan@test.com',
      direction: 'Calle Falsa',
      nif: '12345678A',
      rols: ['USER'],
      banned: true
    };

    mockUserService.getCurrentUser.and.returnValue(of(bannedUser));
    mockUserService.getUserImage.and.returnValue(of(new Blob(['fake'])));

    fixture.detectChanges();

    const badge = fixture.debugElement.query(By.css('.banned-badge'));
    expect(badge).toBeTruthy();
    expect(badge.nativeElement.textContent.trim()).toBe('BANEADO');

    const notice = fixture.debugElement.query(By.css('.banned-notice'));
    expect(notice).toBeTruthy();
    expect(notice.nativeElement.textContent.trim()).toBe('Este usuario esta baneado.');

    const avatarBox = fixture.debugElement.query(By.css('.avatar-box'));
    expect(avatarBox.nativeElement.classList).toContain('avatar-banned');
  });

  it('should navigate to /auth/login when getCurrentUser fails and there is no userId in route', () => {
    fixture.detectChanges();
    expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
  });

  it('should navigate to /error when loadUserById fails with status >= 500', () => {
    mockUserService.getUserById = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.loadUserById(99);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should navigate to /cheeses when loadUserById fails with status < 500', () => {
    mockUserService.getUserById = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 404 }))
    );

    component.loadUserById(99);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/cheeses']);
  });

  it('should navigate to /error when loadReviews fails with status >= 500', () => {
    mockReviewService.getReviewsByUserId.and.returnValue(throwError(() => ({ status: 500 })));

    component.loadReviews(1, 0);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should show alert when loadReviews fails with status < 500', () => {
    mockReviewService.getReviewsByUserId.and.returnValue(throwError(() => ({ status: 404 })));

    component.loadReviews(1, 0);

    expect(mockDialogService.alert).toHaveBeenCalledWith('No se han podido cargar las reseñas.');
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should not call loadReviews when nextPage is called at last page', () => {
    component.currentPage = 2;
    component.totalPages = 3;
    mockReviewService.getReviewsByUserId.calls.reset();

    component.nextPage();

    expect(mockReviewService.getReviewsByUserId).not.toHaveBeenCalled();
  });

  it('should call loadReviews with next page when nextPage is valid', () => {
    component.user = { ...component.user, id: 1 };
    component.currentPage = 0;
    component.totalPages = 3;
    mockReviewService.getReviewsByUserId.and.returnValue(of(mockPage([])));

    component.nextPage();

    expect(mockReviewService.getReviewsByUserId).toHaveBeenCalledWith(1, 1, 3);
  });

  it('should not call loadReviews when previousPage is called at page 0', () => {
    component.currentPage = 0;
    mockReviewService.getReviewsByUserId.calls.reset();

    component.previousPage();

    expect(mockReviewService.getReviewsByUserId).not.toHaveBeenCalled();
  });

  it('should call loadReviews with previous page when previousPage is valid', () => {
    component.user = { ...component.user, id: 1 };
    component.currentPage = 2;
    component.totalPages = 3;
    mockReviewService.getReviewsByUserId.and.returnValue(of(mockPage([])));

    component.previousPage();

    expect(mockReviewService.getReviewsByUserId).toHaveBeenCalledWith(1, 1, 3);
  });

  it('should not navigate when deleteReview fails with status < 500', () => {
    component.user = { ...component.user, id: 1 };
    mockReviewService.deleteReview.and.returnValue(throwError(() => ({ status: 404 })));

    component.deleteReview(1);

    expect(mockDialogService.alert).toHaveBeenCalledWith('No se pudo eliminar la reseña');
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should only alert and not navigate when confirmEdit fails with status < 500', () => {
    mockUserService.updateUser = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 400 }))
    );

    component.user = { id: 1, name: 'Juan', password: '', gmail: 'a@a.com', direction: 'x', nif: 'y', rols: [], banned: false };

    component.confirmEdit();

    expect(mockDialogService.alert).toHaveBeenCalledWith('No se pudo guardar el perfil. Inténtalo de nuevo.');
    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });

  it('should navigate to /error when updateUserImage fails with status >= 500', () => {
    mockUserService.updateUser = jasmine.createSpy().and.returnValue(of(component.user));
    mockUserService.updateUserImage = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 500 }))
    );
    spyOn(component, 'reloadAfterEdit');

    component.user = { id: 1, name: 'Juan', password: '', gmail: 'a@a.com', direction: 'x', nif: 'y', rols: [], banned: false };
    component.selectedImageFile = new File(['abc'], 'img.png');

    component.confirmEdit();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
    expect(component.reloadAfterEdit).toHaveBeenCalled();
  });

  it('should navigate to /error when confirmPasswordChange fails with status >= 500', () => {
    mockUserService.changePassword = jasmine.createSpy().and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.user.id = 1;
    component.passwordForm = {
      currentPassword: 'old',
      newPassword: 'newPassword123',
      confirmPassword: 'newPassword123'
    };

    component.confirmPasswordChange();

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should do nothing when onAvatarClick is called and not in edit mode', () => {
    component.isEditMode = false;
    const clickSpy = spyOn(document, 'getElementById').and.callThrough();

    component.onAvatarClick();

    expect(clickSpy).not.toHaveBeenCalled();
  });

  it('should cancelEdit restoring null snapshot without crashing', () => {
    component.editSnapshot = null;
    component.user.name = 'Changed';

    component.cancelEdit();

    expect(component.isEditMode).toBeFalse();
  });

});

