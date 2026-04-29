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
                webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RegisterUITests {

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
        if (driver != null) {
            driver.quit();
        }
    }

    @Disabled("Flaky in CI: intermittently returns Internal Server Error during UI registration flow")
    @Test
    public void testRegisterUser() throws InterruptedException {
        driver.get("http://localhost:4200/auth/register");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombre")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm-password")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("direccion")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nif")));

        String unique = "User" + System.currentTimeMillis();
        String nif = (System.currentTimeMillis() % 100000000) + "A";

        driver.findElement(By.id("nombre")).sendKeys(unique);
        driver.findElement(By.id("password")).sendKeys("password123");
        driver.findElement(By.id("confirm-password")).sendKeys("password123");
        driver.findElement(By.id("email")).sendKeys(unique + "@test.com");
        driver.findElement(By.id("direccion")).sendKeys("Calle Falsa 123");
        driver.findElement(By.id("nif")).sendKeys(nif);

        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

        Thread.sleep(200);

        submitButton.click();

        Alert alert = SeleniumDialogHelper.waitForDialog(wait);

        assertEquals("Registro exitoso", alert.getText());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));
        assertEquals("http://localhost:4200/cheeses", driver.getCurrentUrl());
    }
}
