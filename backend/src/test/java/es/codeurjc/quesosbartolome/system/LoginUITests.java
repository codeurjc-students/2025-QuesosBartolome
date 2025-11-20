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
public class LoginUITests {

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
        if (driver != null) driver.quit();
    }

    @Test
    public void testRegisterUser() throws InterruptedException {

        driver.get("http://localhost:4200/auth/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombre")));

        String unique = "User" + System.currentTimeMillis();

        driver.findElement(By.id("nombre")).sendKeys(unique);
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("confirm-password")).sendKeys("password123");
        driver.findElement(By.id("email")).sendKeys(unique + "@test.com");
        driver.findElement(By.id("direccion")).sendKeys("Calle Falsa 123");
        driver.findElement(By.id("nif")).sendKeys("12945678A");

        WebElement button = driver.findElement(By.cssSelector("button.btn-login"));

        // Scroll to button
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", button);

        Thread.sleep(300);

        // Force click
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Registro exitoso", alert.getText());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));
        assertEquals("http://localhost:4200/cheeses", driver.getCurrentUrl());
    }

    @Test
    public void testLoginUser() {

        driver.get("http://localhost:4200/auth/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));

        driver.findElement(By.id("username")).sendKeys("Victor");
        driver.findElement(By.id("password")).sendKeys("password123");

        driver.findElement(By.cssSelector("button[type='submit']")).click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("¡Login correcto! Los tokens están en las cookies.", alert.getText());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));
        assertEquals("http://localhost:4200/cheeses", driver.getCurrentUrl());
    }
}
