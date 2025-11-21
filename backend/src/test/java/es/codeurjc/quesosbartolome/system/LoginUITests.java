package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class, 
                webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class LoginUITests {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new"); // Headless mode
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

    @Test
    public void testRegisterUser() {
        driver.get("http://localhost:4200/auth/register");

        // Esperamos todos los campos visibles
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombre")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm-password")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("direccion")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nif")));

        String unique = "User" + System.currentTimeMillis();

        driver.findElement(By.id("nombre")).sendKeys(unique);
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("confirm-password")).sendKeys("password123");
        driver.findElement(By.id("email")).sendKeys(unique + "@test.com");
        driver.findElement(By.id("direccion")).sendKeys("Calle Falsa 123");
        driver.findElement(By.id("nif")).sendKeys("12945678A");

        // Pequeño delay para que Angular procese los bindings (evita overlay de validación)
        try { Thread.sleep(300); } catch (InterruptedException e) { /* ignore */ }

        // Clic seguro mediante Javascript
        WebElement button = driver.findElement(By.cssSelector("button.btn-login"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);

        // Esperamos el alert
        Alert alert = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.alertIsPresent());
        assertEquals("Registro exitoso", alert.getText());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));
        assertEquals("http://localhost:4200/cheeses", driver.getCurrentUrl());
    }


    @Test
    public void testLoginUser() {
        driver.get("http://localhost:4200/auth/login");

        // Esperamos los campos visibles
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));

        driver.findElement(By.id("username")).sendKeys("Victor");
        driver.findElement(By.id("password")).sendKeys("password123");

        // Esperamos que el botón sea clickeable
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", button);
        button.click();

        // Esperamos el alert de login correcto
        Alert alert = new WebDriverWait(driver, Duration.ofSeconds(10))
                .until(ExpectedConditions.alertIsPresent());
        assertEquals("¡Login correcto! Los tokens están en las cookies.", alert.getText());
        alert.accept();

        // Comprobamos navegación a la página de quesos
        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));
        assertEquals("http://localhost:4200/cheeses", driver.getCurrentUrl());
    }
}
