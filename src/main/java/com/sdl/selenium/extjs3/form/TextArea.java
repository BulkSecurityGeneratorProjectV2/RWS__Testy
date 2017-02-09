package com.sdl.selenium.extjs3.form;

import com.sdl.selenium.web.WebLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextArea extends TextField {
    private static final Logger LOGGER = LoggerFactory.getLogger(TextArea.class);

    public TextArea() {
        setClassName("TextArea");
        setTag("textarea");
    }

    public TextArea(WebLocator container) {
        this();
        setContainer(container);
    }

    public TextArea(WebLocator container, String label) {
        this(container);
        setLabel(label);
    }

    public TextArea(String name, WebLocator container) {
        this(container);
        setName(name);
    }
}