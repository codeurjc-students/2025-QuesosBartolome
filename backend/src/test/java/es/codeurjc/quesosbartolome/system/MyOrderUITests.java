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
public class MyOrderUITests {

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
        if (driver != null)
            driver.quit();
    }

    /**
     * Helper to login as a normal user.
     */
    private void loginAsUser() {
        driver.get("http://localhost:4200/");
        WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
        loginBtn.click();

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='username']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

        usernameInput.sendKeys("Victor");
        passwordInput.sendKeys("password123");

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
    }

    @Test
    @Order(1)
    public void testMyOrderRenders() {
        loginAsUser();

        // Click on "Mi pedido" in sidebar
        WebElement myOrderLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(),'Mi pedido')]")));
        myOrderLink.click();

        // Verify order container is visible
        WebElement orderContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".order-container")));
        assertTrue(orderContainer.isDisplayed(), "Order container should be visible");
    }

    @Test
    @Order(2)
    public void testAddAndRemoveItem() {
        loginAsUser();

        // Navigate directly to Azul cheese details (id=2)
        driver.get("http://localhost:4200/cheeses/2");

        // Enter valid number of boxes
        WebElement boxesInput = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
        boxesInput.sendKeys("1");

        // Click Add button using Actions
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
        new Actions(driver).moveToElement(addButton).pause(200).click().perform();

        // Accept success alert
        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        successAlert.accept();

        // Go to "Mi pedido"
        driver.get("http://localhost:4200/myorder");

        // Verify item appears
        List<WebElement> items = driver.findElements(By.cssSelector(".order-item"));
        assertFalse(items.isEmpty(), "Order should contain at least one item");

        // Remove item
        WebElement deleteBtn = items.get(0).findElement(By.cssSelector(".btn-delete"));
        deleteBtn.click();

        // Verify item disappears
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".order-item"), 0));
    }

    @Test
    @Order(3)
    public void testMakeOrderSuccess() {
        loginAsUser();

        // Navigate directly to Azul cheese details (id=2)
        driver.get("http://localhost:4200/cheeses/2");

        // Enter valid number of boxes
        WebElement boxesInput = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
        boxesInput.sendKeys("1");

        // Click Add button using Actions
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
        new Actions(driver).moveToElement(addButton).pause(200).click().perform();

        // Accept success alert
        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        successAlert.accept();

        // Go to "My order"
        driver.get("http://localhost:4200/myorder");

        // Click "Hacer Pedido"
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
        confirmBtn.click();

        // Expect success alert
        Alert orderAlert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(orderAlert.getText().contains("Pedido realizado correctamente"),
                "Order success alert should appear");
        orderAlert.accept();

        // Verify cart is reset (no items)
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".order-item"), 0));
    }

    @Test
    @Order(4)
    public void testMakeOrderFailure() {
        loginAsUser();

        // Go directly to "My order" without adding items
        driver.get("http://localhost:4200/myorder");

        // Click "Hacer Pedido"
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
        confirmBtn.click();

        // Check the alert text
        Alert orderAlert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = orderAlert.getText();
        orderAlert.accept();

        // Assert that the alert text is the error message
        assertEquals("Error al hacer el pedido", alertText,
                "Alert should indicate failure when trying to order with empty cart");
    }
}
