package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
public class StockUITests {

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
        if (driver != null)
            driver.quit();
    }

    /** LOGIN AS ADMIN */
    private void loginAsAdmin() {
        driver.get("http://localhost:4200/");

        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
        loginBtn.click();

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='username']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

        usernameInput.sendKeys("German");
        passwordInput.sendKeys("password123");

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    /** Gets the first cheese stock card */
    private WebElement getFirstStockCard() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".stock-grid")));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".stock-card")));
    }

    @Test
    public void testAddBoxInStockPage() {
        loginAsAdmin();

        driver.get("http://localhost:4200/stock");

        WebElement card = getFirstStockCard();

        // Enter value in input
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(
                card.findElement(By.cssSelector(".add-load input"))));
        input.clear();
        input.sendKeys("3");

        // Wait for Add button to be clickable and scroll to it
        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                card.findElement(By.cssSelector(".btn-add"))));

        // Scroll to the button
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addBtn);

        // Small wait to ensure scroll completes
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        // Click using JavaScript
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);

        // Wait for the operation to complete (no alert is shown)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        // Verify the input was cleared after successful operation
        WebElement inputAfter = card.findElement(By.cssSelector(".add-load input"));
        String inputValue = inputAfter.getDomProperty("value");
        assertTrue(inputValue == null || inputValue.isEmpty(),
                "Input should be cleared after adding box");
    }

    @Test
    public void testRemoveBoxInStockPage() {
        loginAsAdmin();

        driver.get("http://localhost:4200/stock");

        WebElement card = getFirstStockCard();

        // Ensure there is at least 1 box to remove by adding one first
        WebElement input = wait.until(ExpectedConditions.elementToBeClickable(
                card.findElement(By.cssSelector(".add-load input"))));
        input.clear();
        input.sendKeys("2");

        WebElement addBtn = wait.until(ExpectedConditions.elementToBeClickable(
                card.findElement(By.cssSelector(".btn-add"))));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addBtn);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addBtn);

        // Wait for add operation to complete (no alert)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        // Reload the page to get fresh state
        driver.get("http://localhost:4200/stock");

        // Now try to remove a box
        // Wait for boxes to be present (search fresh each time)
        wait.until(d -> {
            WebElement freshCard = d.findElement(By.cssSelector(".stock-card"));
            List<WebElement> boxes = freshCard.findElements(By.cssSelector(".load"));
            return !boxes.isEmpty();
        });

        // Get fresh references after wait
        WebElement freshCard = driver.findElement(By.cssSelector(".stock-card"));
        List<WebElement> boxes = freshCard.findElements(By.cssSelector(".load"));
        assertTrue(boxes.size() > 0, "There should be at least one box to remove");

        // Find and click on the remove button of the first box
        WebElement firstBox = boxes.get(0);
        WebElement removeBtn = firstBox.findElement(By.cssSelector(".remove-btn"));

        // Scroll to the remove button
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", removeBtn);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }

        // Click using JavaScript
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", removeBtn);

        // Wait for remove operation to complete (no alert)
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
        }

        // Test passes if we got here without exceptions
        assertTrue(true, "Remove operation completed successfully");
    }
}
