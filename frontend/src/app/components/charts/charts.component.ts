import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, ViewChild, ElementRef } from '@angular/core';
import { forkJoin } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatTooltipModule } from '@angular/material/tooltip';
import { UserService } from '../../service/user.service';
import { UserDTO } from '../../dto/user.dto';
import { InvoiceDTO } from '../../dto/invoice.dto';
import { InvoiceService } from '../../service/invoice.service';

@Component({
  selector: 'app-charts',
  standalone: true,
  imports: [CommonModule, FormsModule, MatFormFieldModule, MatSelectModule, MatButtonToggleModule, MatTooltipModule],
  templateUrl: './charts.component.html',
  styleUrls: ['./charts.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class ChartsComponent implements OnInit {
  currentUser: UserDTO | null = null;
  isAdmin = false;
  loading = true;
  error = '';

  allInvoices: InvoiceDTO[] = [];
  availableYears: number[] = [];
  selectedYear: number | null = null;

  allUsers: UserDTO[] = [];
  selectedUsers: UserDTO[] = [];
  chartMode: 'line' | 'bar' = 'line';

  selectedMonth: number | null = null;
  barData: Array<{ cheeseId?: number; cheeseName: string; income: number; kg: number }> = [];
  barRects: Array<{
    cheeseName: string;
    xIncome: number;
    yIncome: number;
    hIncome: number;
    xKg: number;
    yKg: number;
    hKg: number;
    width: number;
    labelX: number;
    incomeLabel: string;
    kgLabel: string;
  }> = [];

  readonly monthOptions = [
    { value: 1, label: 'Enero' },
    { value: 2, label: 'Febrero' },
    { value: 3, label: 'Marzo' },
    { value: 4, label: 'Abril' },
    { value: 5, label: 'Mayo' },
    { value: 6, label: 'Junio' },
    { value: 7, label: 'Julio' },
    { value: 8, label: 'Agosto' },
    { value: 9, label: 'Septiembre' },
    { value: 10, label: 'Octubre' },
    { value: 11, label: 'Noviembre' },
    { value: 12, label: 'Diciembre' }
  ];

  labels: string[] = [];
  dataPoints: number[] = [];
  chartTitle = 'Ingresos por mes';
  svgWidth = 800;
  svgHeight = 320;
  padding = { top: 20, right: 20, bottom: 40, left: 50 };

  @ViewChild('svgElem', { static: false }) svgRef?: ElementRef<SVGElement>;

  tooltipVisible = false;
  tooltipX = 0;
  tooltipY = 0;
  tooltipText = '';
  barHoverCheese = '';
  barHoverValue = '';
  barHoverColor = '#8B0000';
  barHoverHint = 'Pasa el ratón por una barra para ver el queso y su valor.';

  constructor(
    private userService: UserService,
    private invoiceService: InvoiceService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) { }

  ngOnInit(): void {
    this.checkAdminAccess();
  }

  private checkAdminAccess(): void {
    this.userService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUser = user;
        this.isAdmin = !!user.rols?.includes('ADMIN');

        if (!this.isAdmin) {
          this.router.navigate(['/']);
          return;
        }

        this.loadChartData();
      },
      error: (err) => {
        if (err.status === 401) {
          this.router.navigate(['/auth/login']);
          return;
        }

        if (err.status === 403 || err.status >= 500) {
          this.router.navigate(['/error']);
          return;
        }

        this.router.navigate(['/auth/login']);
      }
    });
  }

  private loadChartData(): void {
    this.loading = true;
    this.error = '';

    forkJoin({
      invoices: this.invoiceService.getAllInvoicesForCharts(),
      users: this.userService.getAllUsersForCharts()
    }).subscribe({
      next: ({ invoices, users }) => {
        this.allInvoices = invoices ?? [];
        this.allUsers = users ?? [];

        const years = this.allInvoices
          .map((invoice) => this.toDate(invoice.invoiceDate)?.getFullYear())
          .filter((year): year is number => year !== undefined && year !== null);

        this.availableYears = Array.from(new Set(years)).sort((a, b) => b - a);
        this.resetSelectorsToDefault();
        this.refreshActiveChart();
        this.loading = false;
        this.cdr.markForCheck();
      },
      error: () => {
        this.error = 'Error cargando datos';
        this.loading = false;
        this.cdr.markForCheck();
      }
    });
  }

  onYearChange(): void {
    this.refreshActiveChart();
  }

  onUsersChange(): void {
  this.refreshActiveChart();
}

  onChartModeChange(mode: 'line' | 'bar'): void {
    this.chartMode = mode;
    this.resetSelectorsToDefault();
    this.barHoverCheese = '';
    this.barHoverValue = '';
    this.barHoverColor = '#8B0000';
    this.refreshActiveChart();
  }

  onMonthChange(): void {
    this.refreshActiveChart();
  }

  private resetSelectorsToDefault(): void {
    this.selectedYear = this.availableYears[0] ?? null;
    this.selectedMonth = 1;
    this.selectedUsers = [];
  }

  private refreshActiveChart(): void {
    if (this.chartMode === 'bar') {
      this.updateBarChart();
      return;
    }

    this.updateChart();
  }

  private updateBarChart(): void {
    this.barData = [];
    this.barRects = [];
    if (!this.selectedYear) return;

    const invoices = this.allInvoices.filter(inv => {
      const d = this.toDate(inv.invoiceDate);
      if (!d) return false;
      if (d.getFullYear() !== this.selectedYear) return false;
      if (this.selectedMonth && (d.getMonth() + 1) !== this.selectedMonth) return false;
      return true;
    });

    let filteredInvoices = invoices;

    if (this.selectedUsers.length > 0) {
      const selectedIds = this.selectedUsers.map((user) => user.id);
      filteredInvoices = filteredInvoices.filter((invoice) => selectedIds.includes(invoice.user.id));
    }

    const map = new Map<string, { cheeseId?: number; cheeseName: string; income: number; kg: number }>();

    for (const inv of filteredInvoices) {
      const items = inv.order?.items ?? [];
      for (const it of items) {
        const key = (it.cheeseId ?? -1) + '::' + it.cheeseName;
        const current = map.get(key) ?? { cheeseId: it.cheeseId, cheeseName: it.cheeseName, income: 0, kg: 0 };
        current.income += Number(it.totalPrice ?? 0);
        current.kg += Number(it.weight ?? 0);
        map.set(key, current);
      }
    }

    this.barData = Array.from(map.values()).sort((a, b) => b.income - a.income);
    const paddingLeft = 20;
    const paddingRight = 20;
    const availableW = this.svgWidth - paddingLeft - paddingRight;
    const groupGap = 18;
    const groupW = this.barData.length > 0 ? (availableW - groupGap * Math.max(this.barData.length - 1, 0)) / this.barData.length : availableW;
    const maxIncome = Math.max(...this.barData.map(d => d.income), 1);
    const maxKg = Math.max(...this.barData.map(d => d.kg), 1);
    const chartH = this.svgHeight - 120; // leave space for labels

    for (let i = 0; i < this.barData.length; i++) {
      const d = this.barData[i];
      const w = groupW / 2 - 4;
      const hIncome = (d.income / maxIncome) * chartH;
      const yIncome = this.svgHeight - 40 - hIncome;
      const xIncome = paddingLeft + i * (groupW + groupGap) + 4;

      const hKg = (d.kg / maxKg) * chartH;
      const yKg = this.svgHeight - 40 - hKg;
      const xKg = paddingLeft + i * (groupW + groupGap) + groupW / 2 + 2;

      this.barRects.push({
        cheeseName: d.cheeseName,
        xIncome,
        yIncome,
        hIncome,
        xKg,
        yKg,
        hKg,
        width: w,
        labelX: xIncome + groupW / 2,
        incomeLabel: `${d.cheeseName} - ${this.formatMoney(d.income)}`,
        kgLabel: `${d.cheeseName} - ${this.formatKg(d.kg)}`
      });
    }

    this.cdr.markForCheck();
  }

  showBarTooltip(cheeseName: string, value: string, color: string): void {
    this.barHoverCheese = cheeseName;
    this.barHoverValue = value;
    this.barHoverColor = color;
    this.cdr.markForCheck();
  }

  hideBarTooltip(): void {
    this.barHoverCheese = '';
    this.barHoverValue = '';
    this.barHoverColor = '#8B0000';
    this.cdr.markForCheck();
  }

  private buildNiceTicks(maxValue: number): number[] {
    const targetSteps = 4;
    if (maxValue <= 0) return [0];
    const rawStep = maxValue / targetSteps;
    const magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
    const normalized = rawStep / magnitude;
    let niceNorm = 1;
    if (normalized <= 1) niceNorm = 1;
    else if (normalized <= 2) niceNorm = 2;
    else if (normalized <= 5) niceNorm = 5;
    else niceNorm = 10;
    const niceStep = niceNorm * magnitude;
    const ticks: number[] = [];
    const maxTick = Math.ceil(maxValue / niceStep) * niceStep;
    for (let v = 0; v <= maxTick; v += niceStep) {
      ticks.push(Math.round(v * 100) / 100);
    }
    return ticks;
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat('es-ES', {
      style: 'currency',
      currency: 'EUR',
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(value);
  }

  formatKg(value: number): string {
    return `${new Intl.NumberFormat('es-ES', {
      minimumFractionDigits: 1,
      maximumFractionDigits: 2
    }).format(value)} kg`;
  }

  private updateChart(): void {
    if (!this.selectedYear) {
      this.labels = [];
      this.dataPoints = [];
      this.chartTitle = 'Ingresos por mes';
      this.cdr.markForCheck();
      return;
    }

    const yearInvoices = this.allInvoices.filter((invoice) => {
      const date = this.toDate(invoice.invoiceDate);
      return !!date && date.getFullYear() === this.selectedYear;
    });

    let filteredInvoices = yearInvoices;

    if (this.selectedUsers.length > 0) {
      const selectedIds = this.selectedUsers.map(u => u.id);
      filteredInvoices = filteredInvoices.filter(inv =>
        selectedIds.includes(inv.user.id)
      );
    }

    if (!filteredInvoices.length) {
      this.labels = [];
      this.dataPoints = [];
      this.chartTitle = this.buildChartTitle();
      this.cdr.markForCheck();
      return;
    }

    const monthlyTotals = new Map<number, number>();

    for (const invoice of filteredInvoices) {
      const date = this.toDate(invoice.invoiceDate);
      if (!date || invoice.totalPrice == null) continue;

      const month = date.getMonth() + 1;
      const current = monthlyTotals.get(month) ?? 0;
      monthlyTotals.set(month, current + Number(invoice.totalPrice));
    }

    this.labels = this.monthOptions.map((m) => m.label);
    this.dataPoints = this.monthOptions.map((m) => this.roundToTwo(monthlyTotals.get(m.value) ?? 0));
    this.chartTitle = this.buildChartTitle();
    this.cdr.markForCheck();
  }

  getLinePath(): string {
    if (!this.dataPoints || this.dataPoints.length === 0) return '';
    const w = this.svgWidth - this.padding.left - this.padding.right;
    const h = this.svgHeight - this.padding.top - this.padding.bottom;
    const max = Math.max(...this.dataPoints, 0);
    const min = 0;
    const len = this.dataPoints.length;
    const stepX = len > 1 ? w / (len - 1) : w;
    const points: string[] = [];
    for (let i = 0; i < len; i++) {
      const x = this.padding.left + i * stepX;
      const v = this.dataPoints[i];
      const y = this.padding.top + (max - v) * (h / (max - min || 1));
      points.push(`${x},${y}`);
    }
    return 'M ' + points.join(' L ');
  }

  getAreaPath(): string {
    if (!this.dataPoints || this.dataPoints.length === 0) return '';
    const w = this.svgWidth - this.padding.left - this.padding.right;
    const h = this.svgHeight - this.padding.top - this.padding.bottom;
    const max = Math.max(...this.dataPoints, 0);
    const min = 0;
    const len = this.dataPoints.length;
    const stepX = len > 1 ? w / (len - 1) : w;
    const points: Array<[number, number]> = [];
    for (let i = 0; i < len; i++) {
      const x = this.padding.left + i * stepX;
      const v = this.dataPoints[i];
      const y = this.padding.top + (max - v) * (h / (max - min || 1));
      points.push([x, y]);
    }
    const bottomY = this.padding.top + h;

    const pathParts: string[] = [];
    pathParts.push(`M ${points[0][0]},${points[0][1]}`);
    for (let i = 1; i < points.length; i++) {
      pathParts.push(`L ${points[i][0]},${points[i][1]}`);
    }
    pathParts.push(`L ${points[points.length - 1][0]},${bottomY}`);
    pathParts.push(`L ${points[0][0]},${bottomY}`);
    pathParts.push('Z');
    return pathParts.join(' ');
  }


  getPointCoords(index: number): [number, number] {
    const w = this.svgWidth - this.padding.left - this.padding.right;
    const h = this.svgHeight - this.padding.top - this.padding.bottom;
    const max = Math.max(...this.dataPoints, 0);
    const min = 0;
    const len = this.dataPoints.length;
    const stepX = len > 1 ? w / (len - 1) : w;
    const x = this.padding.left + index * stepX;
    const v = this.dataPoints[index] ?? 0;
    const y = this.padding.top + (max - v) * (h / (max - min || 1));
    return [x, y];
  }

  getYAxisTicks(): number[] {
    const max = Math.max(...this.dataPoints, 0);
    const targetSteps = 4;
    if (max <= 0) return [0, 1];
    const rawStep = max / targetSteps;
    const magnitude = Math.pow(10, Math.floor(Math.log10(rawStep)));
    const normalized = rawStep / magnitude;
    let niceNorm = 1;
    if (normalized <= 1) niceNorm = 1;
    else if (normalized <= 2) niceNorm = 2;
    else if (normalized <= 5) niceNorm = 5;
    else niceNorm = 10;
    const niceStep = niceNorm * magnitude;
    const ticks: number[] = [];
    const maxTick = Math.ceil(max / niceStep) * niceStep;
    for (let v = 0; v <= maxTick; v += niceStep) {
      ticks.push(Math.round(v));
    }
    return ticks;
  }

  getYPositionForValue(value: number): number {
    const h = this.svgHeight - this.padding.top - this.padding.bottom;
    const max = Math.max(...this.dataPoints, 0);
    const min = 0;
    return this.padding.top + (max - value) * (h / (max - min || 1));
  }

  showTooltip(evt: MouseEvent, index: number): void {
    const coords = this.getPointCoords(index);
    const rect = this.svgRef?.nativeElement.getBoundingClientRect();
    let left = coords[0];
    let top = coords[1] - 12;
    const minX = 40;
    const maxX = this.svgWidth - 40;
    const minY = 12;
    const maxY = this.svgHeight - 20;
    if (left < minX) left = minX;
    if (left > maxX) left = maxX;
    if (top < minY) top = minY;
    if (top > maxY) top = maxY;
    this.tooltipX = left;
    this.tooltipY = top;
    const monthLabel = this.monthOptions[index]?.label ?? (index + 1).toString();
    const formatted = new Intl.NumberFormat('es-ES', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(this.dataPoints[index] ?? 0);
    this.tooltipText = `Mes: ${monthLabel} — Ingresos: ${formatted} €`;
    this.tooltipVisible = true;
    this.cdr.markForCheck();
  }

  hideTooltip(): void {
    this.tooltipVisible = false;
    this.cdr.markForCheck();
  }


  private buildChartTitle(): string {
    if (!this.selectedYear) {
      return 'Ingresos por mes';
    }

    return `Ingresos - ${this.selectedYear}`;
  }

  private toDate(value: string): Date | null {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  private roundToTwo(value: number): number {
    return Math.round(value * 100) / 100;
  }

}
