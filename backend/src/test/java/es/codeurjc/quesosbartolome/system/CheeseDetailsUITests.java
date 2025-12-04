package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
public class CheeseDetailsUITests {

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

    @Test
    public void testNavigateToSemicuradoDetails() {

        // Open the application
        driver.get("http://localhost:4200/");

        wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.cssSelector(".card-grid")));

        // Wait until at least 1 cheese card is present
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                By.cssSelector(".card"), 0));

        // Locate the card containing the cheese name "Semicurado"
        WebElement semicuradoCard = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.xpath(
                        "//div[contains(@class,'card')]//p[text()='Semicurado']" +
                                "/ancestor::div[contains(@class,'card')]")));

        // Click the card
        semicuradoCard.click();

        // Wait for navigation to /cheeses/{id}
        wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

        // Verify the title on the cheese details page
        WebElement title = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-title")));

        assertEquals("Semicurado", title.getText(),
                "The cheese details page must display the selected cheese name.");
    }

    @Test
    public void testLoggedUserSeesStockAndControls() {

        // 1. Open application
        driver.get("http://localhost:4200/");

        // 2. Click "Iniciar Sesión"
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
        loginBtn.click();

        // 3. Perform login
        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='username']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

        usernameInput.sendKeys("Victor");
        passwordInput.sendKeys("password123");

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        // 4. Accept success alert
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("¡Login correcto! Los tokens están en las cookies.", alert.getText());
        alert.accept();

        // 5. Wait until the cheese grid loads
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

        // 6. Click the Semicurado card
        WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath(
                        "//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
        semicuradoCard.click();

        // 7. Wait for navigation to /cheeses/{id}
        wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

        // 8. Verify cheese title
        WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".cheese-title")));
        assertEquals("Semicurado", title.getText(), "Cheese title should be Semicurado.");

        // 9. Verify boxes input is visible for logged users
        WebElement boxesInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".cajas-input")));
        assertTrue(boxesInput.isDisplayed(), "Boxes input must be visible after login.");

        // 10. Verify Add button is visible
        WebElement addButton = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".add-btn")));
        assertTrue(addButton.isDisplayed(), "Add button must be visible for logged in users.");

        // 11. Verify boxes available (stock) is shown
        WebElement stockLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.xpath("//span[contains(@class,'stock-info')]")));

        String stockText = stockLabel.getText().replaceAll("[^0-9]", "");
        int stock = Integer.parseInt(stockText);

        assertEquals(10, stock, "Semicurado stock must be 10.");

    }

}
