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

                driver.findElement(By.cssSelector("button[type='submit']")).click();
                SeleniumDialogHelper.waitForDialog(wait).accept();
        }

        private void loginAsAdmin() {
                login("Admin", "password123");
        }

        private void loginAsUser() {
                login("Tienda Artesanal de Riaza", "password123");
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
                loginAsUser();

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
                loginAsAdmin();

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
                loginAsUser();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                WebElement azulCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Azul']/ancestor::div[contains(@class,'card')]")));
                azulCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                WebElement boxesInput = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
                boxesInput.sendKeys("1");

                WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
                new Actions(driver).moveToElement(addButton).pause(200).click().perform();

                Alert successAlert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(successAlert.getText().contains("Producto añadido al pedido"));
                successAlert.accept();
        }

        @Test
        public void testAddItemToCart_Failure() throws InterruptedException {
                loginAsUser();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                WebElement boxesInput = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
                boxesInput.sendKeys("0");

                WebElement addButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
                new Actions(driver).moveToElement(addButton).pause(200).click().perform();

                Alert errorAlert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(errorAlert.getText().contains("Error al añadir el producto")
                                || errorAlert.getText().contains("Ingrese una cantidad correcta"));
                errorAlert.accept();
        }

}
