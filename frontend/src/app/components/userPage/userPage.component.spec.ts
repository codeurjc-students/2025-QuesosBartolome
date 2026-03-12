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

describe('UserPageComponent (unit)', () => {

  let component: UserPageComponent;
  let fixture: ComponentFixture<UserPageComponent>;

  let mockUserService: jasmine.SpyObj<UserService>;
  let mockReviewService: jasmine.SpyObj<ReviewService>;
  let mockRouter: jasmine.SpyObj<Router>;
  let mockRoute: any;

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

    mockRoute = { snapshot: { paramMap: new Map() } };
    mockUserService.getCurrentUser.and.returnValue(throwError(() => ({ status: 401 })));
    mockReviewService.getReviewsByUserId.and.returnValue(of(mockPage([])));

    await TestBed.configureTestingModule({
      imports: [UserPageComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: ReviewService, useValue: mockReviewService },
        { provide: Router, useValue: mockRouter },
        { provide: ActivatedRoute, useValue: mockRoute }
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
      rols: ['USER']
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
      rols: ['USER']
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
      rols: ['USER']
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
      rols: ['USER']
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
      rols: ['ADMIN']
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
    spyOn(window, 'confirm').and.returnValue(true);

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
    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');

    component.user = { ...component.user, id: 1 };
    component.currentUser = { ...component.user };

    mockReviewService.deleteReview.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    component.deleteReview(1);

    expect(window.alert).toHaveBeenCalledWith('No se pudo eliminar la reseña');
  });

  it('should navigate to cheese when clicking a review cheese', () => {
    component.reviews = [mockReview];

    component.goToCheese(10);

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/cheeses', 10]);
  });

});
