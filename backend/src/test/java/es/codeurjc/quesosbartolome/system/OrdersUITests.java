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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import es.codeurjc.quesosbartolome.repository.OrderRepository;
import es.codeurjc.quesosbartolome.repository.UserRepository;

@SpringBootTest(classes = es.codeurjc.quesosbartolome.QuesosbartolomeApplication.class)
public class OrdersUITests {

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private OrderRepository orderRepository;

        private WebDriver driver;
        private WebDriverWait wait;

        @BeforeEach
        public void setup() {
                ChromeOptions options = new ChromeOptions();
                options.addArguments("--headless=new");
                options.addArguments("--window-size=1920,1080");
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

                Alert alert = SeleniumDialogHelper.waitForDialog(wait);
                alert.accept();
                wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.xpath("//button[contains(.,'Cerrar Sesión')]")));
        }

        private void registerUser(String username, String password) {
                driver.get("http://localhost:4200/auth/register");

                String nif = String.format("%08dA", Math.abs((int) (System.nanoTime() % 100000000L)));

                // Wait for all form elements to be present and interactable
                WebElement nombreField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nombre")));
                WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
                WebElement direccionField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("direccion")));
                WebElement nifField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("nif")));
                WebElement passwordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password")));
                WebElement confirmPasswordField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirm-password")));

                // Clear fields before sending keys to ensure clean input
                nombreField.clear();
                nombreField.sendKeys(username);
                
                emailField.clear();
                emailField.sendKeys(username + "@example.com");
                
                direccionField.clear();
                direccionField.sendKeys("Calle Falsa 123");
                
                nifField.clear();
                nifField.sendKeys(nif);
                
                passwordField.clear();
                passwordField.sendKeys(password);
                
                confirmPasswordField.clear();
                confirmPasswordField.sendKeys(password);

                WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector("button[type='submit']")));
                submitButton.click();

                Alert alert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(alert.getText().contains("Registro exitoso"));
                alert.accept();
        }

        private void logout() {
                driver.get("http://localhost:4200/");
                WebElement logoutBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//button[contains(.,'Cerrar Sesión')]")));

                wait.until(ExpectedConditions.elementToBeClickable(logoutBtn)).click();
        }

        private void createOrderAsUser() {
                // Go to cheese details page
                driver.get("http://localhost:4200/cheeses/2");

                WebElement boxesInput = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".cajas-input")));
                boxesInput.sendKeys("1");

                WebElement addButton = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector(".add-btn")));
                new Actions(driver).moveToElement(addButton).pause(200).click().perform();

                Alert successAlert = SeleniumDialogHelper.waitForDialog(wait);
                successAlert.accept();

                // Go to My Order page
                driver.get("http://localhost:4200/myorder");

                // Confirm the order
                WebElement confirmBtn = wait.until(
                                ExpectedConditions.elementToBeClickable(By.cssSelector(".btn.confirm")));
                confirmBtn.click();

                Alert orderAlert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(orderAlert.getText().contains("Pedido realizado correctamente"));
                orderAlert.accept();
        }

        private Long getLatestOrderIdForUser(String username) {
                Long userId = userRepository.findByName(username)
                                .map(user -> user.getId())
                                .orElseThrow(() -> new AssertionError("User not found: " + username));

                return orderRepository
                                .findByUserIdOrderByOrderDateDesc(userId,
                                                org.springframework.data.domain.Pageable.unpaged())
                                .stream()
                                .findFirst()
                                .map(order -> order.getId())
                                .orElseThrow(() -> new AssertionError("No order found for user " + username));
        }

        private void openOrdersPageAsUser() {
                driver.get("http://localhost:4200/orders");
                wait.until(ExpectedConditions.urlContains("/orders"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".orders-card")));
        }

        private List<WebElement> waitForOrderRowsWithRetry() {
                for (int attempt = 0; attempt < 3; attempt++) {
                        try {
                                wait.until(driver -> !driver.findElements(By.cssSelector(".orders-row")).isEmpty());
                                List<WebElement> rows = driver.findElements(By.cssSelector(".orders-row"));
                                if (!rows.isEmpty()) {
                                        return rows;
                                }
                        } catch (TimeoutException ignored) {
                                // Retry by refreshing because Angular data loading can be delayed in headless
                                // mode.
                        }
                        driver.navigate().refresh();
                        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".orders-card")));
                }
                return driver.findElements(By.cssSelector(".orders-row"));
        }

        private boolean orderIdExistsInCurrentList(String orderId) {
                List<WebElement> rows = driver.findElements(By.cssSelector(".orders-row"));
                return rows.stream().anyMatch(row -> {
                        List<WebElement> cells = row.findElements(By.cssSelector("span"));
                        return !cells.isEmpty() && cells.get(0).getText().trim().equals(orderId);
                });
        }

        private void assertOrderDisappearsFromList(String orderId) {
                wait.until(ExpectedConditions.urlContains("/orders"));
                wait.until(driver -> !orderIdExistsInCurrentList(orderId));
                assertFalse(orderIdExistsInCurrentList(orderId),
                                "Processed order should not appear in pending orders list");
        }

        @Test
        @Order(1)
        public void testAdminCanSeeCreatedOrder() {

                String username = "Victor" + System.currentTimeMillis();

                registerUser(username, "password123");
                login(username, "password123");
                createOrderAsUser();
                Long orderId = getLatestOrderIdForUser(username);
                logout();

                login("German", "password123");
                driver.get("http://localhost:4200/orders/" + orderId + "/preview");

                WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(".preview-card h1")));
                assertEquals("Vista previa del pedido", title.getText().trim());
        }

        @Test
        public void testProcessOrderPreviewAndConfirmRemovesOrderFromList() {
                String username = "Victor" + System.currentTimeMillis();

                registerUser(username, "password123");
                login(username, "password123");
                createOrderAsUser();
                Long orderId = getLatestOrderIdForUser(username);
                logout();

                login("German", "password123");
                driver.get("http://localhost:4200/orders/" + orderId + "/preview");

                WebElement title = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(".preview-card h1")));
                assertEquals("Vista previa del pedido", title.getText().trim());

                String previewText = driver.findElement(By.cssSelector(".preview-card")).getText();
                assertTrue(previewText.contains("Cliente:"));
                assertTrue(previewText.contains("Fecha:"));
                assertTrue(previewText.contains("Producto"));
                assertTrue(previewText.contains("Cajas"));
                assertTrue(previewText.contains("Peso"));
                assertTrue(previewText.contains("Total"));

                WebElement confirmBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector(".btn.btn-confirm")));
                confirmBtn.click();

                Alert confirmDialog = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(confirmDialog.getText().contains("Confirmar"));
                confirmDialog.accept();

                Alert confirmAlert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(confirmAlert.getText().contains("Factura creada correctamente"));
                confirmAlert.accept();

                assertOrderDisappearsFromList(String.valueOf(orderId));
        }

        @Test
        public void testRejectOrderRemovesOrderFromList() {
                String username = "Victor" + System.currentTimeMillis();

                registerUser(username, "password123");
                login(username, "password123");
                createOrderAsUser();
                Long orderId = getLatestOrderIdForUser(username);
                logout();

                login("German", "password123");
                driver.get("http://localhost:4200/orders/" + orderId + "/preview");

                WebElement rejectBtn = wait.until(ExpectedConditions.elementToBeClickable(
                                By.cssSelector(".btn.btn-cancel")));
                rejectBtn.click();

                Alert rejectConfirm = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(rejectConfirm.getText().contains("Rechazar"));
                rejectConfirm.accept();

                Alert rejectAlert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(rejectAlert.getText().contains("Pedido rechazado"));
                rejectAlert.accept();

                assertOrderDisappearsFromList(String.valueOf(orderId));
        }

        @Test
        public void testUserCanSeeOnlyOwnOrdersInOrdersPage() {
                String username = "Victor" + System.currentTimeMillis();

                registerUser(username, "password123");
                login(username, "password123");
                createOrderAsUser();

                openOrdersPageAsUser();

                List<WebElement> orderRows = waitForOrderRowsWithRetry();

                assertFalse(orderRows.isEmpty(), "User should see at least one own order");
                assertTrue(orderRows.stream().allMatch(row -> row.getText().contains(username)),
                                "User orders list should only contain own orders");

                WebElement firstRow = orderRows.get(0);
                assertFalse(firstRow.findElements(By.cssSelector(".status-tag")).isEmpty(),
                                "User row should show status tag");
                assertTrue(firstRow.findElements(By.cssSelector(".btn-process")).isEmpty(),
                                "User row should not show admin process button");
        }

        @Test
        public void testUserCanOpenOwnOrderPreviewInReadOnlyMode() {
                String username = "Victor" + System.currentTimeMillis();

                registerUser(username, "password123");
                login(username, "password123");
                createOrderAsUser();

                openOrdersPageAsUser();

                WebElement userOrderRow = waitForOrderRowsWithRetry().stream()
                                .filter(row -> row.getText().contains(username))
                                .findFirst()
                                .orElseThrow(() -> new AssertionError("No order row found for user " + username));

                userOrderRow.click();

                wait.until(ExpectedConditions.urlContains("/orders/"));
                wait.until(ExpectedConditions.urlContains("/preview"));
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".preview-card")));

                assertTrue(driver.findElements(By.cssSelector(".btn.btn-confirm")).isEmpty(),
                                "User preview should not show confirm button");
                assertTrue(driver.findElements(By.cssSelector(".btn.btn-cancel")).isEmpty(),
                                "User preview should not show reject button");
        }
}
