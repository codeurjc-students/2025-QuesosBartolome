import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ClientsComponent } from './clients.component';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';

describe('ClientsComponent (unit)', () => {
  let component: ClientsComponent;
  let fixture: ComponentFixture<ClientsComponent>;

  let mockUserService: jasmine.SpyObj<UserService>;

  const mockUsers: UserDTO[] = [
    { id: 1, name: 'Victor', password: '', gmail: 'victor@example.com', direction: '123 Main St', nif: '12345678A', rols: ['USER'] },
    { id: 2, name: 'German', password: '', gmail: 'german@example.com', direction: '456 Oak Ave', nif: '87654321B', rols: ['ADMIN'] }
  ];

  const mockPage = {
    content: mockUsers,
    totalPages: 1,
    totalElements: 2,
    size: 10,
    number: 0,
    first: true,
    last: true,
    numberOfElements: 2
  };

  beforeEach(async () => {
    mockUserService = jasmine.createSpyObj('UserService', ['getAllUsers', 'getUserImage']);

    // Default: return page of users
    mockUserService.getAllUsers.and.returnValue(of(mockPage));
    mockUserService.getUserImage.and.returnValue(of(new Blob())); // empty blob -> fallback avatar

    await TestBed.configureTestingModule({
      imports: [ClientsComponent],
      providers: [
        { provide: UserService, useValue: mockUserService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges(); // triggers ngOnInit
  });

  it('should load and render users', () => {
    const debug: DebugElement = fixture.debugElement;

    // Check number of rows
    const rows = debug.queryAll(By.css('.users-row'));
    expect(rows.length).toBe(2);

    // Check names
    const names = rows.map(r => r.query(By.css('span:nth-child(3)')).nativeElement.textContent.trim());
    expect(names).toContain('Victor');
    expect(names).toContain('German');
  });

  it('should render default avatar images', () => {
    const debug: DebugElement = fixture.debugElement;

    const rows = debug.queryAll(By.css('.users-row'));
    rows.forEach(row => {
      const img = row.query(By.css('img.avatar')).nativeElement as HTMLImageElement;
      const src = img.src;
      // Accept either blob: or fallback URL
      expect(src).toMatch(/blob:|avatar-default/);
    });
  });

  it('should handle error when getAllUsers fails', () => {
    // Simulate error from service
    mockUserService.getAllUsers.and.returnValue(throwError(() => new Error('Error loading users')));

    // Reset users before calling loadUsers
    component.users = [];
    component.loadUsers();

    expect(component.users.length).toBe(0);
  });

  it('should call service with correct page when nextPage is called', () => {
    component.nextPage();
    expect(mockUserService.getAllUsers).toHaveBeenCalledWith(1, component.pageSize);
  });

  it('should decrease page when prevPage is called', () => {
    component.currentPage = 1;
    component.prevPage();
    expect(component.currentPage).toBe(0);
    expect(mockUserService.getAllUsers).toHaveBeenCalledWith(0, component.pageSize);
  });

  it('should not go to negative page', () => {
    component.currentPage = 0;
    component.prevPage();
    expect(component.currentPage).toBe(0);
  });

  it('should call loadUserImage for each user', () => {
    expect(mockUserService.getUserImage).toHaveBeenCalledTimes(2);
    expect(mockUserService.getUserImage).toHaveBeenCalledWith(1);
    expect(mockUserService.getUserImage).toHaveBeenCalledWith(2);
  });
});
