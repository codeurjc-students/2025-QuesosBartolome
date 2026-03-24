import { TestBed } from '@angular/core/testing';
import { HttpClientModule } from '@angular/common/http';

import { InvoiceService } from './invoice.service';
import { LoginService } from './login.service';
import { CartService } from './cart.service';
import { OrderService } from './order.service';
import { InvoiceDTO } from '../dto/invoice.dto';
import { OrderDTO } from '../dto/order.dto';
import { Page } from '../dto/page.dto';

describe('InvoiceService (integration)', () => {

	let service: InvoiceService;
	let loginService: LoginService;
	let cartService: CartService;
	let orderService: OrderService;

	beforeEach(() => {
		TestBed.resetTestingModule();

		TestBed.configureTestingModule({
			imports: [HttpClientModule],
			providers: [InvoiceService, LoginService, CartService, OrderService]
		});

		service = TestBed.inject(InvoiceService);
		loginService = TestBed.inject(LoginService);
		cartService = TestBed.inject(CartService);
		orderService = TestBed.inject(OrderService);
	});

	function loginAs(username: string, password: string): Promise<void> {
		return new Promise((resolve, reject) => {
			loginService.login(username, password).subscribe({
				next: () => resolve(),
				error: (err) => reject('Login failed: ' + err.message)
			});
		});
	}

	function createOrderAsUser(username: string, password: string): Promise<OrderDTO> {
		return loginAs(username, password).then(() =>
			new Promise<OrderDTO>((resolve, reject) => {
				cartService.addCheeseToOrder(5, 1, 1).subscribe({
					next: () => {
						orderService.confirmOrder().subscribe({
							next: (order) => resolve(order),
							error: (err) => reject('Failed to confirm order: ' + err.message)
						});
					},
					error: (err) => reject('Failed to add cheese before confirming order: ' + err.message)
				});
			})
		);
	}

	it('should retrieve paginated invoices after admin login', (done) => {
		loginAs('German', 'password123').then(() => {

			service.getAllInvoices(0, 10).subscribe({
				next: (page: Page<InvoiceDTO>) => {
					expect(page).toBeTruthy();
					expect(Array.isArray(page.content)).toBeTrue();
					expect(page.number).toBe(0);
					expect(page.size).toBe(10);
					done();
				},
				error: (err) => {
					fail('Failed to fetch invoices: ' + err.message);
					done();
				}
			});

		}).catch(err => {
			fail(err);
			done();
		});
	});

	it('should create invoice from order and fetch it by id', (done) => {
		createOrderAsUser('Victor', 'password123').then((order) => {

			return loginAs('German', 'password123').then(() => order);

		}).then((order) => {

			const orderRef = { id: order.id } as OrderDTO;

			service.createInvoiceFromOrder(orderRef).subscribe({
				next: (invoice: InvoiceDTO) => {
					expect(invoice).toBeTruthy();
					expect(invoice.id).toBeGreaterThan(0);
					expect(invoice.invNo).toContain('FACT-Q');

					service.getInvoiceById(invoice.id).subscribe({
						next: (fetched: InvoiceDTO) => {
							expect(fetched).toBeTruthy();
							expect(fetched.id).toBe(invoice.id);
							expect(fetched.user).toBeTruthy();
							done();
						},
						error: (err) => {
							fail('Failed to fetch invoice by id: ' + err.message);
							done();
						}
					});
				},
				error: (err) => {
					fail('Failed to create invoice from order: ' + err.message);
					done();
				}
			});

		}).catch(err => {
			fail(err);
			done();
		});
	});
});
