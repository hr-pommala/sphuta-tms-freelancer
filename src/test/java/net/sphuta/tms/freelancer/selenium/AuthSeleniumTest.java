package net.sphuta.tms.freelancer.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthSeleniumTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/login";

    @BeforeAll
    public static void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @Order(1)
    void loginWithEncryptedPassword() {
        driver.get(BASE_URL);
        // Simulate UI encryption (for demo, just use plain text, as real encryption is JS-side)
        String encryptedPassword = "U2FsdGVkX1+..."; // Replace with a real encrypted value from UI for real test
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("john@example.com");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).sendKeys(encryptedPassword);
        wait.until(ExpectedConditions.elementToBeClickable(By.id("loginBtn"))).click();
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        assertTrue(msg.getText().toLowerCase().contains("welcome") || msg.getText().toLowerCase().contains("success"));
    }
}

