import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';
import { By } from '@angular/platform-browser';

import { InvoicesComponent } from './invoices.component';
import { InvoiceService } from '../../service/invoice.service';
import { InvoiceDTO } from '../../dto/invoice.dto';

describe('InvoicesComponent', () => {
  let component: InvoicesComponent;
  let fixture: ComponentFixture<InvoicesComponent>;
  let invoiceServiceSpy: jasmine.SpyObj<InvoiceService>;
  let routerSpy: jasmine.SpyObj<Router>;

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
    invoiceServiceSpy = jasmine.createSpyObj('InvoiceService', ['getAllInvoices']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);

    invoiceServiceSpy.getAllInvoices.and.returnValue(of({
      content: mockInvoices,
      totalPages: 2,
      totalElements: 2,
      size: 10,
      number: 0,
      first: true,
      last: false,
      numberOfElements: 2
    }));

    await TestBed.configureTestingModule({
      imports: [InvoicesComponent],
      providers: [
        { provide: InvoiceService, useValue: invoiceServiceSpy },
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

  it('should load invoices on init', () => {
    expect(invoiceServiceSpy.getAllInvoices).toHaveBeenCalledWith(0, 10);
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
    component.totalPages = 3;
    invoiceServiceSpy.getAllInvoices.calls.reset();

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
    invoiceServiceSpy.getAllInvoices.calls.reset();

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

  it('should alert when downloadInvoice is called', () => {
    const alertSpy = spyOn(window, 'alert');

    component.downloadInvoice(mockInvoices[0]);

    expect(alertSpy).toHaveBeenCalledWith('Descarga de factura FACT-Q26/1 no implementada todavía.');
  });
});
