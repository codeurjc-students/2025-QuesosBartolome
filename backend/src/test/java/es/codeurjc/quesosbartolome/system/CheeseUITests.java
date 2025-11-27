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
public class CheeseUITests {

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
        wait = new WebDriverWait(driver, Duration.ofSeconds(20)); // esperar un poco más
    }

    @AfterEach 
    public void teardown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testQuesosSemicuradoYAzulVisibles() {

        // Abrimos la app
        driver.get("http://localhost:4200/");

        // Esperamos a que el contenedor de las cards esté visible
        WebElement cardGrid = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.cssSelector(".card-grid")));

        // Esperamos explícitamente a que haya al menos 2 elementos <p> dentro de .card-body
        wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".card-body p"), 1));

        // Ahora sí buscamos todos los <p>
        List<WebElement> nombres = cardGrid.findElements(By.cssSelector(".card-body p"));

        assertFalse(nombres.isEmpty(), "No se encontraron nombres de queso en la página");

        boolean semicurado = nombres.stream()
                .anyMatch(el -> el.getText().equalsIgnoreCase("Semicurado"));
        boolean azul = nombres.stream()
                .anyMatch(el -> el.getText().equalsIgnoreCase("Azul"));

        assertTrue(semicurado, "El queso 'Semicurado' debería aparecer en la página");
        assertTrue(azul, "El queso 'Azul' debería aparecer en la página");
    }

    @Test
    public void testNavigateToAboutUs() {

        // Open the app
        driver.get("http://localhost:4200/");

        // Wait for the sidebar to be visible
        WebElement sidebar = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.cssSelector(".sidebar")));

        // Find the "About Us" button inside the sidebar
        WebElement aboutBtn = sidebar.findElement(
                By.xpath("//li[contains(., 'Acerca de nosotros')]")
        );

        // Click the menu option
        aboutBtn.click();

        // Wait for navigation to /about-us
        wait.until(ExpectedConditions.urlContains("/about-us"));

        // Verify that the About Us title is visible
        WebElement title = wait.until(ExpectedConditions
                .visibilityOfElementLocated(By.cssSelector(".text-section h1"))
        );

        assertEquals(
                "Quesos Bartolomé",
                title.getText(),
                "The About Us page should load correctly"
        );
    }

}