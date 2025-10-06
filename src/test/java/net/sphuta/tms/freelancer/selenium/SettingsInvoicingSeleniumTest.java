package net.sphuta.tms.freelancer.selenium;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SettingsInvoicingSeleniumTest {

    private static WebDriver driver;
    private static WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/invoicing-form";

    @BeforeAll
    public static void setUp() {
        // Automatically manage ChromeDriver
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();

        // Explicit wait
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));

        System.out.println("Browser launched and ready.");
    }

    @AfterAll
    public static void tearDown() {
        if (driver != null) {
            driver.quit();
            System.out.println("Browser closed.");
        }
    }

    @Test
    @Order(1)
    void createSettingsTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId"))).sendKeys("1001");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currency"))).sendKeys("USD");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("taxId"))).sendKeys("TAX-123");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("defaultTaxRate"))).sendKeys("0.08");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("invoiceNumberFormat"))).sendKeys("INV-${yyyy}${seq:5}");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("paymentTermsDays"))).sendKeys("14");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lateFeePercent"))).sendKeys("0.05");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("templateId"))).sendKeys("tmpl_default");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logoFileId"))).sendKeys("logo-123");

        wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn"))).click();
        System.out.println("Create button clicked.");

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        assertTrue(msg.getText().contains("Settings created successfully"));
        System.out.println("Create test passed.");
    }

    @Test
    @Order(2)
    void getSettingsTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("getUserId"))).sendKeys("1001");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//section[@id='getSection']//button"))).click();
        System.out.println("Fetch button clicked.");

        WebElement userIdField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userIdDisplay")));
        WebElement currencyField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currencyDisplay")));

        assertTrue(userIdField.getText().contains("1001"));
        assertTrue(currencyField.getText().contains("USD"));
        System.out.println("Get test passed.");
    }

    @Test
    @Order(3)
    void updateSettingsTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateUserId"))).sendKeys("123e4567-e89b-12d3-a456-426614174000");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateCurrency"))).sendKeys("EUR");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateTaxId"))).sendKeys("TAX-456");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateDefaultTaxRate"))).sendKeys("0.10");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateInvoiceNumberFormat"))).sendKeys("INV-${yyyy}${seq:6}");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updatePaymentTermsDays"))).sendKeys("30");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateLateFeePercent"))).sendKeys("0.08");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateTemplateId"))).sendKeys("tmpl_updated");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateLogoFileId"))).sendKeys("logo-456");

        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//section[@id='updateSection']//button"))).click();
        System.out.println("Update button clicked.");

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(alert.getText().contains("Settings updated successfully"));
        alert.accept();
        System.out.println("Update test passed.");
    }

    @Test
    @Order(4)
    void deleteSettingsTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deleteUserId"))).sendKeys("1001");
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//section[@id='deleteSection']//button"))).click();
        System.out.println("Delete button clicked.");

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(alert.getText().contains("Settings deleted successfully"));
        alert.accept();
        System.out.println("Delete test passed.");
    }

    @Test
    @Order(5)
    void createSettingsWithMissingFieldsTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        // Only fill some fields
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId"))).sendKeys(""); // Missing userId
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currency"))).sendKeys(""); // Missing currency
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("taxId"))).sendKeys("TAX-NEG");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("defaultTaxRate"))).sendKeys("-0.01"); // Invalid negative tax rate
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn"))).click();
        System.out.println("Create button clicked with missing/invalid fields.");

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("errorMessage")));
        assertTrue(errorMsg.getText().toLowerCase().contains("required") || errorMsg.getText().toLowerCase().contains("invalid"));
        System.out.println("Negative create test (missing/invalid fields) passed.");
    }

    @Test
    @Order(6)
    void getSettingsNonExistentUserTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("getUserId"))).sendKeys("9999"); // Assume 9999 does not exist
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//section[@id='getSection']//button"))).click();
        System.out.println("Fetch button clicked for non-existent user.");

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("getErrorMessage")));
        assertTrue(errorMsg.getText().toLowerCase().contains("not found") || errorMsg.getText().toLowerCase().contains("no settings"));
        System.out.println("Negative get test (non-existent user) passed.");
    }

    @Test
    @Order(7)
    void updateSettingsInvalidDataTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateUserId"))).sendKeys("1001");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateCurrency"))).sendKeys("INVALID_CURRENCY");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateTaxId"))).sendKeys(""); // Missing taxId
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateDefaultTaxRate"))).sendKeys("abc"); // Invalid format
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//section[@id='updateSection']//button"))).click();
        System.out.println("Update button clicked with invalid data.");

        WebElement errorMsg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("updateErrorMessage")));
        assertTrue(errorMsg.getText().toLowerCase().contains("invalid") || errorMsg.getText().toLowerCase().contains("required"));
        System.out.println("Negative update test (invalid data) passed.");
    }

    @Test
    @Order(8)
    void deleteSettingsNonExistentUserTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("deleteUserId"))).sendKeys("9999"); // Assume 9999 does not exist
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//section[@id='deleteSection']//button"))).click();
        System.out.println("Delete button clicked for non-existent user.");

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertTrue(alert.getText().toLowerCase().contains("not found") || alert.getText().toLowerCase().contains("no settings"));
        alert.accept();
        System.out.println("Negative delete test (non-existent user) passed.");
    }

    @Test
    @Order(9)
    void createSettingsWithEdgeCaseDataTest() {
        driver.get(BASE_URL);
        System.out.println("Navigated to " + BASE_URL);

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("userId"))).sendKeys("0"); // Edge userId
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("currency"))).sendKeys("JPY");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("taxId"))).sendKeys("TAX-EDGE");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("defaultTaxRate"))).sendKeys("0"); // Zero tax rate
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("invoiceNumberFormat"))).sendKeys(""); // Empty format
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("paymentTermsDays"))).sendKeys("0"); // Zero days
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("lateFeePercent"))).sendKeys("0"); // Zero late fee
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("templateId"))).sendKeys(""); // Empty template
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("logoFileId"))).sendKeys(""); // Empty logo
        wait.until(ExpectedConditions.elementToBeClickable(By.id("submitBtn"))).click();
        System.out.println("Create button clicked with edge-case data.");

        WebElement msg = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("successMessage")));
        assertTrue(msg.getText().contains("Settings created successfully") || msg.getText().toLowerCase().contains("created"));
        System.out.println("Edge-case create test passed.");
    }
}
