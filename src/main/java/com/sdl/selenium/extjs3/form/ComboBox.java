package com.sdl.selenium.extjs3.form;

import com.sdl.selenium.WebLocatorUtils;
import com.sdl.selenium.utils.config.WebDriverConfig;
import com.sdl.selenium.web.WebLocator;
import com.sdl.selenium.web.form.ICombo;
import com.sdl.selenium.web.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;

public class ComboBox extends TextField implements ICombo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComboBox.class);

    private static String listClass = "x-combo-list";

    //TODO change the way comboBox is identified, without using cls
    // (create baseCls and if there is no cls, label then take first combo by baseCls)
    public ComboBox() {
        withClassName("ComboBox");
    }

    public ComboBox(WebLocator container) {
        this();
        withContainer(container);
    }

    public ComboBox(WebLocator container, String label) {
        this(container);
        withLabel(label);
    }

    public ComboBox(String name, WebLocator container) {
        this(container);
        withName(name);
    }

    /**
     * @param value value
     * @return true if value was selected
     */
    @Override
    public boolean setValue(String value) {
        return select(value);
    }

    public boolean select(String value, boolean startWith, long optionRenderMillis) {
        boolean selected;
        String componentId;
        String info = toString();

        String valueTest = startWith ? ("starts-with(text(),'" + value + "')") : ("text()='" + value + "'");
        WebLocator comboListElement = new WebLocator(listClass).withStyle("visibility: visible;").withInfoMessage(this + " -> " + listClass);
        WebLocator option = new WebLocator(comboListElement).withElxPath("//*[" + valueTest + "]").withRenderMillis(optionRenderMillis).withInfoMessage(value);

        if (clickIcon("arrow")) {
            try {
                // TODO temporary try this solution for IE because is too slow
//                if (isIE()) {
//                    componentId = getAttributeId();
//                    selected = setValueWithJs(componentId, value);
//                    return selected;
//                }
                if (WebDriverConfig.isIE()) {
                    comboListElement.withId(getListId());
                    option.withContainer(comboListElement);
                }
                selected = option.click();
            } catch (Exception e) {
//                LOGGER.error(e);
                ready();
                componentId = getAttributeId();
                selected = setValueWithJs(componentId, value);
            }

            if (selected) {
                LOGGER.info("Set value(" + info + "): " + value);
                Utils.sleep(200);
                return true;
            } else {
                clickIcon("arrow"); // to close combo
            }
            LOGGER.debug("(" + info + ") The option '" + value + "' could not be located. " + option.getXPath());
        } else {
            LOGGER.debug("(" + info + ") The combo or arrow could not be located.");
        }
        return false;
    }

    public boolean select(String value, boolean startWith) {
        return select(value, startWith, 300);
    }

    private String getListId() {
        String componentId;
        ready();
        componentId = getAttributeId();
        String getListIdScript = "return Ext.getCmp('" + componentId + "').list.id;";
        LOGGER.debug("script:" + getListIdScript);
        String listId = (String) WebLocatorUtils.doExecuteScript(getListIdScript);
        LOGGER.debug("listId:" + listId);
        return listId;
    }

    /**
     * this method is used in case normal flow for selection fails
     *
     * @param componentId ComboBox id so we can use directly js to force selection of that value
     * @param value       value
     * @return true or false
     */
    private boolean setValueWithJs(final String componentId, final String value) {
        boolean selected;
        String script = "return (function(){var c  = Ext.getCmp('" + componentId + "'); var record = c.findRecord(c.displayField, '" + value + "');" +
                "if(record){c.onSelect(record, c.store.indexOf(record)); return true;} return false;})()";
        LOGGER.warn("force ComboBox Value with js: " + script);
        selected = (Boolean) WebLocatorUtils.doExecuteScript(script);
        LOGGER.warn("force ComboBox select result: " + selected);
        return selected;
    }

    @Override
    public boolean select(String value) {
        return select(value, false);
    }

    public boolean assertSelect(String value) {
        boolean selected = select(value);
        assertThat("Could not selected value on : " + this, selected);
        return selected;
    }
}