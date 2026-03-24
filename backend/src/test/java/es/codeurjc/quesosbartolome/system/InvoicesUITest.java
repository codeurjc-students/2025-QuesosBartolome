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
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
public class InvoicesUITest {

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

	Alert alert = wait.until(ExpectedConditions.alertIsPresent());
	alert.accept();
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

	Alert addAlert = wait.until(ExpectedConditions.alertIsPresent());
	addAlert.accept();

	driver.get("http://localhost:4200/myorder");

	WebElement confirmBtn = wait.until(
		ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
	confirmBtn.click();

	Alert orderAlert = wait.until(ExpectedConditions.alertIsPresent());
	assertTrue(orderAlert.getText().contains("Pedido realizado correctamente"));
	orderAlert.accept();
    }

    @Test
    public void testAdminSeesInvoiceAfterProcessingOrder() {
	String username = "Victor";

	login(username, "password123");
	createOrderAsUser();
	logout();

	login("German", "password123");
	driver.get("http://localhost:4200/orders");

	List<WebElement> orderRows = wait.until(
		ExpectedConditions.visibilityOfAllElementsLocatedBy(By.cssSelector(".orders-row")));
	WebElement userOrder = orderRows.stream()
		.filter(row -> row.getText().contains(username))
		.findFirst()
		.orElseThrow(() -> new AssertionError("No pending order found for user " + username));

	WebElement processBtn = userOrder.findElement(By.cssSelector(".btn-process"));
	wait.until(ExpectedConditions.elementToBeClickable(processBtn)).click();

	WebElement previewCard = wait.until(
		ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".preview-card")));
	assertTrue(previewCard.getText().contains("Vista previa del pedido"));

	WebElement confirmBtn = wait.until(
		ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.btn-confirm")));
	confirmBtn.click();

	Alert invoiceAlert = wait.until(ExpectedConditions.alertIsPresent());
	assertTrue(invoiceAlert.getText().contains("Factura creada correctamente"));
	invoiceAlert.accept();

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
}
