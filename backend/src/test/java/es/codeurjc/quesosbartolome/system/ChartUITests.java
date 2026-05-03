package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ChartUITests {

	private WebDriver driver;
	private WebDriverWait wait;

	@BeforeEach
	public void setup() {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless=new");
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
		driver.get("http://localhost:4200/auth/login");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
		driver.findElement(By.id("username")).sendKeys(username);
		driver.findElement(By.id("password")).sendKeys(password);
		wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']"))).click();
		SeleniumDialogHelper.waitForDialog(wait).accept();
		wait.until(ExpectedConditions.urlContains("/cheeses"));
	}

	private void loginAsAdmin() {
		login("Admin", "password123");
	}
 
	@Test
	public void chartsPageShowsGrafico1AndGrafico2Switches() {
		loginAsAdmin();
		driver.get("http://localhost:4200/charts");

		WebElement grafico1 = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//mat-button-toggle[normalize-space()='Gráfico 1']")));
		WebElement grafico2 = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//mat-button-toggle[normalize-space()='Gráfico 2']")));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("svg.line-chart")));

		assertTrue(grafico1.isDisplayed());
		assertTrue(grafico2.isDisplayed());
	}

	@Test
	public void chartsSwitchChangesBetweenLineAndBarChart() {
		loginAsAdmin();
		driver.get("http://localhost:4200/charts");

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("svg.line-chart")));

		WebElement grafico2 = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//mat-button-toggle[normalize-space()='Gráfico 2']")));
		grafico2.click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("svg.bar-chart")));
		assertFalse(driver.findElements(By.cssSelector("svg.bar-chart")).isEmpty());

		WebElement grafico1 = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//mat-button-toggle[normalize-space()='Gráfico 1']")));
		grafico1.click();

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("svg.line-chart")));
		assertFalse(driver.findElements(By.cssSelector("svg.line-chart")).isEmpty());
	}

}
