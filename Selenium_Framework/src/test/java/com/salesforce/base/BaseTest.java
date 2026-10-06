package com.salesforce.base;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public class BaseTest {

    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitThreadLocal = new ThreadLocal<>();
    private static final Properties properties = new Properties();

    @BeforeTest
    public void setupSuite() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            } else {
                throw new RuntimeException("Configuration file config.properties not found in classpath.");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration properties", e);
        }
    }

    @BeforeMethod
    public void setUp() {
        String browser = System.getProperty("browser", properties.getProperty("browser", "chrome"));
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", properties.getProperty("headless", "false")));
        long explicitWaitSeconds = Long.parseLong(properties.getProperty("explicit.wait.seconds", "15"));
        long pageLoadTimeoutSeconds = Long.parseLong(properties.getProperty("page.load.timeout.seconds", "30"));

        WebDriver driver;
        if (browser.equalsIgnoreCase("firefox")) {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = new FirefoxOptions();
            if (headless) {
                options.addArguments("-headless");
            }
            driver = new FirefoxDriver(options);
        } else {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            if (headless) {
                options.addArguments("--headless=new");
            }
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--remote-allow-origins=*");
            driver = new ChromeDriver(options);
        }

        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(pageLoadTimeoutSeconds));
        driver.manage().window().maximize();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWaitSeconds));

        driverThreadLocal.set(driver);
        waitThreadLocal.set(wait);

        String appUrl = properties.getProperty("app.url", "https://login.salesforce.com/?locale=in");
        driver.get(appUrl);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        WebDriver driver = driverThreadLocal.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                driverThreadLocal.remove();
                waitThreadLocal.remove();
            }
        }
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver has not been initialized for the current thread.");
        }
        return driver;
    }

    public static WebDriverWait getWait() {
        WebDriverWait wait = waitThreadLocal.get();
        if (wait == null) {
            throw new IllegalStateException("WebDriverWait has not been initialized for the current thread.");
        }
        return wait;
    }

    public static Properties getProperties() {
        return properties;
    }
}
