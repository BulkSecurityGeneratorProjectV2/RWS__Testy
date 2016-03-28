package com.sdl.selenium.web.form;

import com.sdl.selenium.web.WebLocator;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ComboBoxTest {
    private static WebLocator container = new WebLocator("container");

    @DataProvider
    public static Object[][] testConstructorPathDataProvider() {
        return new Object[][]{
                {new ComboBox(),             "//select"},
                {new ComboBox(container),    "//*[contains(concat(' ', @class, ' '), ' container ')]//select"},
                {new ComboBox().withId("ID"), "//select[@id='ID']"},
                {new ComboBox().withType("type"), "//select[@type='type']"},
        };
    }

    @Test(dataProvider = "testConstructorPathDataProvider")
    public void getPathSelectorCorrectlyFromConstructors(ComboBox combo, String expectedXpath) {
        Assert.assertEquals(combo.getXPath(), expectedXpath);
    }
}
