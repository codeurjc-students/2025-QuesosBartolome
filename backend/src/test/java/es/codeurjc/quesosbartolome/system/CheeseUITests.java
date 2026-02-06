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
                loginBtn.click();

                WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));

                usernameInput.sendKeys(username);
                passwordInput.sendKeys(password);

                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

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
        @Order(3)
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
        @Order(4)
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

        @SuppressWarnings("deprecation")
        @Test
        @Order(9)
        public void testEditCheeseSuccessfully() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Primero crear un queso auxiliar para editar
                driver.get("http://localhost:4200/newCheese");
                wait.until(ExpectedConditions.urlContains("/newCheese"));

                driver.findElement(By.id("name")).sendKeys("QuesoParaEditar");
                driver.findElement(By.id("price")).sendKeys("20.00");
                driver.findElement(By.id("description")).sendKeys("Este queso será editado en el test");

                Select typeSelect = new Select(driver.findElement(By.id("type")));
                typeSelect.selectByVisibleText("Pasta prensada");

                WebElement manufactureDateInput = driver.findElement(By.id("manufactureDate"));
                manufactureDateInput.sendKeys("2024-01-15");

                WebElement expirationDateInput = driver.findElement(By.id("expirationDate"));
                expirationDateInput.sendKeys("2025-01-15");

                WebElement createBtn = driver.findElement(By.cssSelector("button[type='submit']"));
                createBtn.click();

                Alert createAlert = wait.until(ExpectedConditions.alertIsPresent());
                createAlert.accept();

                // 3. Esperar a volver a la página de inicio y buscar el queso creado
                wait.until(ExpectedConditions.urlToBe("http://localhost:4200/"));
                Thread.sleep(1000);

                driver.get("http://localhost:4200/cheeses");
                wait.until(ExpectedConditions.urlContains("/cheeses"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));

                // 4. Navegar al queso auxiliar creado
                WebElement auxiliarCheeseCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='QuesoParaEditar']/ancestor::div[contains(@class,'card')]")));
                auxiliarCheeseCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 5. Click Edit button
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

                // 6. Wait for navigation to edit page
                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+/edit"));

                // 7. Verify form is populated with existing data
                WebElement nameInput = driver.findElement(By.id("name"));
                assertEquals("QuesoParaEditar", nameInput.getAttribute("value"), "Name should be pre-filled");

                // 8. Modify fields
                nameInput.clear();
                nameInput.sendKeys("QuesoEditado");

                WebElement priceInput = driver.findElement(By.id("price"));
                priceInput.clear();
                priceInput.sendKeys("25.99");

                WebElement descriptionInput = driver.findElement(By.id("description"));
                descriptionInput.clear();
                descriptionInput.sendKeys("Descripción actualizada del queso auxiliar");

                // 9. Submit form
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

                // 10. Verify success alert
                Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Queso actualizado correctamente", successAlert.getText());
                successAlert.accept();

                // 11. Verify redirect to cheese details page
                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 12. Verify updated data is displayed
                WebElement title = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-title")));
                assertEquals("QuesoEditado", title.getText(), "Cheese name should be updated");
        }

        @Test
        @Order(10)
        public void testEditCheeseWithInvalidDates() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Navigate to a cheese details page
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                WebElement azulCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Azul']/ancestor::div[contains(@class,'card')]")));
                azulCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 3. Click Edit button
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

                // 4. Set invalid dates (expiration before manufacture)
                WebElement manufactureInput = driver.findElement(By.id("manufactureDate"));
                manufactureInput.clear();
                manufactureInput.sendKeys("2025-06-01");

                WebElement expirationInput = driver.findElement(By.id("expirationDate"));
                expirationInput.clear();
                expirationInput.sendKeys("2025-01-01");

                // 5. Submit form
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

                // 6. Verify error alert
                Alert errorAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("La fecha de caducidad debe ser posterior a la de fabricación", errorAlert.getText());
                errorAlert.accept();
        }

        @Test
        @Order(11)
        public void testEditCheeseWithMissingFields() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Crear un queso auxiliar primero para editar
                driver.get("http://localhost:4200/newCheese");
                wait.until(ExpectedConditions.urlContains("/newCheese"));

                driver.findElement(By.id("name")).sendKeys("QuesoValidacion");
                driver.findElement(By.id("price")).sendKeys("15.00");
                driver.findElement(By.id("description")).sendKeys("Test validación");

                Select createTypeSelect = new Select(driver.findElement(By.id("type")));
                createTypeSelect.selectByVisibleText("Curado");

                driver.findElement(By.id("manufactureDate")).sendKeys("2024-01-10");
                driver.findElement(By.id("expirationDate")).sendKeys("2025-01-10");

                driver.findElement(By.cssSelector("button[type='submit']")).click();
                Alert createAlert = wait.until(ExpectedConditions.alertIsPresent());
                createAlert.accept();
                wait.until(ExpectedConditions.urlToBe("http://localhost:4200/"));
                Thread.sleep(1000);
                // 3. Navigate to cheese details and click edit
                driver.get("http://localhost:4200/cheeses");
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                WebElement cheeseCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='QuesoValidacion']/ancestor::div[contains(@class,'card')]")));
                cheeseCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

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

                // 3. Clear name field using JavaScript to ensure it's truly empty
                WebElement nameInput = driver.findElement(By.id("name"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].value = '';", nameInput);
                // Trigger blur event to make Angular detect the change
                ((JavascriptExecutor) driver).executeScript(
                                "arguments[0].dispatchEvent(new Event('input')); arguments[0].dispatchEvent(new Event('blur'));",
                                nameInput);

                Thread.sleep(300); // Brief pause for validation to process

                // 4. Try to submit
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

                // 5. Verify error alert
                Alert errorAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Todos los campos son obligatorios", errorAlert.getText());
                errorAlert.accept();
        }

        @Test
        @Order(12)
        public void testRegularUserCannotSeeEditButton() {

                // 1. Login as regular USER
                login("Victor", "password123");

                // 2. Navigate to a cheese details page
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 3. Verify edit button is NOT visible
                assertTrue(driver.findElements(By.cssSelector(".edit-btn")).isEmpty(),
                                "Regular user should NOT see edit button");
        }

        @Test
        @Order(13)
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
                WebElement cheeseCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Queso Para Borrar']/ancestor::div[contains(@class,'card')]")));
                cheeseCard.click();

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
                assertTrue(confirmAlert.getText().contains("¿Estás seguro de que quieres eliminar el queso"));
                confirmAlert.accept();

                // 6. Wait for success alert
                Alert successAlert = wait.until(ExpectedConditions.alertIsPresent());
                assertEquals("Queso eliminado correctamente", successAlert.getText());
                successAlert.accept();

                // 7. Verify redirect to cheese list
                wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));

                // 8. Navigate away and back to force complete reload of cheese list
                driver.get("http://localhost:4200/about-us");
                wait.until(ExpectedConditions.urlContains("/about-us"));
                Thread.sleep(500);

                driver.get("http://localhost:4200/cheeses");
                wait.until(ExpectedConditions.urlToBe("http://localhost:4200/cheeses"));

                // Wait for card grid to be visible and fully loaded
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                Thread.sleep(1500); // Give extra time for all cheeses to render

                List<WebElement> cheeseNames = driver.findElements(By.cssSelector(".card-body p"));

                // Check using contains instead of equalsIgnoreCase for more lenient matching
                boolean cheeseExists = cheeseNames.stream()
                                .anyMatch(el -> {
                                        String text = el.getText();
                                        return text != null && text.toLowerCase().contains("Queso Para Borrar");
                                });

                assertFalse(cheeseExists, "Deleted cheese should not appear in the list");
        }

        @Test
        @Order(14)
        public void testDeleteCheeseCancelled() throws InterruptedException {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Navigate to a cheese details page (use Semicurado as it's always
                // available)
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 2a. Verify we landed on the correct cheese page
                WebElement actualTitle = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cheese-title")));
                assertEquals("Semicurado", actualTitle.getText(), "Should have navigated to Semicurado cheese");

                // 3. Click Delete button
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

                // 4. Dismiss (cancel) confirmation dialog
                Alert confirmAlert = wait.until(ExpectedConditions.alertIsPresent());
                confirmAlert.dismiss();

                // 5. Verify we're still on the cheese details page
                Thread.sleep(500); // Brief wait to ensure no redirect happens
                assertTrue(driver.getCurrentUrl().matches("http://localhost:4200/cheeses/\\d+"),
                                "Should stay on cheese details page when delete is cancelled");

                // 6. Verify cheese details are still visible
                WebElement title = driver.findElement(By.cssSelector(".cheese-title"));
                assertEquals("Semicurado", title.getText(), "Cheese should still be displayed");
        }

        @Test
        @Order(15)
        public void testRegularUserCannotSeeDeleteButton() {

                // 1. Login as regular USER
                login("Victor", "password123");

                // 2. Navigate to a cheese details page (use Semicurado)
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 3. Verify delete button is NOT visible
                assertTrue(driver.findElements(By.cssSelector(".delete-btn")).isEmpty(),
                                "Regular user should NOT see delete button");
        }

        @Test
        @Order(16)
        public void testAdminCanSeeBothEditAndDeleteButtons() {

                // 1. Login as ADMIN
                login("German", "password123");

                // 2. Navigate to a cheese details page
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card-grid")));
                WebElement semicuradoCard = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'card')]//p[text()='Semicurado']/ancestor::div[contains(@class,'card')]")));
                semicuradoCard.click();

                wait.until(ExpectedConditions.urlMatches("http://localhost:4200/cheeses/\\d+"));

                // 3. Verify both buttons are visible
                WebElement editBtn = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".edit-btn")));
                assertTrue(editBtn.isDisplayed(), "ADMIN should see edit button");

                WebElement deleteBtn = wait
                                .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".delete-btn")));
                assertTrue(deleteBtn.isDisplayed(), "ADMIN should see delete button");
        }

}