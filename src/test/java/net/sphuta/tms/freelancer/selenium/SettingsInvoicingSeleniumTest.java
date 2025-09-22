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
}
