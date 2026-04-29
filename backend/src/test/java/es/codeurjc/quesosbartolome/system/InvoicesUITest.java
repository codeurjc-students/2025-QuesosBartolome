package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import es.codeurjc.quesosbartolome.model.Order;
import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
public class InvoicesUITest {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private OrderRepository orderRepository;

	private WebDriver driver;
	private WebDriverWait wait;

	@BeforeEach
	public void setup() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");
		options.addArguments("--window-size=1920,1080");
		options.addArguments("--no-sandbox");
		options.addArguments("--disable-dev-shm-usage");
		options.addArguments("--disable-gpu");
		options.addArguments("--remote-allow-origins=*");
		options.addArguments("--ignore-certificate-errors");
		options.setAcceptInsecureCerts(true);

		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	}

	@AfterEach
	public void teardown() {
		if (driver != null) {
			driver.quit();
		}
	}

	private void login(String username, String password) {
		driver.get("http://localhost:4200/");

		WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
		loginBtn.click();

		WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("input[name='username']")));
		WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

		usernameInput.sendKeys(username);
		passwordInput.sendKeys(password);

		WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
		submitButton.click();

		Alert alert = SeleniumDialogHelper.waitForDialog(wait);
		alert.accept();
		wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.xpath("//button[contains(.,'Cerrar Sesi\u00f3n')]")));
	}

	private void logout() {
		driver.get("http://localhost:4200/");
		WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//button[contains(.,'Cerrar Sesión')]")));
		wait.until(ExpectedConditions.elementToBeClickable(logoutBtn)).click();
	}

	private void createOrderAsUser() {
		driver.get("http://localhost:4200/cheeses/2");

		WebElement boxesInput = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
		boxesInput.sendKeys("1");

		WebElement addButton = wait.until(
				ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
		new Actions(driver).moveToElement(addButton).pause(200).click().perform();

		Alert addAlert = SeleniumDialogHelper.waitForDialog(wait);
		addAlert.accept();

		driver.get("http://localhost:4200/myorder");

		WebElement confirmBtn = wait.until(
				ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
		confirmBtn.click();

		Alert orderAlert = SeleniumDialogHelper.waitForDialog(wait);
		assertTrue(orderAlert.getText().contains("Pedido realizado correctamente"));
		orderAlert.accept();
	}

	private Long getLatestOrderIdForUser(String username) {
		Long userId = userRepository.findByName(username)
				.map(user -> user.getId())
				.orElseThrow(() -> new AssertionError("User not found: " + username));

		return orderRepository
				.findByUserIdOrderByOrderDateDesc(userId, org.springframework.data.domain.Pageable.unpaged())
				.stream()
				.findFirst()
				.map(Order::getId)
				.orElseThrow(() -> new AssertionError("No order found for user " + username));
	}

	private void processFirstPendingOrderForUserAsAdmin(String username) {
		Long orderId = getLatestOrderIdForUser(username);
		driver.get("http://localhost:4200/orders/" + orderId + "/preview");

		WebElement previewCard = wait.until(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".preview-card")));
		assertTrue(previewCard.getText().contains("Vista previa del pedido"));

		WebElement confirmBtn = wait.until(
				ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.btn-confirm")));
		confirmBtn.click();

		Alert confirmDialog = SeleniumDialogHelper.waitForDialog(wait);
		assertTrue(confirmDialog.getText().contains("Confirmar"));
		confirmDialog.accept();

		Alert invoiceAlert = SeleniumDialogHelper.waitForDialog(wait);
		assertTrue(invoiceAlert.getText().contains("Factura creada correctamente"));
		invoiceAlert.accept();
	}

	private void openInvoicesPageAsUser() {
		driver.get("http://localhost:4200/invoices");
		wait.until(ExpectedConditions.urlContains("/invoices"));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".invoices-card")));
	}

	private List<WebElement> waitForInvoiceRowsWithRetry() {
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				wait.until(driver -> !driver.findElements(By.cssSelector(".invoices-row")).isEmpty());
				List<WebElement> rows = driver.findElements(By.cssSelector(".invoices-row"));
				if (!rows.isEmpty()) {
					return rows;
				}
			} catch (TimeoutException ignored) {
				// Retry by refreshing because Angular data loading can be delayed in headless
				// mode.
			}
			driver.navigate().refresh();
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".invoices-card")));
		}
		return driver.findElements(By.cssSelector(".invoices-row"));
	}

	@Test
	public void testAdminSeesInvoiceAfterProcessingOrder() {
		String username = "Victor";

		login(username, "password123");
		createOrderAsUser();
		logout();

		login("German", "password123");
		processFirstPendingOrderForUserAsAdmin(username);

		driver.get("http://localhost:4200/invoices");

		List<WebElement> invoiceRows = wait.until(
				ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".invoices-row")));
		assertFalse(invoiceRows.isEmpty(), "Admin should see at least one invoice");

		boolean foundUserInvoice = invoiceRows.stream().anyMatch(row -> {
			String rowText = row.getText();
			return rowText.contains(username) && rowText.contains("FACT-Q");
		});

		assertTrue(foundUserInvoice, "Invoice for processed order should appear in invoices section");
	}

	@Test
	public void testUserCanSeeOnlyOwnInvoices() {
		String username = "Victor";

		login(username, "password123");
		createOrderAsUser();
		logout();

		login("German", "password123");
		processFirstPendingOrderForUserAsAdmin(username);
		logout();

		login(username, "password123");
		openInvoicesPageAsUser();

		List<WebElement> invoiceRows = waitForInvoiceRowsWithRetry();
		assertFalse(invoiceRows.isEmpty(), "User should see at least one own invoice");
		assertTrue(invoiceRows.stream().allMatch(row -> row.getText().contains(username)),
				"User invoices list should only contain own invoices");
		assertTrue(invoiceRows.stream().allMatch(row -> !row.findElements(By.cssSelector(".btn-download")).isEmpty()),
				"Each invoice row should include download button");
	}

	@Test
	public void testUserCanDownloadOwnInvoiceFromInvoicesPage() {
		String username = "Victor";

		login(username, "password123");
		createOrderAsUser();
		logout();

		login("German", "password123");
		processFirstPendingOrderForUserAsAdmin(username);
		logout();

		login(username, "password123");
		openInvoicesPageAsUser();

		WebElement invoiceRow = waitForInvoiceRowsWithRetry().stream()
				.filter(row -> row.getText().contains(username) && row.getText().contains("FACT-Q"))
				.findFirst()
				.orElseThrow(() -> new AssertionError("No invoice row found for user " + username));

		WebElement downloadBtn = invoiceRow.findElement(By.cssSelector(".btn-download"));
		wait.until(ExpectedConditions.elementToBeClickable(downloadBtn)).click();

		assertTrue(driver.findElements(By.cssSelector(".invoices-card")).size() == 1,
				"Invoices page should remain visible after download action");
	}
}
