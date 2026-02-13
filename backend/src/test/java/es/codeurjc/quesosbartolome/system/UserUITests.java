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

                Alert alert = wait.until(ExpectedConditions.alertIsPresent());
                alert.accept();
        }

        @Test
        public void testUserProfileVisibleAfterLogin() {
                login("Victor", "password123");

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

                assertEquals("Victor", nameField.getDomProperty("value"));
                assertEquals("victor@example.com", gmailField.getDomProperty("value"));
                assertEquals("123 Main St", directionField.getDomProperty("value"));
                assertEquals("12345678A", nifField.getDomProperty("value"));

                // Validate avatar image
                WebElement avatarImg = profileContainer.findElement(By.cssSelector(".avatar-box img"));
                String src = avatarImg.getDomProperty("src");
                assertNotNull(src);
                assertTrue(src.contains("assets/avatar-default.png") || src.startsWith("blob:"),
                                "Avatar should be default or a generated blob");
        }

        @Test
        public void testAdminSeesClients() {
                // Login as ADMIN
                login("German", "password123");

                // Go directly to the users page
                driver.get("http://localhost:4200/users");

                // Wait for the users table to load
                List<WebElement> userRows = wait.until(ExpectedConditions
                                .visibilityOfAllElementsLocatedBy(By.cssSelector(".users-row")));

                assertFalse(userRows.isEmpty(), "Users list should not be empty");

                // Check that Victor appears
                boolean foundVictor = userRows.stream()
                                .anyMatch(row -> row.getText().contains("Victor"));
                assertTrue(foundVictor, "The users table should include Victor");

                // Validate Victor's fields
                WebElement victorRow = userRows.stream()
                                .filter(row -> row.getText().contains("Victor"))
                                .findFirst()
                                .orElseThrow();

                String name = victorRow.findElement(By.xpath("./span[2]")).getText();
                String gmail = victorRow.findElement(By.xpath("./span[3]")).getText();
                String direction = victorRow.findElement(By.xpath("./span[4]")).getText();
                String nif = victorRow.findElement(By.xpath("./span[5]")).getText();

                assertEquals("Victor", name);
                assertEquals("victor@example.com", gmail);
                assertEquals("123 Main St", direction);
                assertEquals("12345678A", nif);

                // Validate avatar
                WebElement avatarImg = victorRow.findElement(By.cssSelector("img.avatar"));
                String src = avatarImg.getDomProperty("src");
                assertNotNull(src);
                assertTrue(src.contains("assets/avatar-default") || src.startsWith("blob:"),
                                "Avatar should be default or a generated blob");

                // Validate ban button
                WebElement banButton = victorRow.findElement(By.cssSelector("button.btn-ban"));
                assertNotNull(banButton);
                assertEquals("Banear", banButton.getText());
        }

}
