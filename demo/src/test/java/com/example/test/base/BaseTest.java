package com.example.test.base;

import java.time.Duration;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import io.github.bonigarcia.wdm.WebDriverManager;

/**
 * Classe base de testes para Selenium WebDriver.
 * Fornece configuração e encerramento do WebDriver para todas as classes de teste.
 */
public class BaseTest {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final String BASE_URL = "http://localhost:9090";
    protected static final int TIMEOUT_SECONDS = 10;

    /**
     * Configura o WebDriver antes de cada teste
     */
    @BeforeMethod
    public void setup() {
        WebDriverManager.chromedriver().browserVersion("143").setup();
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUT_SECONDS));
        wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
        driver.get(BASE_URL + "/produtos/listar");
    }

    /**
     * Encerra o WebDriver após cada teste
     */
    @AfterMethod
    public void teardown() {
        if (driver != null) {
            driver.quit();
        }
    }
}
