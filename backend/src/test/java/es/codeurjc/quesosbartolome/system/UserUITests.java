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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
                if (driver != null)
                        driver.quit();
        }

        /**
         * Helper method for login
         */
        private void login(String username, String password) {
                driver.get("http://localhost:4200/");

                WebElement loginButton = wait.until(ExpectedConditions
                                .elementToBeClickable(By.xpath("//button[contains(text(),'Iniciar Sesión')]")));
                loginButton.click();

                WebElement usernameInput = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector("input[name='username']")));
                WebElement passwordInput = driver.findElement(By.cssSelector("input[name='password']"));
                usernameInput.sendKeys(username);
                passwordInput.sendKeys(password);

                WebElement submitButton = driver.findElement(By.cssSelector("button[type='submit']"));
                submitButton.click();

                Alert alert = SeleniumDialogHelper.waitForDialog(wait);
                String alertText = alert.getText();
                alert.accept();
                if (alertText.contains("incorrectas") || alertText.toLowerCase().contains("error")) {
                        throw new AssertionError("Login failed for '" + username + "': " + alertText);
                }
        }

        private WebElement openOwnProfile() {
                WebElement profileButton = wait.until(ExpectedConditions
                                .elementToBeClickable(By.cssSelector("button[aria-label='Mi perfil']")));
                profileButton.click();
                return wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".profile-container")));
        }

        private WebElement inputByLabel(WebElement container, String labelText) {
                return container.findElement(By.xpath(".//label[text()='" + labelText + "']/following-sibling::input"));
        }

        private void forceClick(WebElement element) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center'});", element);
                wait.until(ExpectedConditions.visibilityOf(element));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
        }

        @Order(1)
        @Test
        public void testUserProfileVisibleAfterLogin() {
                login("Tienda Artesanal de Riaza", "password123");

                // Click on profile button
                WebElement profileButton = wait.until(ExpectedConditions
                                .elementToBeClickable(By.cssSelector("button[aria-label='Mi perfil']")));
                profileButton.click();

                // Wait for profile container to be visible
                WebElement profileContainer = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".profile-container")));

                WebElement nameField = profileContainer
                                .findElement(By.xpath("//label[text()='Nombre']/following-sibling::input"));
                WebElement gmailField = profileContainer
                                .findElement(By.xpath("//label[text()='Gmail']/following-sibling::input"));
                WebElement directionField = profileContainer
                                .findElement(By.xpath("//label[text()='Dirección']/following-sibling::input"));
                WebElement nifField = profileContainer
                                .findElement(By.xpath("//label[text()='NIF']/following-sibling::input"));

                assertEquals("Tienda Artesanal de Riaza", nameField.getDomProperty("value"));
                assertEquals("riaza@ejemplo.com", gmailField.getDomProperty("value"));
                assertEquals("Plaza Mayor, Riaza", directionField.getDomProperty("value"));
                assertEquals("70000001A", nifField.getDomProperty("value"));

                // Validate avatar image
                WebElement avatarImg = profileContainer.findElement(By.cssSelector(".avatar-box img"));
                String src = avatarImg.getDomProperty("src");
                assertNotNull(src);
                assertTrue(src.contains("assets/avatar-default.png") || src.startsWith("blob:"),
                                "Avatar should be default or a generated blob");
        }

        @Order(2)
        @Test
        public void testAdminSeesClients() {
                // Login as ADMIN
                login("Admin", "password123");

                // Go directly to the users page
                driver.get("http://localhost:4200/users");

                // Wait for the users table to load
                List<WebElement> userRows = wait.until(ExpectedConditions
                                .visibilityOfAllElementsLocatedBy(By.cssSelector(".users-row")));

                assertFalse(userRows.isEmpty(), "Users list should not be empty");

                boolean foundUser = userRows.stream()
                                .anyMatch(row -> row.getText().contains("Tienda Artesanal de Riaza"));
                assertTrue(foundUser, "The users table should include Tienda Artesanal de Riaza");

                WebElement tiendaRow = userRows.stream()
                                .filter(row -> row.getText().contains("Tienda Artesanal de Riaza"))
                                .findFirst()
                                .orElseThrow();

                String name = tiendaRow.findElement(By.xpath("./span[2]")).getText();
                String gmail = tiendaRow.findElement(By.xpath("./span[3]")).getText();
                String direction = tiendaRow.findElement(By.xpath("./span[4]")).getText();
                String nif = tiendaRow.findElement(By.xpath("./span[5]")).getText();

                assertEquals("Tienda Artesanal de Riaza", name);
                assertEquals("riaza@ejemplo.com", gmail);
                assertEquals("Plaza Mayor, Riaza", direction);
                assertEquals("70000001A", nif);

                // Validate avatar
                WebElement avatarImg = tiendaRow.findElement(By.cssSelector("img.avatar"));
                String src = avatarImg.getDomProperty("src");
                assertNotNull(src);
                assertTrue(src.contains("assets/avatar-default") || src.startsWith("blob:"),
                                "Avatar should be default or a generated blob");

                // Validate ban button
                WebElement banButton = tiendaRow.findElement(By.cssSelector("button.btn-ban"));
                assertNotNull(banButton);
                assertEquals("Banear", banButton.getText());
        }

        @Order(3)
        @Test
        public void testEditProfileSuccess() {
                login("Tienda Artesanal de Riaza", "password123");
                WebElement profileContainer = openOwnProfile();

                String oldDirection = inputByLabel(profileContainer, "Dirección").getDomProperty("value");
                String newDirection = oldDirection + " Test";

                forceClick(profileContainer.findElement(By.xpath(".//button[normalize-space()='Editar']")));

                WebElement directionField = inputByLabel(profileContainer, "Dirección");
                directionField.clear();
                directionField.sendKeys(newDirection);

                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Confirmar edición')]")));

                wait.until(ExpectedConditions.textToBePresentInElementValue(
                                By.xpath("//label[text()='Dirección']/following-sibling::input"), newDirection));
                assertEquals(newDirection, inputByLabel(profileContainer, "Dirección").getDomProperty("value"));

                // Restore old value to avoid side effects
                forceClick(profileContainer.findElement(By.xpath(".//button[normalize-space()='Editar']")));
                WebElement directionRestore = inputByLabel(profileContainer, "Dirección");
                directionRestore.clear();
                directionRestore.sendKeys(oldDirection);
                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Confirmar edición')]")));
                wait.until(ExpectedConditions.textToBePresentInElementValue(
                                By.xpath("//label[text()='Dirección']/following-sibling::input"), oldDirection));
        }

        @Order(4)
        @Test
        public void testEditProfileCancelKeepsOriginalData() {
                login("Tienda Artesanal de Riaza", "password123");
                WebElement profileContainer = openOwnProfile();

                String originalDirection = inputByLabel(profileContainer, "Dirección").getDomProperty("value");

                forceClick(profileContainer.findElement(By.xpath(".//button[normalize-space()='Editar']")));

                WebElement directionField = inputByLabel(profileContainer, "Dirección");
                directionField.clear();
                directionField.sendKeys("DireccionQueNoDebeGuardarse");

                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Cancelar edición')]")));

                assertEquals(originalDirection, inputByLabel(profileContainer, "Dirección").getDomProperty("value"));
        }

        @Order(5)
        @Test
        public void testChangePasswordMismatchShowsValidationError() {
                login("Tienda Artesanal de Riaza", "password123");
                WebElement profileContainer = openOwnProfile();

                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Cambiar Contraseña')]")));

                inputByLabel(profileContainer, "Contraseña actual").sendKeys("password123");
                inputByLabel(profileContainer, "Nueva contraseña").sendKeys("newPassword123");
                inputByLabel(profileContainer, "Repite la nueva contraseña").sendKeys("differentPassword123");

                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Confirmar cambio')]")));

                WebElement error = wait.until(ExpectedConditions
                                .visibilityOfElementLocated(By.cssSelector(".form-message.error")));
                assertTrue(error.getText().contains("no coinciden"));
        }

        @Order(6)
        @Test
        public void testChangePasswordSuccessThenLoginWithNewPassword() {
                login("Tienda Artesanal de Riaza", "password123");
                WebElement profileContainer = openOwnProfile();

                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Cambiar Contraseña')]")));

                inputByLabel(profileContainer, "Contraseña actual").sendKeys("password123");
                inputByLabel(profileContainer, "Nueva contraseña").sendKeys("password1234");
                inputByLabel(profileContainer, "Repite la nueva contraseña").sendKeys("password1234");

                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Confirmar cambio')]")));

                Alert successAlert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(successAlert.getText().contains("actualizada"));
                successAlert.accept();

                // Restore old password in the same session — no logout/re-login needed.
                // The component resets isPasswordMode to false on success, so click the button again.
                forceClick(wait.until(ExpectedConditions
                                .elementToBeClickable(By.xpath("//button[contains(text(),'Cambiar Contraseña')]"))));
                inputByLabel(profileContainer, "Contraseña actual").sendKeys("password1234");
                inputByLabel(profileContainer, "Nueva contraseña").sendKeys("password123");
                inputByLabel(profileContainer, "Repite la nueva contraseña").sendKeys("password123");
                forceClick(profileContainer.findElement(By.xpath(".//button[contains(text(),'Confirmar cambio')]")));
                Alert restoreAlert = SeleniumDialogHelper.waitForDialog(wait);
                assertTrue(restoreAlert.getText().contains("actualizada"));
                restoreAlert.accept();
        }

        @Order(7)
        @Test
        public void testAdminBansUserAndProfileShowsBannedMessage() {
                login("Admin", "password123");

                driver.get("http://localhost:4200/users");

                WebElement userRow = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'users-row')][.//span[normalize-space()='Colmado Sepulveda']]")));

                WebElement banButton = userRow.findElement(By.cssSelector("button.btn-ban"));

                if (banButton.getText().trim().equalsIgnoreCase("Desbanear")) {
                        forceClick(banButton);
                        Alert unbanConfirm = SeleniumDialogHelper.waitForDialog(wait);
                        unbanConfirm.accept();
                        wait.until(ExpectedConditions.textToBePresentInElement(banButton, "Banear"));
                }

                forceClick(banButton);
                Alert banConfirm = SeleniumDialogHelper.waitForDialog(wait);
                banConfirm.accept();

                wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.xpath("//div[contains(@class,'users-row')][.//span[normalize-space()='Colmado Sepulveda']]//span[contains(@class,'status-pill') and normalize-space()='BANEADO']")));

                WebElement bannedUserRow = wait.until(ExpectedConditions.elementToBeClickable(
                                By.xpath("//div[contains(@class,'users-row')][.//span[normalize-space()='Colmado Sepulveda']]")));
                forceClick(bannedUserRow);

                WebElement bannedMessage = wait.until(ExpectedConditions.visibilityOfElementLocated(
                                By.cssSelector(".banned-notice")));

                assertTrue(bannedMessage.getText().toLowerCase().contains("baneado"));
        }

}
