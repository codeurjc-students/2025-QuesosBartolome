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
public class UserUITests {

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
    public void testUserProfileVisibleAfterLogin() {
        driver.get("http://localhost:4200/");

        WebElement loginButton = wait.until(ExpectedConditions
                .elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
        loginButton.click();

        // login
        WebElement usernameInput = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.cssSelector("input[name='username']")));
        WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));
        usernameInput.sendKeys("Victor");
        passwordInput.sendKeys("password123");

        WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
        submitButton.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("¡Login correcto! Los tokens están en las cookies.", alert.getText());
        alert.accept();


        WebElement profileButton = wait.until(ExpectedConditions
                .elementToBeClickable(By.cssSelector("button[aria-label='Mi perfil']")));
        profileButton.click();


        WebElement profileContainer = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.cssSelector(".profile-container")));

        WebElement nameField = profileContainer.findElement(By.xpath("//label[text()='Nombre']/following-sibling::input"));
        WebElement gmailField = profileContainer.findElement(By.xpath("//label[text()='Gmail']/following-sibling::input"));
        WebElement directionField = profileContainer.findElement(By.xpath("//label[text()='Dirección']/following-sibling::input"));
        WebElement nifField = profileContainer.findElement(By.xpath("//label[text()='NIF']/following-sibling::input"));


        assertEquals("Victor", nameField.getDomProperty("value"));
        assertEquals("victor@example.com", gmailField.getDomProperty("value"));
        assertEquals("123 Main St", directionField.getDomProperty("value"));
        assertEquals("12345678A", nifField.getDomProperty("value"));

        WebElement avatarImg = profileContainer.findElement(By.cssSelector(".avatar-box img"));
        String src = avatarImg.getDomProperty("src");
        assertNotNull(src);
        assertTrue(src.contains("assets/avatar-default.png") || src.startsWith("blob:"),
                "La imagen debería ser el avatar por defecto o un blob generado");
    }
}
