package es.codeurjc.quesosbartolome.system;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class cheeseUITests {

    private static WebDriver driver;

    @BeforeAll
    static void setup() {
        driver = new ChromeDriver();
    }

    @Test
    void testQuesosSemicuradoYAzulVisibles() {
        driver.get("http://localhost:4200/"); // URL del frontend Angular

        // Wait until at least one element with class 'cheese-name' is visible
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-name")));

        // Now get all cheese name elements
        List<WebElement> nombres = driver.findElements(By.cssSelector(".cheese-name"));

        // Check List is not empty
        assertFalse(nombres.isEmpty(), "No cheese names were found on the page");

        // Search by name
        boolean semicurado = nombres.stream()
                .anyMatch(el -> el.getText().equalsIgnoreCase("Semicurado"));
        boolean azul = nombres.stream()
                .anyMatch(el -> el.getText().equalsIgnoreCase("Azul"));

        // Verify both
        assertTrue(semicurado, "The cheese 'Semicurado' should be displayed on the page");
        assertTrue(azul, "The cheese 'Azul' should be displayed on the page");
    }

    @AfterAll
    static void tearDown() {
        if (driver != null) driver.quit();
    }
}
