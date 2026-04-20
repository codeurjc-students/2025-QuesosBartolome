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
    public void testLoginUser() {
        driver.get("http://localhost:4200/auth/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));

        driver.findElement(By.id("username")).sendKeys("Victor");
        driver.findElement(By.id("password")).sendKeys("password123");

        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        button.click();

        Alert alert = SeleniumDialogHelper.waitForDialog(wait);
        assertEquals("¡Login correcto! Los tokens están en las cookies.", alert.getText());
        alert.accept();

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));
        assertEquals("http://localhost:4200/cheeses", driver.getCurrentUrl());
    }

    @Test
    public void testBannedUserCannotLoginShowsErrorAlert() {
        driver.get("http://localhost:4200/auth/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));

        driver.findElement(By.id("username")).sendKeys("German");
        driver.findElement(By.id("password")).sendKeys("password123");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginButton.click();

        Alert loginOkAlert = SeleniumDialogHelper.waitForDialog(wait);
        assertEquals("¡Login correcto! Los tokens están en las cookies.", loginOkAlert.getText());
        loginOkAlert.accept();

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));

        safeClick(By.xpath("//li[normalize-space()='Clientes']"));

        wait.until(ExpectedConditions.urlToBe("http://localhost:4200/users"));

        WebElement targetRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'users-row')][.//span[normalize-space()='User']]")));

        WebElement banButton = targetRow.findElement(By.cssSelector("button.btn-ban"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", banButton);
        banButton.click();

        Alert confirmBanAlert = SeleniumDialogHelper.waitForDialog(wait);
        confirmBanAlert.accept();

        wait.until(ExpectedConditions.visibilityOfElementLocated(
            By.xpath("//div[contains(@class,'users-row')][.//span[normalize-space()='User']]//span[contains(@class,'status-pill') and normalize-space()='BANEADO']")));

        driver.get("http://localhost:4200/cheeses");
        safeClick(By.xpath("//button[normalize-space()='Cerrar Sesión']"));

        driver.get("http://localhost:4200/auth/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));

        driver.findElement(By.id("username")).sendKeys("User");
        driver.findElement(By.id("password")).sendKeys("password123");

        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        button.click();

        Alert alert = SeleniumDialogHelper.waitForDialog(wait);
        assertEquals("No puedes iniciar sesion: tu cuenta esta baneada.", alert.getText());
        alert.accept();
    }

    @Test
    public void testLoginFailsWithWrongPasswordForVictor() {
        driver.get("http://localhost:4200/auth/login");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("username")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));

        driver.findElement(By.id("username")).sendKeys("Victor");
        driver.findElement(By.id("password")).sendKeys("password_incorrecta");

        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        button.click();

        Alert alert = SeleniumDialogHelper.waitForDialog(wait);
        assertEquals("Credenciales incorrectas.", alert.getText());
        alert.accept();
    }

    private void safeClick(By locator) {
        WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", element);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }
    }
}
