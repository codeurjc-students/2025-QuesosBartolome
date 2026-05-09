package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ReviewUITests {

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
		options.addArguments("--user-data-dir=/tmp/chrome-user-data-" + System.currentTimeMillis());
		options.addArguments("--ignore-certificate-errors");
		options.setAcceptInsecureCerts(true);

		driver = new ChromeDriver(options);
		wait = new WebDriverWait(driver, Duration.ofSeconds(20));
	}

	private void clickWithFallback(WebElement element) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
		try {
			Thread.sleep(200);
			new Actions(driver)
					.moveToElement(element)
					.pause(Duration.ofMillis(200))
					.click()
					.perform();
		} catch (Exception e) {
			((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
		}
	}

	private void openCheeseDetailsByName(String cheeseName) {
		int cheeseId = switch (cheeseName) {
			case "Semicurado" -> 1;
			case "Azul" -> 2;
			case "Curado" -> 3;
			case "Chevrett" -> 4;
			case "Tierno" -> 5;
			default -> throw new IllegalArgumentException("Unknown cheese: " + cheeseName);
		};

		WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//div[contains(@class,'card')]//p[contains(text(), '" + cheeseName
						+ "')]/ancestor::div[contains(@class,'card')]")));
		clickWithFallback(card);

		try {
			wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));
		} catch (TimeoutException ex) {
			driver.get("http://localhost:4200/cheeses/" + cheeseId);
			wait.until(ExpectedConditions.urlContains("/cheeses/" + cheeseId));
		}
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".reviews-section")));
	}

	@Test
	@Order(1)
	public void testReviewsAreVisibleInCheeseDetails() {
		driver.get("http://localhost:4200/");
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

		openCheeseDetailsByName("Semicurado");

		List<WebElement> reviewCards = driver.findElements(By.cssSelector(".review-card"));
		List<WebElement> noReviewsMessage = driver.findElements(By.cssSelector(".no-reviews"));

		assertTrue(!reviewCards.isEmpty() || !noReviewsMessage.isEmpty(),
				"The reviews section should show reviews or an empty-state message");

		if (!reviewCards.isEmpty()) {
			List<WebElement> comments = driver.findElements(By.cssSelector(".review-comment"));
			assertTrue(comments.stream().anyMatch(c -> !c.getText().trim().isEmpty()),
					"If review cards exist, at least one comment should be non-empty");
		}
	}

}
