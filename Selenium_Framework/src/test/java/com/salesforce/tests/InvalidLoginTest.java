package com.salesforce.tests;

import com.salesforce.base.BaseTest;
import com.salesforce.pages.LoginPage;
import org.testng.Assert;
import org.testng.annotations.Test;

public class InvalidLoginTest extends BaseTest {

    @Test
    public void testLoginFailureWithInvalidCredentials() {
        try {
            LoginPage loginPage = new LoginPage(getDriver());

            Assert.assertTrue(loginPage.isUsernameFieldDisplayed(), "Username field is missing from login page.");
            Assert.assertTrue(loginPage.isPasswordFieldDisplayed(), "Password field is missing from login page.");
            Assert.assertTrue(loginPage.isLoginButtonDisplayed(), "Login button is missing from login page.");

            String invalidUsername = "unauthorized_qa_user_" + System.currentTimeMillis() + "@testdomain.com";
            String invalidPassword = "WrongPassword_987#";

            loginPage.doLogin(invalidUsername, invalidPassword);

            Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Error banner was not displayed following invalid credentials submission.");

            String errorText = loginPage.getErrorMessageText();
            Assert.assertTrue(
                    errorText.contains("Please check your username and password") ||
                    errorText.contains("check your username") ||
                    !errorText.isEmpty(),
                    "Displayed error message text did not match expected invalid login notification. Actual text: " + errorText
            );

            Assert.assertTrue(
                    getDriver().getCurrentUrl().contains("login.salesforce.com"),
                    "User was unexpectedly redirected away from login page following invalid login."
            );
        } catch (Exception e) {
            Assert.fail("Invalid login test execution failed: " + e.getMessage(), e);
        }
    }
}
