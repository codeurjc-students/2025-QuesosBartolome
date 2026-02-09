package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
public class CheeseDetailsUITests {

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
                if (driver != null)
                        driver.quit();
        }

        @Test
        public void testNavigateToSemicuradoDetails() {
                driver.get("http://localhost:4200/");

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(".card"), 0));

                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                WebElement title = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-title")));
                assertEquals("Semicurado", title.getText(),
                                "The cheese details page must display the selected cheese name.");
        }

        @Test
        public void testLoggedUserSeesStockAndControls() {
                driver.get("http://localhost:4200/");

                WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                loginBtn.click();

                WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("Victor");
                passwordInput.sendKeys("password123");

                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("¡Login correcto! Los tokens están en las cookies.", alert.getText());
                alert.accept();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                WebElement title = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-title")));
                assertEquals("Semicurado", title.getText(), "Cheese title should be Semicurado.");

                WebElement boxesInput = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
                assertTrue(boxesInput.isDisplayed(), "Boxes input must be visible after login.");

                WebElement addButton = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".add-btn")));
                assertTrue(addButton.isDisplayed(), "Add button must be visible for logged in users.");

                WebElement stockLabel = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//span[contains(@class,'stock-info')]")));
                String stockText = stockLabel.getText().replaceAll("[^0-9]", "");
                int stock = Integer.parseInt(stockText);
                assertEquals(24, stock, "Semicurado stock must be 24.");
        }

        @Test
        public void testAdminSeesEditButtonAndNoAddControls() {
                driver.get("http://localhost:4200/");

                WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                loginBtn.click();

                WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("German");
                passwordInput.sendKeys("password123");

                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("¡Login correcto! Los tokens están en las cookies.", alert.getText());
                alert.accept();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                WebElement title = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-title")));
                assertEquals("Semicurado", title.getText(), "Cheese title should be Semicurado.");

                WebElement editButton = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".edit-btn")));
                assertTrue(editButton.isDisplayed(), "Edit button must be visible for ADMIN.");

                assertTrue(driver.findElements(By.cssSelector(".cajas-input")).isEmpty(),
                                "Boxes input should not be visible for ADMIN.");
                assertTrue(driver.findElements(By.cssSelector(".add-btn")).isEmpty(),
                                "Add button should not be visible for ADMIN.");
        }

        @Test
        public void testAddItemToCart_Success() throws InterruptedException {
                // Open the application
                driver.get("http://localhost:4200/");

                // Click on "Iniciar Sesión" button
                WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                loginBtn.click();

                // Fill in login form
                WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("Victor");
                passwordInput.sendKeys("password123");

                // Submit login form
                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

                // Accept login success alert
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();

                // Wait until cheese grid is visible
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                // Click on Azul card
                WebElement azulCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Azul']/ancestor::div[contains(@class,'card')]")));
                azulCard.click();

                // Wait for navigation to cheese details page
                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // Enter valid number of boxes
                WebElement boxesInput = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
                boxesInput.sendKeys("1");

                // Use Actions to click on Add button
                WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
                Actions actions = new Actions(driver);
                actions.moveToElement(addButton).pause(200).click().perform();

                // Expect success alert
                Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertTrue(successAlert.getText().contains("Producto añadido al pedido"));
                successAlert.accept();
        }

        @Test
        public void testAddItemToCart_Failure() throws InterruptedException {
                // Open the application
                driver.get("http://localhost:4200/");

                // Click on "Iniciar Sesión" button
                WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                loginBtn.click();

                // Fill in login form
                WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("Victor");
                passwordInput.sendKeys("password123");

                // Submit login form
                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

                // Accept login success alert
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();

                // Wait until cheese grid is visible
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                // Click on Semicurado card
                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                // Wait for navigation to cheese details page
                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // Enter invalid number of boxes (0)
                WebElement boxesInput = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
                boxesInput.sendKeys("0");

                // Use Actions to click on Add button
                WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
                Actions actions = new Actions(driver);
                actions.moveToElement(addButton).pause(200).click().perform();

                // Expect error alert
                Alert errorAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertTrue(errorAlert.getText().contains("Error al añadir el producto")
                                || errorAlert.getText().contains("Ingrese una cantidad correcta"));
                errorAlert.accept();
        }

}