package com.proyecto.AccesoUsuarios;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CompueduUITest {

    private WebDriver driver;
    private WebDriverWait wait;
    // URL de tu servicio Java en Render
    private final String BASE_URL = "https://compuedu.onrender.com";

    @BeforeEach
    public void setUp() {
        // Inicializa el navegador Chrome
        driver = new ChromeDriver();
        driver.manage().window().maximize();
        // Espera explícita de hasta 10 segundos para soportar la carga en Render
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @Test
    public void testFlujoCompletoInstitucion() {
        // --- PASO 1: LOGIN AUTOMATIZADO ---
        driver.get(BASE_URL + "/login");

        WebElement usernameInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("username")));
        WebElement passwordInput = driver.findElement(By.name("password"));
        WebElement loginButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameInput.sendKeys("politecnico");
        passwordInput.sendKeys("06092006");
        loginButton.click();

        // FUERZA LA RUTA DEL MÓDULO: Tras el login, obligamos a Selenium a navegar a la
        // ruta exacta del taller
        driver.get(BASE_URL + "/institucion/dashboard");

        // Validar que estamos exitosamente dentro del panel de la institución
        wait.until(ExpectedConditions.urlContains("/institucion/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/institucion/dashboard"));

        // --- PASO 2: CAMBIO DE ESTADO DE POSTULANTE ---
        // Localiza el selector de estado de la primera fila de la tabla
        WebElement selectEstado = wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("estado")));
        Select dropdown = new Select(selectEstado);
        dropdown.selectByValue("ACEPTADO");

        // CORREGIDO: Ajustado el action del formulario removiendo el prefijo
        // /institucion
        WebElement submitEstadoBtn = driver
                .findElement(By.xpath("//form[contains(@action, '/inscripciones/cambiar-estado')]//button"));
        submitEstadoBtn.click();

        // Validar mensaje de éxito en pantalla
        WebElement mensajeExito = wait
                .until(ExpectedConditions.visibilityOfElementLocated(By.className("alert-success")));
        assertTrue(mensajeExito.getText().contains("correctamente"));

        // --- PASO 3: REDIRECCIÓN AL MÓDULO PHP Y RETORNO ---
        // Hace clic en el botón de estadísticas
        WebElement btnEstadisticas = driver.findElement(By.linkText("Ver Estadísticas"));
        btnEstadisticas.click();

        // Verificar que saltó al contenedor de PHP en Render
        wait.until(ExpectedConditions.urlContains("/institucion/estadisticas.php"));
        assertTrue(driver.getPageSource().contains("Módulo Integrado de Analítica Institucional"));

        // Probar el botón de volver basado en el historial del navegador
        WebElement btnVolver = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(), 'Volver al Panel')]")));
        btnVolver.click();

        // CORREGIDO: Validar que regresó exitosamente a la ruta real de Spring Boot
        wait.until(ExpectedConditions.urlContains("/dashboard"));
    }

    @AfterEach
    public void tearDown() {
        if (driver != null) {
            driver.quit(); // Cierra todas las ventanas del navegador al finalizar
        }
    }
}