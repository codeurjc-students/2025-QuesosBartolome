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

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", loginBtn);
        try {
            Thread.sleep(200);
            new Actions(driver)
                    .moveToElement(loginBtn)
                    .pause(Duration.ofMillis(200))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
        }

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[name='username']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

        usernameInput.sendKeys("User");
        passwordInput.sendKeys("password123");

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[type='submit']")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", submitButton);
        try {
            Thread.sleep(200);
            new Actions(driver)
                    .moveToElement(submitButton)
                    .pause(Duration.ofMillis(200))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
        }

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        alert.accept();
        
        try {
            Thread.sleep(500); // Wait for login to complete
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @Order(1)
    public void testMyOrderRenders() throws InterruptedException {
        loginAsUser();

        // Click on "Mi pedido" in sidebar
        WebElement myOrderLink = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(),'Mi pedido')]")));
        
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", myOrderLink);
        try {
            Thread.sleep(300);
            new Actions(driver)
                    .moveToElement(myOrderLink)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", myOrderLink);
        }

        // Wait for navigation to /myOrder page
        wait.until(ExpectedConditions.urlContains("/myOrder"));
        Thread.sleep(500);

        // Verify order container is visible
        WebElement orderContainer = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector(".order-container")));
        assertTrue(orderContainer.isDisplayed(), "Order container should be visible");
    }

    @Test
    @Order(2)
    public void testAddAndRemoveItem() throws InterruptedException {
        loginAsUser();

        // Navigate directly to Tierno cheese details (id=5) - has large inventory
        driver.get("http://localhost:4200/cheeses/5");
        wait.until(ExpectedConditions.urlContains("/cheeses/5"));
        Thread.sleep(500);

        // Enter valid number of boxes
        WebElement boxesInput = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
        boxesInput.clear();
        boxesInput.sendKeys("1");

        // Click Add button with safe pattern
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addButton);
        Thread.sleep(300);
        
        try {
            new Actions(driver)
                    .moveToElement(addButton)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addButton);
        }

        // Accept success alert
        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        successAlert.accept();

        // Go to "Mi pedido"
        driver.get("http://localhost:4200/myorder");
        wait.until(ExpectedConditions.urlContains("/myorder"));
        Thread.sleep(500);

        // Verify item appears
        List<WebElement> items = driver.findElements(By.cssSelector(".order-item"));
        assertFalse(items.isEmpty(), "Order should contain at least one item");

        // Remove item with safe click
        WebElement deleteBtn = items.get(0).findElement(By.cssSelector(".btn-delete"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", deleteBtn);
        Thread.sleep(300);
        
        try {
            new Actions(driver)
                    .moveToElement(deleteBtn)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
        }

        // Verify item disappears
        wait.until(ExpectedConditions.numberOfElementsToBe(By.cssSelector(".order-item"), 0));
    }

    @Test
    @Order(3)
    public void testMakeOrderSuccess() throws InterruptedException {
        loginAsUser();

        // Navigate directly to Tierno cheese details (id=5) - has large inventory
        driver.get("http://localhost:4200/cheeses/5");
        wait.until(ExpectedConditions.urlContains("/cheeses/5"));
        Thread.sleep(500);

        // Enter valid number of boxes
        WebElement boxesInput = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
        boxesInput.clear();
        boxesInput.sendKeys("1");

        // Click Add button with safe pattern
        WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", addButton);
        Thread.sleep(300);
        
        try {
            new Actions(driver)
                    .moveToElement(addButton)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", addButton);
        }

        // Accept success alert
        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        successAlert.accept();

        // Go to "My order"
        driver.get("http://localhost:4200/myorder");
        wait.until(ExpectedConditions.urlContains("/myorder"));
        Thread.sleep(500);

        // Click "Hacer Pedido" with safe pattern
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", confirmBtn);
        Thread.sleep(300);
        
        try {
            new Actions(driver)
                    .moveToElement(confirmBtn)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
        }

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
    public void testMakeOrderFailure() throws InterruptedException {
        loginAsUser();

        // Go to "My order" and ensure cart is empty
        driver.get("http://localhost:4200/myorder");
        wait.until(ExpectedConditions.urlContains("/myorder"));
        Thread.sleep(500);
        
        // Remove all items if any exist
        List<WebElement> existingItems = driver.findElements(By.cssSelector(".order-item"));
        while (!existingItems.isEmpty()) {
            WebElement deleteBtn = existingItems.get(0).findElement(By.cssSelector(".btn-delete"));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", deleteBtn);
            Thread.sleep(200);
            try {
                new Actions(driver)
                        .moveToElement(deleteBtn)
                        .pause(Duration.ofMillis(200))
                        .click()
                        .perform();
            } catch (Exception e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
            }
            Thread.sleep(300);
            existingItems = driver.findElements(By.cssSelector(".order-item"));
        }

        // Click "Hacer Pedido" with safe pattern
        WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", confirmBtn);
        Thread.sleep(300);
        
        try {
            new Actions(driver)
                    .moveToElement(confirmBtn)
                    .pause(Duration.ofMillis(300))
                    .click()
                    .perform();
        } catch (Exception e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmBtn);
        }

        // Check the alert text
        Alert orderAlert = wait.until(ExpectedConditions.alertIsPresent());
        String alertText = orderAlert.getText();
        orderAlert.accept();

        // Assert that the alert text is the error message
        assertEquals("Error al hacer el pedido", alertText,
                "Alert should indicate failure when trying to order with empty cart");
    }
}
