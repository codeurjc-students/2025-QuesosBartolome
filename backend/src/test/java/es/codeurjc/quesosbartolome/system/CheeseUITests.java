package es.codeurjc.quesosbartolome.system;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.List;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.*;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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

        private void login(String username, String password) {
                driver.get("http://localhost:4200/");
                WebElement loginBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(text(),'Iniciar Sesión')]")));

                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                loginBtn);
                try {
                        Thread.sleep(200);
                        new Actions(driver)
                                        .moveToElement(loginBtn)
                                        .pause(Duration.ofMillis(200))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
                }

                WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys(username);
                passwordInput.sendKeys(password);

                WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button[type='submit']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                submitButton);
                try {
                        Thread.sleep(200);
                        new Actions(driver)
                                        .moveToElement(submitButton)
                                        .pause(Duration.ofMillis(200))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                }

                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
        }

        @Test
        @Order(1)
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
        @Order(2)
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
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                aboutBtn);
                try {
                        Thread.sleep(300);
                        new Actions(driver)
                                        .moveToElement(aboutBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", aboutBtn);
                }

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
        @Order(3)
        public void testUserNavigationMenuForRegularUser() {

                driver.get("http://localhost:4200/");

                // Open login page
                WebElement loginBtn = wait.until(ExpectedConditions
                                .elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                loginBtn);
                try {
                        Thread.sleep(200);
                        new Actions(driver)
                                        .moveToElement(loginBtn)
                                        .pause(Duration.ofMillis(200))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
                }

                // Login as USER (Victor)
                WebElement usernameInput = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("Victor");
                passwordInput.sendKeys("password123");

                WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button[type='submit']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                submitButton);
                try {
                        Thread.sleep(200);
                        new Actions(driver)
                                        .moveToElement(submitButton)
                                        .pause(Duration.ofMillis(200))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                }

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
        @Order(4)
        public void testUserNavigationMenuForAdmin() {

                driver.get("http://localhost:4200/");

                // Open login page
                WebElement loginBtn = wait.until(ExpectedConditions
                                .elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                loginBtn);
                try {
                        Thread.sleep(200);
                        new Actions(driver)
                                        .moveToElement(loginBtn)
                                        .pause(Duration.ofMillis(200))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", loginBtn);
                }

                // Login as ADMIN (German)
                WebElement usernameInput = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys("German"); // ADMIN USERNAME
                passwordInput.sendKeys("password123");

                WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button[type='submit']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                submitButton);
                try {
                        Thread.sleep(200);
                        new Actions(driver)
                                        .moveToElement(submitButton)
                                        .pause(Duration.ofMillis(200))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitButton);
                }

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
        @Order(5)
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

        @Test
        @Order(6)
        public void testCreateCheeseSuccessfully() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Go to New Cheese page
                driver.get("http://localhost:4200/newCheese");
                wait.until(ExpectedConditions.urlContains("/newCheese"));

                // 3. Fill the form
                driver.findElement(By.id("name")).sendKeys("Nuevo Queso creado Selenium");
                driver.findElement(By.id("price")).sendKeys("12.50");
                driver.findElement(By.id("description")).sendKeys("Queso creado ");

                Select typeSelect = new Select(driver.findElement(By.id("type")));
                typeSelect.selectByVisibleText("Cremoso");

                WebElement manufacture = driver.findElement(By.id("manufactureDate"));
                manufacture.sendKeys("2024-01-24");

                WebElement expiration = driver.findElement(By.id("expirationDate"));
                expiration.sendKeys("2025-01-24");
                
                // 4. Submit form - ensure button is in view and clickable
                WebElement createBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

                // Scroll to button to ensure it's visible
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", createBtn);
                Thread.sleep(300); // Brief pause after scroll

                // Try Actions first
                try {
                        new Actions(driver)
                                        .moveToElement(createBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        // Fallback to JavaScript click if Actions fails
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
                }

                // 5. Wait for success alert
                Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
                successAlert.accept();

                // 6. Wait for redirect
                wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));

                // 7. Check cheese appears
                WebElement cardGrid = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                boolean exists = cardGrid.getText().contains("Nuevo Queso creado Selenium");
                assertTrue(exists, "The newly created cheese should appear in the cheese list.");
        }

        @Test
        @Order(7)
        public void testCreateCheeseMissingFieldsShowsAlert() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Go to New Cheese page
                driver.get("http://localhost:4200/newCheese");
                wait.until(ExpectedConditions.urlContains("/newCheese"));

                // 3. Fill only some fields
                driver.findElement(By.id("name")).sendKeys("Queso Incompleto");
                driver.findElement(By.id("price")).sendKeys("10.00");

                // 4. Submit form - ensure button is in view and clickable
                WebElement createBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

                // Scroll to button to ensure it's visible
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                createBtn);
                Thread.sleep(300); // Brief pause after scroll

                // Try Actions first
                try {
                        new Actions(driver)
                                        .moveToElement(createBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        // Fallback to JavaScript click if Actions fails
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
                }

                // 5. Capture alert
                Alert errorAlert = wait.until(ExpectedConditions.alertIsPresent());
                String alertText = errorAlert.getText();
                errorAlert.accept();

                assertEquals("Todos los campos son obligatorios", alertText);
        }

        @Test
        @Order(8)
        public void testCreateCheeseWithExpirationDateBeforeManufactureDateShowsAlert() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Go to New Cheese page
                driver.get("http://localhost:4200/newCheese");
                wait.until(ExpectedConditions.urlContains("/newCheese"));

                // 3. Fill all fields but with expiration date before manufacture date
                driver.findElement(By.id("name")).sendKeys("Queso Fecha Inválida");
                driver.findElement(By.id("price")).sendKeys("15.00");
                driver.findElement(By.id("description")).sendKeys("Queso con fechas incorrectas");

                Select typeSelect = new Select(driver.findElement(By.id("type")));
                typeSelect.selectByVisibleText("Cremoso");

                // Set manufacture date to 2025-01-24 and expiration date to 2024-01-24 (before
                // manufacture)
                WebElement manufacture = driver.findElement(By.id("manufactureDate"));
                manufacture.sendKeys("2025-01-24");

                WebElement expiration = driver.findElement(By.id("expirationDate"));
                expiration.sendKeys("2024-01-24");

                // 4. Submit form - ensure button is in view and clickable
                WebElement createBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));

                // Scroll to button to ensure it's visible
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                createBtn);
                Thread.sleep(300); // Brief pause after scroll

                // Try Actions first
                try {
                        new Actions(driver)
                                        .moveToElement(createBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        // Fallback to JavaScript click if Actions fails
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
                }

                // 5. Capture alert and verify error message
                Alert errorAlert = wait.until(ExpectedConditions.alertIsPresent());
                String alertText = errorAlert.getText();
                errorAlert.accept();

                assertEquals("La fecha de caducidad debe ser posterior a la de fabricación", alertText,
                                "Should show error when expiration date is before manufacture date");
        }

        @Test
        @Order(9)
        public void testEditCheeseSuccessfully() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Crear un queso auxiliar para editar
                driver.get("http://localhost:4200/newCheese");
                wait.until(ExpectedConditions.urlContains("/newCheese"));

                driver.findElement(By.id("name")).sendKeys("QuesoAEditar");
                driver.findElement(By.id("price")).sendKeys("18.00");
                driver.findElement(By.id("description")).sendKeys("Descripción inicial");

                Select typeSelect = new Select(driver.findElement(By.id("type")));
                typeSelect.selectByVisibleText("Cremoso");

                driver.findElement(By.id("manufactureDate")).sendKeys("2024-01-01");
                driver.findElement(By.id("expirationDate")).sendKeys("2025-01-01");

                WebElement createBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                createBtn);
                Thread.sleep(300);

                try {
                        new Actions(driver)
                                        .moveToElement(createBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
                }

                Alert createAlert = wait.until(ExpectedConditions.alertIsPresent());
                createAlert.accept();

                // 3. Esperar a que cargue la lista de quesos
                wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                Thread.sleep(500);

                // 4. Clickar en el queso creado
                WebElement cheeseCard = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'card')]//p[text()='QuesoAEditar']/ancestor::div[contains(@class,'card')]")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                cheeseCard);
                Thread.sleep(300);

                try {
                        new Actions(driver)
                                        .moveToElement(cheeseCard)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cheeseCard);
                }

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 5. Clickar botón Edit
                WebElement editBtn = wait
                                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".edit-btn")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", editBtn);
                Thread.sleep(300);

                try {
                        new Actions(driver)
                                        .moveToElement(editBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", editBtn);
                }

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+/edit"));

                // 6. Modificar nombre y precio
                WebElement nameInput = driver.findElement(By.id("name"));
                nameInput.clear();
                nameInput.sendKeys("QuesoEditadoOK");

                WebElement priceInput = driver.findElement(By.id("price"));
                priceInput.clear();
                priceInput.sendKeys("22.50");
                driver.findElement(By.id("description")).clear();
                driver.findElement(By.id("description")).sendKeys("Descripción editada");

                Select typeSelect2 = new Select(driver.findElement(By.id("type")));
                typeSelect2.selectByVisibleText("Cremoso");

                driver.findElement(By.id("manufactureDate")).clear();
                driver.findElement(By.id("manufactureDate")).sendKeys("2024-01-01");

                driver.findElement(By.id("expirationDate")).clear();
                driver.findElement(By.id("expirationDate")).sendKeys("2025-01-01");

                // 7. Submit
                WebElement submitBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                submitBtn);
                Thread.sleep(300);

                try {
                        new Actions(driver)
                                        .moveToElement(submitBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", submitBtn);
                }

                // 8. Verificar alert de éxito
                Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Queso actualizado correctamente", successAlert.getText());
                successAlert.accept();
        }

        @Test
        @Order(10)
        public void testDeleteCheeseSuccessfully() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. First, create a cheese to delete
                driver.get("http://localhost:4200/newCheese");
                wait.until(ExpectedConditions.urlContains("/newCheese"));

                driver.findElement(By.id("name")).sendKeys("Queso Para Borrar");
                driver.findElement(By.id("price")).sendKeys("10.00");
                driver.findElement(By.id("description")).sendKeys("Este queso será borrado en el test");

                Select typeSelect = new Select(driver.findElement(By.id("type")));
                typeSelect.selectByVisibleText("Cremoso");

                driver.findElement(By.id("manufactureDate")).sendKeys("2024-01-01");
                driver.findElement(By.id("expirationDate")).sendKeys("2025-01-01");

                WebElement createBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                createBtn);
                Thread.sleep(300);

                try {
                        new Actions(driver)
                                        .moveToElement(createBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", createBtn);
                }

                Alert createAlert = wait.until(ExpectedConditions.alertIsPresent());
                createAlert.accept();

                wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));

                // 3. Navigate to the created cheese
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                Thread.sleep(500);

                WebElement cheeseCard = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Queso Para Borrar']/ancestor::div[contains(@class,'card')]")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                cheeseCard);
                Thread.sleep(300);

                try {
                        new Actions(driver)
                                        .moveToElement(cheeseCard)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cheeseCard);
                }

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 4. Click Delete button
                WebElement deleteBtn = wait
                                .until(ExpectedConditions.elementToBeClickable(By.cssSelector(".delete-btn")));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});",
                                deleteBtn);
                Thread.sleep(300);

                try {
                        new Actions(driver)
                                        .moveToElement(deleteBtn)
                                        .pause(Duration.ofMillis(300))
                                        .click()
                                        .perform();
                } catch (Exception e) {
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteBtn);
                }

                // 5. Accept confirmation dialog
                Alert confirmAlert = wait.until(ExpectedConditions.alertIsPresent());
                confirmAlert.accept();

                // 6. Wait for success alert
                Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Queso eliminado correctamente", successAlert.getText());
                successAlert.accept();
        }

}