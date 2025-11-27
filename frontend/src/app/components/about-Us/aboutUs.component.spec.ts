import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AboutUsComponent } from './aboutUs.component';
import { NO_ERRORS_SCHEMA } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';

describe('AboutUsComponent', () => {
  let component: AboutUsComponent;
  let fixture: ComponentFixture<AboutUsComponent>;
  let sanitizer: DomSanitizer;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AboutUsComponent],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(AboutUsComponent);
    component = fixture.componentInstance;
    sanitizer = TestBed.inject(DomSanitizer);
  });

  it('should generate safe map url on init', () => {
    const spy = spyOn<any>(component, 'generateSafeMapUrl').and.callThrough();

    component.ngOnInit();
    
    expect(spy).toHaveBeenCalledWith('Aldeonte, Segovia, España');
    expect(component.safeMapUrl).toBeTruthy();
  });
});
