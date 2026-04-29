import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';

import { ReviewService } from './review.service';
import { LoginService } from './login.service';
import { ReviewDTO } from '../dto/review.dto';
import { Page } from '../dto/page.dto';

describe('ReviewService (integration)', () => {

  let service: ReviewService;
  let loginService: LoginService;

  beforeAll(() => {
    jasmine.DEFAULT_TIMEOUT_INTERVAL = 15000;
  });

  beforeEach(() => {
    TestBed.resetTestingModule();

    TestBed.configureTestingModule({
      imports: [HttpClientModule],
      providers: [ReviewService, LoginService]
    });

    service = TestBed.inject(ReviewService);
    loginService = TestBed.inject(LoginService);
  });

  function loginAs(username: string, password: string): Promise<void> {
    return new Promise((resolve, reject) => {
      loginService.login(username, password).subscribe({
        next: () => resolve(),
        error: (err) => reject('Login failed: ' + err.message)
      });
    });
  }

  it('should create a review after login', (done) => {
    loginAs('Victor', 'password123').then(() => {

      service.createReview(5, 'Review desde test', 5).subscribe({
        next: (review: ReviewDTO) => {
          expect(review).toBeTruthy();
          expect(review.id).toBeDefined();
          expect(review.rating).toBe(5);
          expect(review.comment).toBe('Review desde test');
          done();
        },
        error: (err) => {
          fail('Failed to create review: ' + err.message);
          done();
        }
      });

    }).catch(err => {
      fail(err);
      done();
    });
  });

  it('should retrieve reviews by cheese id', (done) => {
    loginAs('Victor', 'password123').then(() => {

      service.getReviewsByCheeseId(5, 0, 3).subscribe({
        next: (page: Page<ReviewDTO>) => {
          expect(page).toBeTruthy();
          expect(Array.isArray(page.content)).toBeTrue();
          expect(page.number).toBe(0);
          expect(page.size).toBe(3);
          done();
        },
        error: (err) => {
          fail('Failed to fetch reviews by cheese: ' + err.message);
          done();
        }
      });

    }).catch(err => {
      fail(err);
      done();
    });
  });

  it('should retrieve reviews by user id', (done) => {
    loginAs('Victor', 'password123').then(() => {

      service.getReviewsByUserId(1, 0, 10).subscribe({
        next: (page: Page<ReviewDTO>) => {
          expect(page).toBeTruthy();
          expect(Array.isArray(page.content)).toBeTrue();
          expect(page.number).toBe(0);
          expect(page.size).toBe(10);
          done();
        },
        error: (err) => {
          fail('Failed to fetch reviews by user: ' + err.message);
          done();
        }
      });

    }).catch(err => {
      fail(err);
      done();
    });
  });

  it('should retrieve a review by id', (done) => {
    loginAs('Victor', 'password123').then(() => {

      service.createReview(4, 'Review para buscar', 5).subscribe({
        next: (created: ReviewDTO) => {

          service.getReviewById(created.id).subscribe({
            next: (found: ReviewDTO) => {
              expect(found).toBeTruthy();
              expect(found.id).toBe(created.id);
              expect(found.comment).toBe('Review para buscar');
              done();
            },
            error: (err) => {
              fail('Failed to fetch review by id: ' + err.message);
              done();
            }
          });

        },
        error: (err) => {
          fail('Failed to create review before fetching: ' + err.message);
          done();
        }
      });

    }).catch(err => {
      fail(err);
      done();
    });
  });

  it('should delete a review after login', (done) => {
    loginAs('Victor', 'password123').then(() => {

      service.createReview(3, 'Review para borrar', 5).subscribe({
        next: (created: ReviewDTO) => {

          service.deleteReview(created.id).subscribe({
            next: () => {
              // Try to fetch it again → should fail with 404
              service.getReviewById(created.id).subscribe({
                next: () => {
                  fail('Review should not exist after deletion');
                  done();
                },
                error: () => {
                  // Expected
                  done();
                }
              });
            },
            error: (err) => {
              fail('Failed to delete review: ' + err.message);
              done();
            }
          });

        },
        error: (err) => {
          fail('Failed to create review before deletion: ' + err.message);
          done();
        }
      });

    }).catch(err => {
      fail(err);
      done();
    });
  });

});
