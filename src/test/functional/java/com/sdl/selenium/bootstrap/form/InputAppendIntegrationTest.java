package com.sdl.selenium.bootstrap.form;

import com.sdl.selenium.InputData;
import com.sdl.selenium.TestBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

public class InputAppendIntegrationTest extends TestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(InputAppendIntegrationTest.class);

    Form form = new Form(null, "Form Title");
    InputAppend inputAppend = new InputAppend(form, "LPID for Merge:");

    @BeforeClass
    public void startTests() {
        driver.get(InputData.BOOTSTRAP_URL);
    }

    @Test
    public void setValueInputAppend() {
        assertTrue(inputAppend.setValue("1234"));
        assertTrue("1234".equals(inputAppend.getValue()));
    }

    @Test
    public void clickInputAppend() {
        assertTrue(inputAppend.append());
    }
}
