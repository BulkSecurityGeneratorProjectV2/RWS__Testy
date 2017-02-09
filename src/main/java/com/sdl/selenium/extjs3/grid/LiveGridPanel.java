package com.sdl.selenium.extjs3.grid;

import com.sdl.selenium.extjs3.button.Button;
import com.sdl.selenium.web.WebLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LiveGridPanel extends GridPanel {
    private static final Logger LOGGER = LoggerFactory.getLogger(LiveGridPanel.class);

    public LiveGridPanel() {
        setClassName("LiveGridPanel");
        setBaseCls("ext-ux-livegrid");
        setHeaderBaseCls("x-panel");
    }

    public LiveGridPanel(String cls) {
        this();
        setClasses(cls);
    }

    public LiveGridPanel(WebLocator container) {
        this();
        setContainer(container);
    }

    public LiveGridPanel(String cls, String searchColumnId) {
        this(cls);
        setSearchColumnId(searchColumnId);
    }

    public LiveGridPanel(WebLocator container, String searchColumnId) {
        this(container);
        setSearchColumnId(searchColumnId);
    }

    public LiveGridPanel(WebLocator container, String cls, String searchColumnId) {
        this(container);
        setClasses(cls);
        setSearchColumnId(searchColumnId);
    }

    /**
     * Scroll Top one visible page in LiveGrid
     *
     * @param id element
     * @return true if scrolled
     */
    protected boolean scrollTop(String id) {
        String script = "return (function(c){var a=c.view,b=a.liveScroller,d=a.liveScrollerInsets,n=d.length,h=n*d[0].style.height.replace('px','');if(b.dom.style.display=='none'){return false}if(b.dom.scrollTop!=0){b.dom.scrollTop=0;return true}return false})(window.Ext.getCmp('" + id + "'))";
        return executeScrollScript("scrollTop", script);
    }

    public boolean scrollBottom() {
        String id = getAttrId();
        while (scrollPageDown(id)){}
        return true;
    }

    /**
     * Scroll Up one visible page in LiveGrid
     *
     * @return true if scrolled
     */
    public boolean scrollPageUp() {
        LOGGER.warn("TODO not yet implemented.");
        //TODO not yet implemented.
        /*String id = getAttributeId();
        String script = "return (function(c){var a=c.view,b=a.scroller;if(b.dom.scrollTop>0){b.dom.scrollTop-=b.getHeight()-10;return true}return false})(window.Ext.getCmp('" + id + "'))";
        if (hasWebDriver()) {
            return executeScrollScript("scrollPageUp", "return " + script);
        } else {
            return executeScrollScript("scrollPageUp", script);
        }*/
        return false;
    }

    /**
     * Scroll Down one visible page in LiveGrid
     *
     * @param id element
     * @return true if scrolled
     */
    protected boolean scrollPageDown(String id) {
        String script = "return (function(c){var a=c.view,b=a.liveScroller,d=a.liveScrollerInsets,n=d.length,h=n*d[0].style.height.replace('px','');if(b.dom.style.display=='none'){return false}if(b.dom.scrollTop<(h-b.getHeight()-1)){b.dom.scrollTop+=b.getHeight()-10;return true}return false})(window.Ext.getCmp('" + id + "'))";
        return executeScrollScript("scrollPageDown", script);
    }

    @Override
    public boolean waitToLoad(int seconds) {
        //TODO find a good solution for x-mask-loading
        return true;
    }

    @Override
    public WebLocator getSelectAllChecker(String columnId) {
        waitToRender();
        return new WebLocator(this).setElPath("//*[contains(@class, 'x-grid3-hd-" + columnId + "')]//div");
    }

    public boolean refresh() {
        Button refreshButton = new Button(this).setIconCls("x-tbar-loading").setInfoMessage("Loading...");
        return ready(true) && refreshButton.clickAt() && ready(true);
    }
}
