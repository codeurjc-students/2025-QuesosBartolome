import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ClientsComponent } from './clients.component';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';
import { By } from '@angular/platform-browser';
import { DebugElement } from '@angular/core';
import { Router } from '@angular/router';

describe('ClientsComponent (unit)', () => {
  let component: ClientsComponent;
  let fixture: ComponentFixture<ClientsComponent>;

  let mockUserService: jasmine.SpyObj<UserService>;
  let mockRouter: jasmine.SpyObj<Router>;

  const mockUsers: UserDTO[] = [
    { id: 1, name: 'Victor', password: '', gmail: 'victor@example.com', direction: '123 Main St', nif: '12345678A', rols: ['USER'], banned: false },
    { id: 2, name: 'German', password: '', gmail: 'german@example.com', direction: '456 Oak Ave', nif: '87654321B', rols: ['ADMIN'], banned: true }
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
    mockUserService = jasmine.createSpyObj('UserService', [
      'getAllUsers',
      'getUserImage',
      'toggleUserBan'
    ]);

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);

    const freshUsers = mockUsers.map(u => ({ ...u }));
    const freshPage = { ...mockPage, content: freshUsers };
    mockUserService.getAllUsers.and.returnValue(of(freshPage));
    mockUserService.getUserImage.and.returnValue(of(new Blob()));

    await TestBed.configureTestingModule({
      imports: [ClientsComponent],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: Router, useValue: mockRouter }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(ClientsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should load and render users', () => {
    const rows = fixture.debugElement.queryAll(By.css('.users-row'));
    expect(rows.length).toBe(2);

    const names = rows.map(r => r.query(By.css('span:nth-child(3)')).nativeElement.textContent.trim());
    expect(names).toContain('Victor');
    expect(names).toContain('German');
  });

  it('should render default avatar images', () => {
    const rows = fixture.debugElement.queryAll(By.css('.users-row'));
    rows.forEach(row => {
      const img = row.query(By.css('img.avatar')).nativeElement as HTMLImageElement;
      expect(img.src).toMatch(/blob:|avatar-default/);
    });
  });

  it('should handle error when getAllUsers fails', () => {
    mockUserService.getAllUsers.and.returnValue(throwError(() => new Error('Error loading users')));

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

  it('should display correct status text for each user', async () => {
    await fixture.whenStable();
    fixture.detectChanges();

    const rows = fixture.debugElement.queryAll(By.css('.users-row'));

    const status1 = rows[0].query(By.css('.status-pill')).nativeElement.textContent.trim();
    const status2 = rows[1].query(By.css('.status-pill')).nativeElement.textContent.trim();

    expect(status1).toBe('ACTIVO');
    expect(status2).toBe('BANEADO');
  });

  it('should apply banned CSS classes when user is banned', () => {
    const row = fixture.debugElement.queryAll(By.css('.users-row'))[1];
    const img = row.query(By.css('img.avatar'));

    expect(row.nativeElement.classList).toContain('user-banned');
    expect(img.nativeElement.classList).toContain('avatar-banned');
  });

  it('should show correct ban/unban button text', async () => {
    await fixture.whenStable();
    fixture.detectChanges();

    const buttons = fixture.debugElement.queryAll(By.css('.btn-ban'));

    expect(buttons[0].nativeElement.textContent.trim()).toBe('Banear');
    expect(buttons[1].nativeElement.textContent.trim()).toBe('Desbanear');
  });



  it('should call toggleUserBan when ban button is clicked', () => {
    const user = component.users[0];
    mockUserService.toggleUserBan.and.returnValue(of({ ...user, banned: true }));

    spyOn(window, 'confirm').and.returnValue(true);

    const button = fixture.debugElement.queryAll(By.css('.btn-ban'))[0];
    button.triggerEventHandler('click', new Event('click'));

    expect(mockUserService.toggleUserBan).toHaveBeenCalledWith(user.id);
    expect(user.banned).toBeTrue();
  });

  it('should NOT call toggleUserBan if confirm is cancelled', () => {
    const user = component.users[0];
    spyOn(window, 'confirm').and.returnValue(false);

    const button = fixture.debugElement.queryAll(By.css('.btn-ban'))[0];
    button.triggerEventHandler('click', new Event('click'));

    expect(mockUserService.toggleUserBan).not.toHaveBeenCalled();
  });

  it('should handle error when toggleUserBan fails', () => {
    const user = component.users[0];

    mockUserService.toggleUserBan.and.returnValue(
      throwError(() => ({ status: 500 }))
    );

    spyOn(window, 'confirm').and.returnValue(true);
    spyOn(window, 'alert');

    const button = fixture.debugElement.queryAll(By.css('.btn-ban'))[0];
    button.triggerEventHandler('click', new Event('click'));

    expect(window.alert).toHaveBeenCalled();
  });

  it('should navigate to user profile when row is clicked', () => {
    const row = fixture.debugElement.queryAll(By.css('.users-row'))[0];
    row.triggerEventHandler('click', new Event('click'));

    expect(mockRouter.navigate).toHaveBeenCalledWith(['/user', 1]);
  });

  it('should NOT navigate when clicking ban button', () => {
    spyOn(window, 'confirm').and.returnValue(true);
    mockUserService.toggleUserBan.and.returnValue(of({ ...component.users[0], banned: true }));

    const button = fixture.debugElement.queryAll(By.css('.btn-ban'))[0];
    button.triggerEventHandler('click', new Event('click'));

    expect(mockRouter.navigate).not.toHaveBeenCalled();
  });
});
