package com.sdl.selenium.web;

import com.sdl.selenium.InputData;
import com.sdl.selenium.TestBase;
import com.sdl.selenium.utils.config.WebDriverConfig;
import com.sdl.selenium.web.utils.PropertiesReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class WebLocator1IntegrationTest extends TestBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebLocator1IntegrationTest.class);

    private WebLocator webLocatorId = new WebLocator().withId("webLocatorId");
    private WebLocator webLocatorCls = new WebLocator().withClasses("webLocatorCls");
    private WebLocator webLocatorNotAttribute = new WebLocator().withClasses("notExist");

    private WebLocator webLocatorWithMoreEnter = new WebLocator().withClasses("more-elements-inside").withText("more enter inside div");
    private WebLocator webLocatorWithMoreEnterMoreElements = new WebLocator().withClasses("more-elements-inside").withText("more enter inside div", SearchType.DEEP_CHILD_NODE);
    private WebLocator webLocatorNoWithMoreEnterMoreElements = new WebLocator().withClasses("more-elements-inside no-enter").withText("more enter inside div", SearchType.DEEP_CHILD_NODE);
    private WebLocator webLocatorNoWithMoreEnter = new WebLocator().withClasses("more-elements-inside no-enter").withText("more enter inside div");
    private WebLocator webLocatorWithMoreText = new WebLocator().withElxPath("//*[contains(@class, 'element7') and concat(text()[1], ./*/text(), text()[2], ./*/text()[contains(.,'care')], text()[3])='Some important text care trebuie']");
    private WebLocator webLocatorComplex = new WebLocator().withClasses("element11").withText("Some more important text that is very important . end", SearchType.HTML_NODE);
    private WebLocator webLocatorLogger = new WebLocator().withId("logger");

    @DataProvider
    public static Object[][] testConstructorPathDataProviderText() {
        String text = "WebLocator text for search type";
        String cls = "searchTextType";
        return new Object[][]{
                {new WebLocator().withClasses(cls).withText(text, SearchType.CONTAINS), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and contains(text(),'" + text + "')]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.EQUALS), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and text()='" + text + "']"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.STARTS_WITH), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and starts-with(text(),'" + text + "')]"},

                {new WebLocator().withClasses(cls).withText(text, SearchType.CONTAINS, SearchType.CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(text()[contains(.,'" + text + "')]) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.EQUALS, SearchType.CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(text()[.='" + text + "']) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.STARTS_WITH, SearchType.CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(text()[starts-with(.,'" + text + "')]) > 0]"},

                {new WebLocator().withClasses(cls).withText(text, SearchType.CONTAINS, SearchType.DEEP_CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(*//text()[contains(.,'" + text + "')]) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.EQUALS, SearchType.DEEP_CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(*//text()[.='" + text + "']) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.STARTS_WITH, SearchType.DEEP_CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(*//text()[starts-with(.,'" + text + "')]) > 0]"},

                {new WebLocator().withClasses(cls).withText(text, SearchType.CONTAINS, SearchType.TRIM), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and contains(normalize-space(text()),'" + text + "')]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.EQUALS, SearchType.TRIM), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and normalize-space(text())='" + text + "']"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.STARTS_WITH, SearchType.TRIM), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and starts-with(normalize-space(text()),'" + text + "')]"},

                {new WebLocator().withClasses(cls).withText(text, SearchType.CONTAINS, SearchType.TRIM, SearchType.CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(text()[contains(normalize-space(.),'" + text + "')]) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.EQUALS, SearchType.TRIM, SearchType.CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(text()[normalize-space(.)='" + text + "']) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.STARTS_WITH, SearchType.TRIM, SearchType.CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(text()[starts-with(normalize-space(.),'" + text + "')]) > 0]"},

                {new WebLocator().withClasses(cls).withText(text, SearchType.CONTAINS, SearchType.TRIM, SearchType.DEEP_CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(*//text()[contains(normalize-space(.),'" + text + "')]) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.EQUALS, SearchType.TRIM, SearchType.DEEP_CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(*//text()[normalize-space(.)='" + text + "']) > 0]"},
                {new WebLocator().withClasses(cls).withText(text, SearchType.STARTS_WITH, SearchType.TRIM, SearchType.DEEP_CHILD_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and count(*//text()[starts-with(normalize-space(.),'" + text + "')]) > 0]"},

                {new WebLocator().withClasses(cls).withText(text, SearchType.HTML_NODE), "//*[contains(concat(' ', @class, ' '), ' searchTextType ') and (normalize-space(concat(./*[1]//text(), ' ', text()[1], ' ', ./*[2]//text(), ' ', text()[2], ' ', ./*[3]//text(), ' ', text()[3], ' ', ./*[4]//text(), ' ', text()[4], ' ', ./*[5]//text(), ' ', text()[5]))='WebLocator text for search type' or normalize-space(concat(text()[1], ' ', ./*[1]//text(), ' ', text()[2], ' ', ./*[2]//text(), ' ', text()[3], ' ', ./*[3]//text(), ' ', text()[4], ' ', ./*[4]//text(), ' ', text()[5], ' ', ./*[5]//text()))='WebLocator text for search type')]"},
        };
    }

    @BeforeClass
    public void startTests() {
        driver.get(InputData.WEB_LOCATOR_URL);
    }

    @Test
    public void identifyById() {
        assertEquals(webLocatorId.getAttributeId(), "webLocatorId");
        assertEquals(webLocatorId.getAttributeClass(), "");
    }

    @Test
    public void identifyByClass() {
        assertEquals(webLocatorCls.getAttributeClass(), "webLocatorCls");
        assertEquals(webLocatorCls.getAttributeId(), "");
    }

    @Test
    public void attributeForNotFoundElement() {
        assertEquals(webLocatorNotAttribute.getAttributeClass(), null);
    }

    //@Test
    public void webDriverConfig() {
        WebLocator l = new WebLocator().withClasses("x-tool-maximize");
        LOGGER.debug(l.getXPath());
        LOGGER.debug("//*[contains(@class,'x-tool-maximize')]");

        String browserConfig = InputData.BROWSER_CONFIG;
        PropertiesReader properties = new PropertiesReader(browserConfig);
        String browserName = properties.getProperty("browser");

        Browser browser = Browser.valueOf(browserName.toUpperCase());
        if (browser == Browser.FIREFOX) {
            assertTrue(WebDriverConfig.isFireFox());
        } else if (browser == Browser.CHROME) {
            assertTrue(WebDriverConfig.isChrome());
        } else if (browser == Browser.IEXPLORE) {
            assertTrue(WebDriverConfig.isIE());
        }
    }

    @Test
    public void getTextFromDiv() {
        assertEquals(webLocatorWithMoreEnter.getAttributeClass(), "more-elements-inside no-enter element4");
        assertEquals(webLocatorWithMoreEnterMoreElements.getAttributeClass(), "more-elements-inside with-enter element1");
        assertEquals(webLocatorNoWithMoreEnterMoreElements.getAttributeClass(), "more-elements-inside no-enter element3");
        assertEquals(webLocatorNoWithMoreEnter.getAttributeClass(), "more-elements-inside no-enter element4");
        assertEquals(webLocatorWithMoreText.getAttributeClass(), "more-elements-inside with-enter element7");
        assertEquals(webLocatorComplex.getAttributeClass(), "more-elements-inside with-enter element11");
    }

    @Test(dataProvider = "testConstructorPathDataProviderText")
    public void shouldFindAllCombinationsForTextSearchTypeTest(WebLocator el, String expectedPath) {

        LOGGER.debug(el.getXPath());
        assertTrue(el.click());

        boolean useChildNodesSearch = el.getPathBuilder().getSearchTextType().contains(SearchType.DEEP_CHILD_NODE);

        String expected = "WebLocator text for search type-searchTextType" + (useChildNodesSearch ? " deep" : "");
        assertEquals(webLocatorLogger.getText(), expected);

        webLocatorWithMoreEnter.click();
    }
}
