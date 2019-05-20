package com.sdl.selenium.extjs6.form;

import com.sdl.selenium.web.SearchType;
import com.sdl.selenium.web.WebLocator;
import com.sdl.selenium.web.form.ICombo;
import com.sdl.selenium.web.utils.RetryUtils;
import com.sdl.selenium.web.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.Keys;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@Slf4j
public class TagField extends ComboBox implements ICombo {

    private WebLocator list = new WebLocator(this).setElPath("/ancestor::*[contains(concat(' ', @class, ' '), ' x-tagfield-list ')]");

    public TagField() {
        setClassName("TagField");
        setBaseCls("x-tagfield-input-field");
    }

    public TagField(WebLocator container) {
        this();
        setContainer(container);
    }

    public TagField(WebLocator container, String label, SearchType... searchTypes) {
        this(container);
        if (searchTypes.length == 0) {
            searchTypes = new SearchType[]{SearchType.DEEP_CHILD_NODE_OR_SELF};
        } else {
            List<SearchType> types = new ArrayList<>(Arrays.asList(searchTypes));
            types.add(SearchType.DEEP_CHILD_NODE_OR_SELF);
            searchTypes = types.toArray(new SearchType[0]);
        }
        setLabel(label, searchTypes);
    }

    @Override
    public boolean select(String value) {
        return select(SearchType.EQUALS, value);
    }

    public boolean select(String... values) {
        return select(SearchType.EQUALS, values);
    }

    public boolean select(boolean holdOpen, String... values) {
        return select(SearchType.EQUALS, holdOpen, values);
    }

    public boolean select(SearchType searchType, String... values) {
        boolean selected = doSelect(searchType, 300, true, values);
        assertThat("Could not selected value on : " + this, selected, is(true));
        return selected;
    }

    public boolean select(SearchType searchType, boolean holdOpen, String... values) {
        boolean selected = doSelect(searchType, 300, holdOpen, values);
        assertThat("Could not selected value on : " + this, selected, is(true));
        return selected;
    }

    /**
     * @param searchType         use {@link SearchType}
     * @param optionRenderMillis eg. 300ms
     * @param holdOpen           true | false
     * @param values             values[]
     * @return true if value was selected
     */
    public boolean doSelect(SearchType searchType, long optionRenderMillis, boolean holdOpen, String... values) {
        boolean selected = true;
        String info = toString();
        if (holdOpen) {
            if (expand()) {
                for (String value : values) {
                    WebLocator option = getComboEl(value, optionRenderMillis, searchType);
                    selected = selected && option.doClick();
                    if (selected) {
                        log.info("Set value(" + info + "): " + value);
                    } else {
                        log.debug("(" + info + ") The option '" + value + "' could not be located. " + option.getXPath());
                    }
                }
                collapse(); // to close combo
            }
        } else {
            for (String value : values) {
                expand();
                WebLocator option = getComboEl(value, optionRenderMillis, searchType);
                selected = selected && option.doClick();
                if (selected) {
                    log.info("Set value(" + info + "): " + value);
                } else {
                    log.debug("(" + info + ") The option '" + value + "' could not be located. " + option.getXPath());
                    collapse();
                }
            }
        }
        return selected;
    }

    public boolean remove(String... values) {
        boolean removed = true;
        for (String value : values) {
            WebLocator item = new WebLocator(list).setClasses("x-tagfield-item").setText(value, SearchType.DEEP_CHILD_NODE_OR_SELF);
            WebLocator closeEl = new WebLocator(item).setClasses("x-tagfield-item-close");
            removed = removed && RetryUtils.retry(14, closeEl::click);
        }
        return removed;
    }

    public boolean doRemove(String... values) {
        boolean removed = true;
        for (String value : values) {
            WebLocator item = new WebLocator(list).setClasses("x-tagfield-item").setText(value, SearchType.DEEP_CHILD_NODE_OR_SELF);
            WebLocator closeEl = new WebLocator(item).setClasses("x-tagfield-item-close");
            removed = removed && RetryUtils.retry(15, closeEl::doClick);
        }
        return removed;
    }

    @Override
    public boolean setValue(String value) {
        assertReady(value);
        boolean setValue = executor.setValue(this, value);
        Utils.sleep(50);
        return setValue && sendKeys(Keys.ENTER) != null;
    }

    @Override
    public String getValue() {
        return list.getText();
    }

    public List<String> getAllSelectedValues() {
        String text = list.getText();
        if (text != null) {
            boolean isEmpty = false;
            String[] comboValues = text.split("\\n");
            if (comboValues.length == 1) {
                isEmpty = "".equals(comboValues[0]);
            }
            return isEmpty ? new ArrayList<>() : Arrays.asList(comboValues);
        }
        return new ArrayList<>();
    }

    public boolean expand() {
        return clickIcon("arrow-trigger");
    }

    public boolean collapse() {
        return expand();
    }
}