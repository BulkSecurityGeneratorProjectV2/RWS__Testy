package com.sdl.selenium.extjs3;

import com.sdl.selenium.TestBase;
import com.sdl.selenium.bootstrap.form.Form;
import com.sdl.selenium.conditions.Condition;
import com.sdl.selenium.conditions.ConditionManager;
import com.sdl.selenium.conditions.ElementRemovedSuccessCondition;
import com.sdl.selenium.conditions.RenderSuccessCondition;
import com.sdl.selenium.extjs3.button.Button;
import com.sdl.selenium.extjs3.conditions.MessageBoxSuccessCondition;
import com.sdl.selenium.extjs3.grid.GridPanel;
import com.sdl.selenium.extjs3.tab.TabPanel;
import com.sdl.selenium.extjs3.window.MessageBox;
import com.sdl.selenium.extjs3.window.Window;
import com.sdl.selenium.web.SearchType;
import com.sdl.selenium.web.WebLocator;
import com.sdl.selenium.web.form.TextField;
import com.sdl.selenium.web.link.WebLink;
import com.sdl.selenium.web.table.Cell;
import com.sdl.selenium.web.table.Row;
import com.sdl.selenium.web.table.Table;
import com.sdl.selenium.web.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

public class DeployTesty extends TestBase {
    private static final Logger logger = LoggerFactory.getLogger(DeployTesty.class);

    // Rulati acest test dupa ce ati oprit orice test!!!!

    private static final String DOMAIN_USER = "domain.user";
    private static final String DOMAIN_PASS = "***";

    private static final String JENKINS_JOB_URL = "http://cluj-jenkins02:8080/job/testy/";

    private static final String NEXUS_REPOSITORY_URL = "http://cljeng-nexus02:8081/nexus/#view-repositories;oss-sonatype-snapshots";

    private WebLocator loginContainer = new WebLocator().withClasses("login");
    private WebLink loginEl = new WebLink(loginContainer, "log in").withSearchTextType(SearchType.HTML_NODE);
    private WebLink logOutEl = new WebLink(loginContainer, "log out").withSearchTextType(SearchType.HTML_NODE);
    private Form loginForm = new Form().withName("login");
    private TextField login = new TextField(loginForm).withName("j_username");
    private TextField pass = new TextField(loginForm).withName("j_password");
    private com.sdl.selenium.web.button.Button logInButton = new com.sdl.selenium.web.button.Button(loginForm).withId("yui-gen1-button");
    private WebLocator table = new WebLocator().withId("tasks");
    private WebLink buildNow = new WebLink(table, "Build Now").withClasses("task-link");
    private Table buildHistory = new Table().withClasses("pane", "stripped");
    private Row buildNowEl = new Row(buildHistory).withClasses("build-row", "transitive");

    private WebLocator logInNexus = new WebLocator().withId("head-link-r");
    private Window nexusLogInWindow = new Window("Nexus Repository Manager Log In");
    private com.sdl.selenium.extjs3.form.TextField userName = new com.sdl.selenium.extjs3.form.TextField(nexusLogInWindow, "Username:");
    private com.sdl.selenium.extjs3.form.TextField password = new com.sdl.selenium.extjs3.form.TextField(nexusLogInWindow, "Password:");
    private Button logIn = new Button(nexusLogInWindow, "Log In");
    private WebLocator viewRepositories = new WebLocator().withId("view-repositories");
    private GridPanel repositoryGridPanel = new GridPanel(viewRepositories);
    private TabPanel browseStorage = new TabPanel(viewRepositories, "Browse Storage");
    private Table table1 = new Table(browseStorage).withCls("x-toolbar-ct");
    private WebLocator testyDir = new WebLocator().withElxPath("//a[@class='x-tree-node-anchor' and count(.//span[text()='Testy']) > 0]");
    private Cell cell = table1.getCell(4, new Cell(3, "Path Lookup:", SearchType.EQUALS));
    private com.sdl.selenium.extjs3.form.TextField searchField = new com.sdl.selenium.extjs3.form.TextField(cell);
    private WebLocator menuList = new WebLocator().withClasses("x-menu").withExcludeClasses("x-hide-offsets");
    private WebLocator deleteEl = new WebLocator(menuList).withClasses("x-menu-item-text").withText("Delete");

    @BeforeClass
    public void startTests() {
        driver.get(JENKINS_JOB_URL);
    }

    @Test
    public void loginJenkins() {
        loginEl.click();
        loginForm.ready(10);
        WebLocator.getExecutor().executeScript("arguments[0].scrollIntoView(true);", loginForm.currentElement);
        login.setValue(DOMAIN_USER);
        pass.setValue(DOMAIN_PASS);
        logInButton.click();
        logOutEl.ready(10);
    }

    @Test(dependsOnMethods = "loginJenkins")
    public void deployOnJenkins() {
        buildNow.click();
        String testyDir = System.getProperty("user.home") + "\\.m2\\repository\\com\\sdl\\lt\\Testy";
        cleanDir(testyDir);
        waitRenderEl(buildNowEl, 5000);
        waitRemovedEl(buildNowEl, 720000);
    }

    @Test(dependsOnMethods = "deployOnJenkins")
    public void loginAsAdminSDLNexus() {
        driver.get(NEXUS_REPOSITORY_URL);
        logInNexus.ready();
        Utils.sleep(1000);
        logInNexus.click();
        Utils.sleep(1000);
        userName.ready();
        userName.setValue(DOMAIN_USER);
        password.setValue(DOMAIN_PASS);
        logIn.click();
    }

    @Test(dependsOnMethods = "loginAsAdminSDLNexus")
    public void removeFormSDLNexus() {
        repositoryGridPanel.setTimeout(10);
        repositoryGridPanel.rowSelect("OSS Sonatype Snapshots");
        browseStorage.setActive();
        searchField.setValue("com/sdl/lt/Testy");
        testyDir.ready(10);
        Utils.sleep(1000);
        Actions action = new Actions(driver);
        action.contextClick(testyDir.currentElement).perform();
        deleteEl.click();
        confirmYesMSG("Delete the selected \"/com/sdl/lt/Testy/\" folder?");
        Utils.sleep(1000);
    }

    private void cleanDir(String path) {
        try {
            FileUtils.cleanDirectory(new File(path));
        } catch (IllegalArgumentException | IOException e) {
            logger.debug("not found " + path);
        }
    }

    private boolean waitRemovedEl(WebLocator el, long time) {
        return new ConditionManager(time).add(new ElementRemovedSuccessCondition(el)).execute().isSuccess();
    }

    private boolean waitRenderEl(WebLocator el, long time) {
        return new ConditionManager(time).add(new RenderSuccessCondition(el)).execute().isSuccess();
    }

    private boolean confirmYesMSG(String msg) {
        boolean success = false;
        Condition condition = new ConditionManager(16000).add(new MessageBoxSuccessCondition(msg)).execute();
        if (condition.isSuccess()) {
            MessageBox.pressYes();
            success = true;
        }
        return success;
    }
}
