package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	private void clickWithRetry(By locator) {
		for (int attempt = 0; attempt < 3; attempt++) {
			try {
				WebElement element = wait.until(ExpectedConditions.refreshed(
						ExpectedConditions.elementToBeClickable(locator)));
				clickWithFallback(element);
				return;
			} catch (StaleElementReferenceException e) {
				if (attempt == 2) {
					throw e;
				}
			}
		}
	}

	private void login(String username, String password) {
		driver.get("http://localhost:4200/");

		WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
				By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
		clickWithFallback(loginBtn);

		WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector("input[name='username']")));
		WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

		usernameInput.sendKeys(username);
		passwordInput.sendKeys(password);

		WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
				By.cssSelector("button[type='submit']")));
		clickWithFallback(submitButton);

		Alert alert = SeleniumDialogHelper.waitForDialog(wait);
		String loginText = alert.getText();
		alert.accept();
		assertTrue(loginText.equals("Inicio de sesión correcto"), "Login failed with alert: " + loginText);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
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

	private int getTotalReviewsFromTitle() {
		WebElement reviewsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector(".reviews-section h2")));

		Matcher matcher = Pattern.compile("(\\d+)").matcher(reviewsTitle.getText());
		assertTrue(matcher.find(), "Could not parse total reviews from section title");
		return Integer.parseInt(matcher.group(1));
	}

	private String createReviewInCurrentCheese(String comment, int rating) {
		clickWithRetry(By.cssSelector(".btn-review-toggle"));

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".create-review-section")));

		WebElement ratingInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("rating")));
		WebElement commentInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("comment")));

		((JavascriptExecutor) driver).executeScript(
				"arguments[0].value = arguments[1].toString();"
						+ "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
						+ "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
				ratingInput, rating);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].value = arguments[1];"
						+ "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));"
						+ "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
				commentInput, comment);

		clickWithRetry(By.cssSelector(".btn-submit"));

		String dialogText = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector(".dialog-message"))).getText();
		return dialogText;
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

	@Test
	@Order(2)
	public void testUserCanCreateReview() {
		String uniqueComment = "Review Selenium " + System.currentTimeMillis();

		login("Tienda Artesanal de Riaza", "password123");
		openCheeseDetailsByName("Azul");

		String dialogText = createReviewInCurrentCheese(uniqueComment, 5);
		assertEquals("Reseña creada correctamente", dialogText, "Unexpected dialog after creating review");

		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.cssSelector(".create-review-section")));
	}

}
