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
public class OrdersUITests {

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

    // ---------------------------
    // Helpers
    // ---------------------------

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
        // Ir a un queso concreto
        driver.get("http://localhost:4200/cheeses/2");

        WebElement boxesInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
        boxesInput.sendKeys("1");

        WebElement addButton = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
        new Actions(driver).moveToElement(addButton).pause(200).click().perform();

        Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
        successAlert.accept();

        // Ir a Mi pedido
        driver.get("http://localhost:4200/myorder");

        // Confirmar pedido
        WebElement confirmBtn = wait.until(
                ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
        confirmBtn.click();

        Alert orderAlert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(orderAlert.getText().contains("Pedido realizado correctamente"));
        orderAlert.accept();
    }

    // ---------------------------
    // TEST PRINCIPAL
    // ---------------------------

    @Test
    @Order(1)
    public void testAdminCanSeeCreatedOrder() {

        // 1. Login como usuario normal y crear pedido
        login("Victor", "password123");
        createOrderAsUser();
        logout();

        // 2. Login como ADMIN
        login("German", "password123");

        // 3. Ir al menú Pedidos
        WebElement ordersMenu = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//li[contains(text(),'Pedidos')]")));
        ordersMenu.click();

        // 4. Comprobar que aparece al menos un pedido
        List<WebElement> orderRows = wait.until(
                ExpectedConditions.visibilityOfAllElementsLocatedBy(
                        By.cssSelector(".orders-row")));

        assertFalse(orderRows.isEmpty(), "Admin should see at least one order");

        // 5. Comprobar que el pedido contiene el usuario Victor
        boolean foundVictor = orderRows.stream()
                .anyMatch(row -> row.getText().contains("Victor"));

        assertTrue(foundVictor, "The order created by Victor should appear in admin orders list");
    }
}
