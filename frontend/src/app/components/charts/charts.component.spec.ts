import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';

import { ChartsComponent } from './charts.component';
import { UserService } from '../../service/user.service';
import { InvoiceService } from '../../service/invoice.service';

describe('ChartsComponent (unit)', () => {
	let component: ChartsComponent;
	let fixture: ComponentFixture<ChartsComponent>;
	let mockUserService: jasmine.SpyObj<UserService>;
	let mockInvoiceService: jasmine.SpyObj<InvoiceService>;
	let mockRouter: jasmine.SpyObj<Router>;

	const invoicesMock: any[] = [
		{
			id: 1,
			invNo: 'INV-1',
			user: { id: 1, name: 'Admin' },
			order: {
				id: 11,
				items: [
					{ cheeseId: 10, cheeseName: 'Manchego', totalPrice: 20, weight: 1.2 },
					{ cheeseId: 20, cheeseName: 'Azul', totalPrice: 40, weight: 2.4 }
				]
			},
			taxableBase: 60,
			totalPrice: 60,
			invoiceDate: '2025-01-10T00:00:00Z'
		},
		{
			id: 2,
			invNo: 'INV-2',
			user: { id: 2, name: 'Riaza' },
			order: {
				id: 12,
				items: [
					{ cheeseId: 10, cheeseName: 'Manchego', totalPrice: 30, weight: 1.5 }
				]
			},
			taxableBase: 30,
			totalPrice: 30,
			invoiceDate: '2024-02-12T00:00:00Z'
		}
	];

	const usersMock: any[] = [
		{ id: 1, name: 'Admin', rols: ['ADMIN'] },
		{ id: 2, name: 'Tienda Artesanal de Riaza', rols: ['USER'] }
	];

	beforeEach(async () => {
		mockUserService = jasmine.createSpyObj('UserService', ['getCurrentUser', 'getAllUsersForCharts']);
		mockInvoiceService = jasmine.createSpyObj('InvoiceService', ['getAllInvoicesForCharts']);
		mockRouter = jasmine.createSpyObj('Router', ['navigate']);

		mockUserService.getCurrentUser.and.returnValue(of({ id: 99, rols: ['ADMIN'] } as any));
		mockUserService.getAllUsersForCharts.and.returnValue(of(usersMock as any));
		mockInvoiceService.getAllInvoicesForCharts.and.returnValue(of(invoicesMock as any));

		await TestBed.configureTestingModule({
			imports: [ChartsComponent],
			providers: [
				{ provide: UserService, useValue: mockUserService },
				{ provide: InvoiceService, useValue: mockInvoiceService },
				{ provide: Router, useValue: mockRouter }
			]
		}).compileComponents();

		fixture = TestBed.createComponent(ChartsComponent);
		component = fixture.componentInstance;
		fixture.detectChanges();
	});

	it('should create and load chart data for admin', () => {
		expect(component).toBeTruthy();
		expect(component.isAdmin).toBeTrue();
		expect(component.loading).toBeFalse();
		expect(component.availableYears).toEqual([2025, 2024]);
		expect(component.selectedYear).toBe(2025);
		expect(component.labels.length).toBe(12);
		expect(component.dataPoints.length).toBe(12);
	});

	it('should redirect to home when user is not admin', () => {
		mockUserService.getCurrentUser.and.returnValue(of({ id: 1, rols: ['USER'] } as any));
		const localFixture = TestBed.createComponent(ChartsComponent);
		localFixture.detectChanges();

		expect(mockRouter.navigate).toHaveBeenCalledWith(['/']);
	});

	it('should redirect to login when getCurrentUser fails', () => {
		mockUserService.getCurrentUser.and.returnValue(throwError(() => new Error('401')));
		const localFixture = TestBed.createComponent(ChartsComponent);
		localFixture.detectChanges();

		expect(mockRouter.navigate).toHaveBeenCalledWith(['/auth/login']);
	});

	it('should set error message when chart data loading fails', () => {
		mockInvoiceService.getAllInvoicesForCharts.and.returnValue(throwError(() => new Error('boom')));
		const localFixture = TestBed.createComponent(ChartsComponent);
		const localComponent = localFixture.componentInstance;
		localFixture.detectChanges();

		expect(localComponent.loading).toBeFalse();
		expect(localComponent.error).toBe('Error cargando datos');
	});

	it('should refresh line chart when year and users change', () => {
		spyOn(console, 'log');
		component.selectedYear = 2025;
		component.selectedUsers = [{ id: 1 } as any];

		component.onYearChange();
		component.onUsersChange();

		expect(component.chartTitle).toBe('Ingresos - 2025');
		expect(component.dataPoints[0]).toBe(60);
		expect(console.log).toHaveBeenCalled();
	});

	it('should switch to bar chart mode and rebuild bar data', () => {
		component.onChartModeChange('bar');

		expect(component.chartMode).toBe('bar');
		expect(component.selectedMonth).toBe(1);
		expect(component.selectedUsers).toEqual([]);
		expect(component.barData.length).toBeGreaterThan(0);
		expect(component.barRects.length).toBe(component.barData.length);
		expect(component.barIncomeTicks.length).toBeGreaterThan(0);
		expect(component.barKgTicks.length).toBeGreaterThan(0);
	});

	it('should filter bar chart by selected month and selected users', () => {
		component.chartMode = 'bar';
		component.selectedYear = 2025;
		component.selectedMonth = 1;
		component.selectedUsers = [{ id: 1 } as any];

		component.onMonthChange();

		expect(component.barData.length).toBe(2);
		expect(component.barData[0].income).toBeGreaterThanOrEqual(component.barData[1].income);
	});

	it('should return empty line chart when selected year is null', () => {
		component.chartMode = 'line';
		component.selectedYear = null;

		component.onYearChange();

		expect(component.labels).toEqual([]);
		expect(component.dataPoints).toEqual([]);
		expect(component.chartTitle).toBe('Ingresos por mes');
	});

	it('should keep line chart empty when filter removes all invoices', () => {
		component.chartMode = 'line';
		component.selectedYear = 2025;
		component.selectedUsers = [{ id: 777 } as any];

		component.onUsersChange();

		expect(component.labels).toEqual([]);
		expect(component.dataPoints).toEqual([]);
		expect(component.chartTitle).toBe('Ingresos - 2025');
	});

	it('should return empty bar chart when selected year is null', () => {
		component.chartMode = 'bar';
		component.selectedYear = null;

		component.onMonthChange();

		expect(component.barData).toEqual([]);
		expect(component.barRects).toEqual([]);
	});

	it('should show and hide bar tooltip', () => {
		component.showBarTooltip('Manchego', 'Manchego - 10,00 EUR', '#2f8f2f');

		expect(component.barHoverCheese).toBe('Manchego');
		expect(component.barHoverValue).toContain('Manchego');
		expect(component.barHoverColor).toBe('#2f8f2f');

		component.hideBarTooltip();

		expect(component.barHoverCheese).toBe('');
		expect(component.barHoverValue).toBe('');
		expect(component.barHoverColor).toBe('#8B0000');
	});

	it('should generate svg paths and points', () => {
		component.dataPoints = [10, 20, 30];

		const linePath = component.getLinePath();
		const areaPath = component.getAreaPath();
		const pointsAttr = component.getPointsAttr();

		expect(linePath.startsWith('M ')).toBeTrue();
		expect(areaPath.endsWith('Z')).toBeTrue();
		expect(pointsAttr.split(' ').length).toBe(3);
	});

	it('should return empty svg paths when there is no data', () => {
		component.dataPoints = [];

		expect(component.getLinePath()).toBe('');
		expect(component.getAreaPath()).toBe('');
		expect(component.getPointsAttr()).toBe('');
	});

	it('should compute point coords, ticks and y-position', () => {
		component.dataPoints = [0, 50, 100];

		const coords = component.getPointCoords(1);
		const ticks = component.getYAxisTicks();
		const y = component.getYPositionForValue(50);

		expect(coords.length).toBe(2);
		expect(ticks.length).toBeGreaterThan(1);
		expect(y).toBeGreaterThan(component.padding.top);
	});

	it('should return default ticks when max value is zero', () => {
		component.dataPoints = [0, 0];

		expect(component.getYAxisTicks()).toEqual([0, 1]);
	});

	it('should show and hide line tooltip with bounded position', () => {
		component.dataPoints = [100, 0];
		component.svgWidth = 100;
		component.svgHeight = 100;

		component.showTooltip({} as MouseEvent, 0);

		expect(component.tooltipVisible).toBeTrue();
		expect(component.tooltipX).toBeGreaterThanOrEqual(40);
		expect(component.tooltipY).toBeGreaterThanOrEqual(12);
		expect(component.tooltipText).toContain('Mes: Enero');

		component.hideTooltip();
		expect(component.tooltipVisible).toBeFalse();
	});

	it('should format money and kg values', () => {
		const formattedMoney = component.formatMoney(1234.5);
		const formattedKg = component.formatKg(12.345);

		expect(formattedMoney).toContain('€');
		expect(formattedKg).toContain('kg');
	});

	it('should switch back to line mode and clear bar tooltip state', () => {
		component.chartMode = 'bar';
		component.barHoverCheese = 'Azul';
		component.barHoverValue = 'x';
		component.barHoverColor = '#2f8f2f';

		component.onChartModeChange('line');

		expect(component.chartMode).toBe('line');
		expect(component.barHoverCheese).toBe('');
		expect(component.barHoverValue).toBe('');
		expect(component.barHoverColor).toBe('#8B0000');
	});

	it('should evaluate buildNiceTicks branches', () => {
		const buildNiceTicks = (component as any).buildNiceTicks.bind(component);

		expect(buildNiceTicks(0)).toEqual([0]);
		expect(buildNiceTicks(1).length).toBeGreaterThan(1);
		expect(buildNiceTicks(9).length).toBeGreaterThan(1);
		expect(buildNiceTicks(40).length).toBeGreaterThan(1);
		expect(buildNiceTicks(800).length).toBeGreaterThan(1);
	});

	it('should parse dates and return null for invalid date', () => {
		const toDate = (component as any).toDate.bind(component);

		expect(toDate('2025-01-01T00:00:00Z')).toEqual(jasmine.any(Date));
		expect(toDate('not-a-date')).toBeNull();
	});

	it('should round numbers to two decimals', () => {
		const roundToTwo = (component as any).roundToTwo.bind(component);

		expect(roundToTwo(1.234)).toBe(1.23);
		expect(roundToTwo(1.235)).toBe(1.24);
	});

	it('should use month fallback text in tooltip when month index is out of range', () => {
		component.dataPoints = [10, 20];

		component.showTooltip({} as MouseEvent, 99);

		expect(component.tooltipVisible).toBeTrue();
		expect(component.tooltipText).toContain('Mes: 100');
	});

	it('should clamp tooltip coordinates to max bounds', () => {
		component.dataPoints = [0, 100];
		component.svgWidth = 120;
		component.svgHeight = 80;

		component.showTooltip({} as MouseEvent, 1);

		expect(component.tooltipX).toBeLessThanOrEqual(component.svgWidth - 40);
		expect(component.tooltipY).toBeLessThanOrEqual(component.svgHeight - 20);
	});

	it('should build line and area paths for single point', () => {
		component.dataPoints = [25];

		expect(component.getLinePath()).toContain('M ');
		expect(component.getAreaPath()).toContain('Z');
		expect(component.getPointsAttr()).toContain(',');
	});

	it('should include whole year in bar chart when month is null', () => {
		component.chartMode = 'bar';
		component.selectedYear = 2025;
		component.selectedMonth = null;
		component.selectedUsers = [];

		component.onMonthChange();

		expect(component.barData.length).toBeGreaterThan(0);
	});

	it('should ignore invoices with null total price in line chart', () => {
		component.allInvoices = [
			{
				id: 1,
				invNo: 'INV-X',
				user: { id: 1, name: 'Admin' },
				order: { id: 1, items: [] },
				taxableBase: 0,
				totalPrice: null,
				invoiceDate: '2025-01-05T00:00:00Z'
			} as any,
			{
				id: 2,
				invNo: 'INV-Y',
				user: { id: 1, name: 'Admin' },
				order: { id: 2, items: [] },
				taxableBase: 10,
				totalPrice: 10,
				invoiceDate: '2025-01-06T00:00:00Z'
			} as any
		];
		component.selectedYear = 2025;
		component.selectedUsers = [];
		component.chartMode = 'line';

		component.onYearChange();

		expect(component.dataPoints[0]).toBe(10);
	});

  it('should fallback to empty arrays when services return null data', () => {
    mockUserService.getAllUsersForCharts.and.returnValue(of(null as any));
    mockInvoiceService.getAllInvoicesForCharts.and.returnValue(of(null as any));

    const localFixture = TestBed.createComponent(ChartsComponent);
    const localComponent = localFixture.componentInstance;
    localFixture.detectChanges();

    expect(localComponent.allInvoices).toEqual([]);
    expect(localComponent.allUsers).toEqual([]);
    expect(localComponent.availableYears).toEqual([]);
    expect(localComponent.selectedYear).toBeNull();
  });

  it('should handle invoices without order items in bar mode', () => {
    component.allInvoices = [
      {
        id: 3,
        invNo: 'INV-Z',
        user: { id: 1, name: 'Admin' },
        order: null,
        taxableBase: 0,
        totalPrice: 0,
        invoiceDate: '2025-03-01T00:00:00Z'
      } as any
    ];
    component.chartMode = 'bar';
    component.selectedYear = 2025;
    component.selectedMonth = null;
    component.selectedUsers = [];

    component.onMonthChange();

    expect(component.barData).toEqual([]);
    expect(component.barRects).toEqual([]);
  });

  it('should use fallback denominator branches when all datapoints are zero', () => {
    component.dataPoints = [0];

    const coord = component.getPointCoords(0);
    const yPos = component.getYPositionForValue(0);

    expect(coord[0]).toBeGreaterThanOrEqual(component.padding.left);
    expect(yPos).toBeGreaterThanOrEqual(component.padding.top);
  });
});
