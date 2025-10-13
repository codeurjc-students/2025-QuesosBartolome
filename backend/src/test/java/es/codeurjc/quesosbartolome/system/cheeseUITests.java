package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = "server.port=8080"
)
public class cheeseUITests {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    public void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterEach
    public void teardown() {
        if (driver != null) driver.quit();
    }

    @Test
    public void testQuesosSemicuradoYAzulVisibles() {
        driver.get("http://localhost:4200/");

        // Esperar hasta que los elementos de queso aparezcan
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-name")));

        List<WebElement> nombres = driver.findElements(By.cssSelector(".cheese-name"));
        assertFalse(nombres.isEmpty(), "No se encontraron nombres de queso en la página");

        boolean semicurado = nombres.stream()
                .anyMatch(el -> el.getText().equalsIgnoreCase("Semicurado"));
        boolean azul = nombres.stream()
                .anyMatch(el -> el.getText().equalsIgnoreCase("Azul"));

        assertTrue(semicurado, "El queso 'Semicurado' debería aparecer en la página");
        assertTrue(azul, "El queso 'Azul' debería aparecer en la página");
    }
}
