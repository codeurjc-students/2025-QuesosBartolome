import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';

import { InvoicesComponent } from './invoices.component';
import { InvoiceService } from '../../service/invoice.service';
import { InvoiceDTO } from '../../dto/invoice.dto';
import { UserService } from '../../service/user.service';
import { DialogService } from '../../service/dialog.service';

describe('InvoicesComponent', () => {
  let component: InvoicesComponent;
  let fixture: ComponentFixture<InvoicesComponent>;
  let invoiceServiceSpy: jasmine.SpyObj<InvoiceService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let dialogServiceSpy: jasmine.SpyObj<DialogService>;

  const mockInvoices: InvoiceDTO[] = [
    {
      id: 1,
      invNo: 'FACT-Q26/1',
      user: { id: 1, name: 'Victor' },
      order: {} as any,
      taxableBase: 100,
      totalPrice: 104,
      invoiceDate: '2026-03-23T10:00:00'
    },
    {
      id: 2,
      invNo: 'FACT-Q26/2',
      user: { id: 2, name: 'German' },
      order: {} as any,
      taxableBase: 50,
      totalPrice: 52,
      invoiceDate: '2026-03-23T11:00:00'
    }
  ];

  beforeEach(async () => {
    invoiceServiceSpy = jasmine.createSpyObj('InvoiceService', ['getAllInvoices', 'downloadInvoicePdf']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    userServiceSpy = jasmine.createSpyObj('UserService', ['getCurrentUser', 'getMyInvoices', 'downloadMyInvoicePdf']);
    dialogServiceSpy = jasmine.createSpyObj('DialogService', ['alert']);
    userServiceSpy.getCurrentUser.and.returnValue(of({ rols: ['ADMIN'] } as any));

    invoiceServiceSpy.getAllInvoices.and.returnValues(
      of({
        content: [mockInvoices[0]],
        totalPages: 2,
        totalElements: 2,
        size: 10,
        number: 0,
        first: true,
        last: false,
        numberOfElements: 1
      }),
      of({
        content: mockInvoices,
        totalPages: 2,
        totalElements: 2,
        size: 10,
        number: 1,
        first: false,
        last: true,
        numberOfElements: 2
      })
    );
    invoiceServiceSpy.downloadInvoicePdf.and.returnValue(of(new Blob(['pdf'], { type: 'application/pdf' })));
    userServiceSpy.getMyInvoices.and.returnValue(of({
      content: [mockInvoices[0]],
      totalPages: 1,
      totalElements: 1,
      size: 10,
      number: 0,
      first: true,
      last: true,
      numberOfElements: 1
    }));
    userServiceSpy.downloadMyInvoicePdf.and.returnValue(of(new Blob(['pdf'], { type: 'application/pdf' })));

    await TestBed.configureTestingModule({
      imports: [InvoicesComponent],
      providers: [
        { provide: InvoiceService, useValue: invoiceServiceSpy },
        { provide: UserService, useValue: userServiceSpy },
        { provide: DialogService, useValue: dialogServiceSpy },
        { provide: Router, useValue: routerSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(InvoicesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load invoices on init and go to last page', () => {
    expect(invoiceServiceSpy.getAllInvoices).toHaveBeenCalledWith(0, 10);
    expect(invoiceServiceSpy.getAllInvoices).toHaveBeenCalledWith(1, 10);
    expect(component.currentPage).toBe(1);
    expect(component.invoices.length).toBe(2);
  });

  it('should render invoice rows', () => {
    const rows = fixture.debugElement.queryAll(By.css('.invoices-row'));
    expect(rows.length).toBe(2);

    const text = rows.map(r => r.nativeElement.textContent);
    expect(text.join(' ')).toContain('FACT-Q26/1');
    expect(text.join(' ')).toContain('Victor');
    expect(text.join(' ')).toContain('FACT-Q26/2');
    expect(text.join(' ')).toContain('German');
  });

  it('should show empty state when no invoices and not loading', () => {
    component.invoices = [];
    component.loading = false;
    fixture.detectChanges();

    const empty = fixture.debugElement.query(By.css('.empty-state'));
    expect(empty).toBeTruthy();
    expect(empty.nativeElement.textContent).toContain('No hay facturas para mostrar');
  });

  it('should navigate to error page when loadInvoices fails with 500', () => {
    invoiceServiceSpy.getAllInvoices.and.returnValue(throwError(() => ({ status: 500 })));

    component.loadInvoices();

    expect(component.loading).toBeFalse();
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/error']);
  });

  it('should not navigate to error page when loadInvoices fails with 404', () => {
    invoiceServiceSpy.getAllInvoices.and.returnValue(throwError(() => ({ status: 404 })));

    component.loadInvoices();

    expect(component.loading).toBeFalse();
    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('should go to next page when there are more pages', () => {
    component.currentPage = 0;
    component.totalPages = 2;
    invoiceServiceSpy.getAllInvoices.calls.reset();
    invoiceServiceSpy.getAllInvoices.and.returnValue(of({
      content: mockInvoices,
      totalPages: 2,
      totalElements: 2,
      size: 10,
      number: 1,
      first: false,
      last: true,
      numberOfElements: 2
    }));

    component.nextPage();

    expect(component.currentPage).toBe(1);
    expect(invoiceServiceSpy.getAllInvoices).toHaveBeenCalledWith(1, component.pageSize);
  });

  it('should not go past last page', () => {
    component.currentPage = 1;
    component.totalPages = 2;
    invoiceServiceSpy.getAllInvoices.calls.reset();

    component.nextPage();

    expect(component.currentPage).toBe(1);
    expect(invoiceServiceSpy.getAllInvoices).not.toHaveBeenCalled();
  });

  it('should go to previous page when currentPage is greater than 0', () => {
    component.currentPage = 1;
    component.totalPages = 2;
    invoiceServiceSpy.getAllInvoices.calls.reset();
    invoiceServiceSpy.getAllInvoices.and.returnValue(of({
      content: [mockInvoices[0]],
      totalPages: 2,
      totalElements: 2,
      size: 10,
      number: 0,
      first: true,
      last: false,
      numberOfElements: 1
    }));

    component.prevPage();

    expect(component.currentPage).toBe(0);
    expect(invoiceServiceSpy.getAllInvoices).toHaveBeenCalledWith(0, component.pageSize);
  });

  it('should not go to negative page', () => {
    component.currentPage = 0;
    invoiceServiceSpy.getAllInvoices.calls.reset();

    component.prevPage();

    expect(component.currentPage).toBe(0);
    expect(invoiceServiceSpy.getAllInvoices).not.toHaveBeenCalled();
  });

  it('should load user invoices when current user is USER', () => {
    userServiceSpy.getCurrentUser.and.returnValue(of({ id: 7, rols: ['USER'] } as any));
    invoiceServiceSpy.getAllInvoices.calls.reset();

    component.ngOnInit();

    expect(userServiceSpy.getMyInvoices).toHaveBeenCalledWith(7, 0, 10);
    expect(invoiceServiceSpy.getAllInvoices).not.toHaveBeenCalled();
    expect(component.invoices.length).toBe(1);
    expect(component.invoices[0].user.name).toBe('Victor');
  });

  it('should navigate to login when user has no id and is not admin', () => {
    component.currentUser = { rols: ['USER'] } as any;
    routerSpy.navigate.calls.reset();
    invoiceServiceSpy.getAllInvoices.calls.reset();
    userServiceSpy.getMyInvoices.calls.reset();

    component.loadInvoices();

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
    expect(invoiceServiceSpy.getAllInvoices).not.toHaveBeenCalled();
    expect(userServiceSpy.getMyInvoices).not.toHaveBeenCalled();
  });

  it('should use admin download endpoint for ADMIN user', () => {
    const invoice = mockInvoices[0];

    component.currentUser = { id: 1, rols: ['ADMIN'] } as any;
    component.downloadInvoice(invoice);

    expect(invoiceServiceSpy.downloadInvoicePdf).toHaveBeenCalledWith(invoice.id);
    expect(userServiceSpy.downloadMyInvoicePdf).not.toHaveBeenCalled();
  });

  it('should use user download endpoint for USER user', () => {
    const invoice = mockInvoices[0];

    component.currentUser = { id: 7, rols: ['USER'] } as any;
    component.downloadInvoice(invoice);

    expect(userServiceSpy.downloadMyInvoicePdf).toHaveBeenCalledWith(7, invoice.id);
    expect(invoiceServiceSpy.downloadInvoicePdf).not.toHaveBeenCalled();
  });

  it('should show not found dialog when download returns 404', () => {
    const invoice = mockInvoices[0];

    component.currentUser = { id: 1, rols: ['ADMIN'] } as any;
    invoiceServiceSpy.downloadInvoicePdf.and.returnValue(throwError(() => ({ status: 404 })));

    component.downloadInvoice(invoice);

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith('Factura no encontrada.');
  });

  it('should show generic dialog when download returns non-404 error', () => {
    const invoice = mockInvoices[0];

    component.currentUser = { id: 1, rols: ['ADMIN'] } as any;
    invoiceServiceSpy.downloadInvoicePdf.and.returnValue(throwError(() => ({ status: 500 })));

    component.downloadInvoice(invoice);

    expect(dialogServiceSpy.alert).toHaveBeenCalledWith('Error al descargar la factura. Intenta de nuevo.');
  });

  it('should navigate to login on download when user has no id and is not admin', () => {
    component.currentUser = { rols: ['USER'] } as any;
    routerSpy.navigate.calls.reset();

    component.downloadInvoice(mockInvoices[0]);

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/auth/login']);
    expect(invoiceServiceSpy.downloadInvoicePdf).not.toHaveBeenCalled();
    expect(userServiceSpy.downloadMyInvoicePdf).not.toHaveBeenCalled();
  });

});
