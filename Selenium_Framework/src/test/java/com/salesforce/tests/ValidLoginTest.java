package com.salesforce.tests;

import com.salesforce.base.BaseTest;
import com.salesforce.pages.LoginPage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ValidLoginTest extends BaseTest {

    @Test
    public void testSuccessfulLoginWithValidCredentials() {
        try {
            LoginPage loginPage = new LoginPage(getDriver());

            Assert.assertTrue(loginPage.isUsernameFieldDisplayed(), "Username input field is not displayed on login page.");
            Assert.assertTrue(loginPage.isPasswordFieldDisplayed(), "Password input field is not displayed on login page.");
            Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button is not displayed on login page.");

            String username = System.getenv("SF_VALID_USERNAME");
            if (username == null || username.trim().isEmpty()) {
                username = System.getProperty("sf.username", "testuser@example.com");
            }

            String password = System.getenv("SF_VALID_PASSWORD");
            if (password == null || password.trim().isEmpty()) {
                password = System.getProperty("sf.password", "ValidPass123!");
            }

            loginPage.doLogin(username, password);

            boolean sessionNavigated = false;
            try {
                sessionNavigated = getWait().until(ExpectedConditions.or(
                        ExpectedConditions.urlContains("lightning.force.com"),
                        ExpectedConditions.urlContains("salesforce.com/setup"),
                        ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@id='userNav' or contains(@class,'profileTrigger') or @id='oneHeader']"))
                ));
            } catch (TimeoutException e) {
                sessionNavigated = !getDriver().getCurrentUrl().contains("login.salesforce.com");
            }

            Assert.assertTrue(sessionNavigated, "User was not navigated to authenticated destination after valid login.");
        } catch (Exception e) {
            Assert.fail("Valid login test encountered an unexpected failure: " + e.getMessage(), e);
        }
    }
}
