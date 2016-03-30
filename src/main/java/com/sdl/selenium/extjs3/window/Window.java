package com.sdl.selenium.extjs3.window;

import com.sdl.selenium.extjs3.panel.Panel;
import com.sdl.selenium.utils.config.WebDriverConfig;
import com.sdl.selenium.web.WebLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Window extends Panel {
    private static final Logger LOGGER = LoggerFactory.getLogger(Window.class);

    private boolean modal;

    public boolean isModal() {
        return modal;
    }

    public void setModal(boolean modal) {
        this.modal = modal;
        String selector = null;
        if (modal) {
            // test for IE be cause of :
            // http://jira.openqa.org/browse/SEL-545
            // and http://code.google.com/p/selenium/issues/detail?id=1716
            if (WebDriverConfig.isIE()) {
                selector = "preceding-sibling::*[contains(@class, 'ext-el-mask')]";
            } else {
                selector = "preceding-sibling::*[contains(@class, 'ext-el-mask') and contains(@style, 'display: block')]";
            }
        }
        withElxPathSuffix("modal-window", selector);
    }

    /**
     * Windows have default style="visibility: visible;"
     */
    public Window() {
        withClassName("Window");
        withBaseCls("x-window");
        setHeaderBaseCls(getPathBuilder().getBaseCls());
        withTemplate("title", "count(*[contains(@class,'" + getHeaderBaseCls() + "-header') or contains(@class, '-tl')]//*[text()='%s']) > 0");
        withElxPathSuffix("exclude-hide-cls", null);
        // test for IE be cause of :
        // http://jira.openqa.org/browse/SEL-545
        // and http://code.google.com/p/selenium/issues/detail?id=1716
        if (!WebDriverConfig.isIE()) {
            withStyle("visibility: visible;");
        }
    }

    public Window(Boolean modal) {
        this();
        setModal(modal);
    }

    public Window(String title) {
        this();
        withTitle(title);
    }

    public Window(String title, Boolean modal) {
        this(modal);
        withTitle(title);
    }

    public String getTitleWindow() {
        ready();
        WebLocator locator = new WebLocator(this).withClasses("x-window-header-text");
        return locator.getText();
    }
}