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
                wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        }

        @AfterEach
        public void teardown() {
                if (driver != null)
                        driver.quit();
        }

        @Test
        public void testQuesosSemicuradoYAzulVisibles() {

                // 1: Open the main application page.
                driver.get("http://localhost:4200/");

                // 2: Wait until the cheese card grid is visible.
                WebElement cardGrid = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".card-grid")));

                // 3: Wait until at least two cheese name elements are loaded.
                wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(
                                By.cssSelector(".card-body p"), 1));

                // 4: Retrieve all <p> elements representing cheese names.
                List<WebElement> nombres = cardGrid.findElements(By.cssSelector(".card-body p"));

                // 5: Ensure the list of cheese names is not empty.
                assertFalse(nombres.isEmpty(), "No cheese names were found on the page");

                // 6: Check whether 'Semicurado' is present in the displayed cheeses.
                boolean semicurado = nombres.stream()
                                .anyMatch(el -> el.getText().equalsIgnoreCase("Semicurado"));

                // 7: Check whether 'Azul' is present in the displayed cheeses.
                boolean azul = nombres.stream()
                                .anyMatch(el -> el.getText().equalsIgnoreCase("Azul"));

                // 8: Assert that 'Semicurado' is visible to the user.
                assertTrue(semicurado, "'Semicurado' should appear on the page");

                // 9: Assert that 'Azul' is visible to the user.
                assertTrue(azul, "'Azul' should appear on the page");
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
                                By.xpath("//li[contains(., 'Acerca de nosotros')]"));

                // Click the menu option
                aboutBtn.click();

                // Wait for navigation to /about-us
                wait.until(ExpectedConditions.urlContains("/about-us"));

                // Verify that the About Us title is visible
                WebElement title = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".text-section h1")));

                assertEquals(
                                "Quesos Bartolomé",
                                title.getText(),
                                "The About Us page should load correctly");
        }

        @Test
        public void testUserNavigationMenuForRegularUser() {

                driver.get("http://localhost:4200/");

                // Open login page
                WebElement loginBtn = wait.until(ExpectedConditions
                                .elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                loginBtn.click();

                // Login as USER (Victor)
                WebElement usernameInput = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("Victor");
                passwordInput.sendKeys("password123");

                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

                // Accept alert
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();

                // Sidebar visible
                WebElement sidebar = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".menu")));

                // Regular USER should see:
                assertTrue(sidebar.getText().contains("Mi pedido"), "USER should see 'Mi pedido'");

                // And SHOULD NOT see admin entries
                assertFalse(sidebar.getText().contains("Pedidos"), "USER must NOT see 'Pedidos'");
                assertFalse(sidebar.getText().contains("Facturas"), "USER must NOT see 'Facturas'");
                assertFalse(sidebar.getText().contains("Gráficos"), "USER must NOT see 'Gráficos'");
                assertFalse(sidebar.getText().contains("Stock"), "USER must NOT see 'Stock'");
                assertFalse(sidebar.getText().contains("Clientes"), "USER must NOT see 'Clientes'");
        }

        @Test
        public void testUserNavigationMenuForAdmin() {

                driver.get("http://localhost:4200/");

                // Open login page
                WebElement loginBtn = wait.until(ExpectedConditions
                                .elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                loginBtn.click();

                // Login as ADMIN (German)
                WebElement usernameInput = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("German"); // ADMIN USERNAME
                passwordInput.sendKeys("password123");

                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

                // Accept alert
                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();

                // Sidebar visible
                WebElement sidebar = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".menu")));

                // ADMIN should NOT see "Mi pedido"
                assertFalse(sidebar.getText().contains("Mi pedido"), "ADMIN must NOT see 'Mi pedido'");

                // ADMIN should see all admin options
                assertTrue(sidebar.getText().contains("Pedidos"), "ADMIN should see 'Pedidos'");
                assertTrue(sidebar.getText().contains("Facturas"), "ADMIN should see 'Facturas'");
                assertTrue(sidebar.getText().contains("Gráficos"), "ADMIN should see 'Gráficos'");
                assertTrue(sidebar.getText().contains("Stock"), "ADMIN should see 'Stock'");
                assertTrue(sidebar.getText().contains("Clientes"), "ADMIN should see 'Clientes'");
        }

        @Test
        public void testNavigationMenuWhenNotLoggedIn() {

                // Open the application (fresh session)
                driver.get("http://localhost:4200/");

                // Wait for sidebar / menu to be visible
                WebElement sidebar = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".menu")));

                String menuText = sidebar.getText();

                // Always visible item
                assertTrue(menuText.contains("Acerca de nosotros"),
                                "The menu must always contain 'Acerca de nosotros' for anonymous users.");

                // Items that must NOT be visible for anonymous user
                assertFalse(menuText.contains("Mi pedido"), "Anonymous user must NOT see 'Mi pedido'");
                assertFalse(menuText.contains("Pedidos"), "Anonymous user must NOT see 'Pedidos'");
                assertFalse(menuText.contains("Facturas"), "Anonymous user must NOT see 'Facturas'");
                assertFalse(menuText.contains("Gráficos"), "Anonymous user must NOT see 'Gráficos'");
                assertFalse(menuText.contains("Stock"), "Anonymous user must NOT see 'Stock'");
                assertFalse(menuText.contains("Clientes"), "Anonymous user must NOT see 'Clientes'");

                // Check that auth buttons are visible (login/register)
                WebElement authButtons = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".auth-buttons")));

                // There should be a login and register button in the unauthenticated state
                assertTrue(authButtons.getText().contains("Iniciar Sesión"),
                                "Login button should be visible when not logged in.");
                assertTrue(authButtons.getText().contains("Registrarse"),
                                "Register button should be visible when not logged in.");
        }

}