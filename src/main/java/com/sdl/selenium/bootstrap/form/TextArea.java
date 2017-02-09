package com.sdl.selenium.bootstrap.form;

import com.sdl.selenium.web.WebLocator;

public class TextArea extends com.sdl.selenium.web.form.TextArea {

    public TextArea() {
    }

    public TextArea(WebLocator container) {
        this();
        setContainer(container);
    }

    public TextArea(WebLocator container, String label) {
        this(container);
        setLabel(label);
    }
}