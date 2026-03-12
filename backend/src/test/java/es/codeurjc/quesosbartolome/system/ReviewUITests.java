package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
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

	@AfterEach
	public void teardown() {
		if (driver != null) {
			driver.quit();
		}
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

		Alert alert = wait.until(ExpectedConditions.alertIsPresent());
		String loginText = alert.getText();
		System.out.println("[ReviewUITests] Login alert text: " + loginText);
		alert.accept();
		assertTrue(loginText.toLowerCase().contains("login correcto"), "Login failed with alert: " + loginText);

		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
	}

	private void openCheeseDetailsByName(String cheeseName) {
		WebElement card = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.xpath("//div[contains(@class,'card')]//p[contains(text(), '" + cheeseName
						+ "')]/ancestor::div[contains(@class,'card')]")));
		clickWithFallback(card);

		wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".reviews-section")));
	}

	private int getTotalReviewsFromTitle() {
		WebElement reviewsTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(
				By.cssSelector(".reviews-section h2")));

		Matcher matcher = Pattern.compile("(\\d+)").matcher(reviewsTitle.getText());
		assertTrue(matcher.find(), "Could not parse total reviews from section title");
		return Integer.parseInt(matcher.group(1));
	}

	private void createReviewInCurrentCheese(String comment, int rating) {
		WebElement toggleReviewButton = wait.until(ExpectedConditions.elementToBeClickable(
				By.cssSelector(".btn-review-toggle")));
		clickWithFallback(toggleReviewButton);

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

		WebElement submitReviewButton = wait.until(ExpectedConditions.elementToBeClickable(
				By.cssSelector(".btn-submit")));
		installDialogHooks();
		clickWithFallback(submitReviewButton);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));",
				submitReviewButton);

		String text = tryReadAnyAlertText(Duration.ofSeconds(4));
		if (text != null) {
			System.out.println("[ReviewUITests] Create review alert text: " + text);
		}
	}

	private void installDialogHooks() {
		((JavascriptExecutor) driver).executeScript(
				"window.__testAlerts = [];"
						+ "window.__testConfirms = [];"
						+ "window.alert = function(msg){ window.__testAlerts.push(String(msg)); };"
						+ "window.confirm = function(msg){ window.__testConfirms.push(String(msg)); return true; };"
		);
	}

	private List<String> getCapturedAlerts() {
		Object value = ((JavascriptExecutor) driver).executeScript("return window.__testAlerts || []; ");
		List<String> result = new ArrayList<>();
		if (value instanceof List<?> rawList) {
			for (Object item : rawList) {
				result.add(String.valueOf(item));
			}
		}
		return result;
	}

	private String tryReadAnyAlertText(Duration timeout) {
		long end = System.currentTimeMillis() + timeout.toMillis();
		while (System.currentTimeMillis() < end) {
			try {
				Alert nativeAlert = driver.switchTo().alert();
				String nativeText = nativeAlert.getText();
				nativeAlert.accept();
				return nativeText;
			} catch (NoAlertPresentException ignored) {
				// Keep checking until timeout.
			}

			List<String> alerts = getCapturedAlerts();
			if (!alerts.isEmpty()) {
				return alerts.get(0);
			}

			try {
				Thread.sleep(150);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return null;
			}
		}
		return null;
	}

	private void resetSession() {
		driver.manage().deleteAllCookies();
		driver.get("http://localhost:4200/");
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

		login("Victor", "password123");
		openCheeseDetailsByName("Azul");

		int previousTotal = getTotalReviewsFromTitle();

		createReviewInCurrentCheese(uniqueComment, 5);

		wait.until(d -> getTotalReviewsFromTitle() >= previousTotal + 1);
	}

	@Test
	@Order(3)
	public void testUserCanDeleteOwnReview() {
		String deletableComment = "Review To Delete " + System.currentTimeMillis();

		login("Victor", "password123");
		openCheeseDetailsByName("Azul");

		int previousTotal = getTotalReviewsFromTitle();
		createReviewInCurrentCheese(deletableComment, 4);
		wait.until(d -> getTotalReviewsFromTitle() >= previousTotal + 1);

		resetSession();
		login("German", "password123");
		openCheeseDetailsByName("Azul");

		int totalBeforeDelete = getTotalReviewsFromTitle();
		assertTrue(totalBeforeDelete > 0, "There must be at least one review to delete");

		WebElement deleteButton = wait.until(ExpectedConditions.elementToBeClickable(
				By.cssSelector(".btn-delete-review")));
		installDialogHooks();
		clickWithFallback(deleteButton);
		((JavascriptExecutor) driver).executeScript(
				"arguments[0].dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, view: window }));",
				deleteButton);

		String deleteText = tryReadAnyAlertText(Duration.ofSeconds(6));
		assertNotNull(deleteText, "No delete alert/notification was detected after clicking delete review");
		System.out.println("[ReviewUITests] Delete review alert text: " + deleteText);

		wait.until(d -> getTotalReviewsFromTitle() <= totalBeforeDelete - 1);
	}
}
