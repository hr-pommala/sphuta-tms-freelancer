package net.sphuta.tms.freelancer.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SettingsPreferencesSeleniumTest {
    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/settings-preferences-form";

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
    void createPreferencesTest() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId"))).sendKeys("2001");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("theme"))).sendKeys("dark");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn"))).click();
        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        assertTrue(msg.getText().contains("Preferences saved successfully"));
    }

    @Test
    @Order(2)
    void createPreferencesWithMissingFieldsTest() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId"))).sendKeys("");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("theme"))).sendKeys("");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn"))).click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage")));
        assertTrue(errorMsg.getText().toLowerCase().contains("required"));
    }

    @Test
    @Order(3)
    void getPreferencesNonExistentUserTest() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("getUserId"))).sendKeys("9999");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("getBtn"))).click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("getErrorMessage")));
        assertTrue(errorMsg.getText().toLowerCase().contains("not found"));
    }

    @Test
    @Order(4)
    void updatePreferencesInvalidDataTest() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateUserId"))).sendKeys("2001");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateTheme"))).sendKeys("");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("updateBtn"))).click();
        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateErrorMessage")));
        assertTrue(errorMsg.getText().toLowerCase().contains("required"));
    }

    @Test
    @Order(5)
    void deletePreferencesNonExistentUserTest() {
        driver.get(BASE_URL);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deleteUserId"))).sendKeys("9999");
        wait.until(ExpectedConditions.elementToBeClickable(By.id("deleteBtn"))).click();
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(alert.getText().toLowerCase().contains("not found"));
        alert.accept();
    }
}

