package com.sdl.bootstrap.button;

import com.sdl.selenium.web.WebLocator;
import org.apache.log4j.Logger;
import org.openqa.selenium.interactions.Actions;

public class DownloadFile extends WebLocator implements Download {
    private static final Logger logger = Logger.getLogger(DownloadFile.class);

    public DownloadFile() {
        setClassName("DownloadFile");
        setBaseCls("btn");
        setTag("button");
    }

    /**
     * @param container parent
     */
    public DownloadFile(WebLocator container) {
        this();
        setContainer(container);
    }

    public DownloadFile(WebLocator container, String label) {
        this(container);
        setLabel(label);
    }

    /**
     * Download file with AutoIT. Work only on FireFox.
     * Use only this: button.download("C:\\downloadAndCancel.exe", "TestSet.tmx");
     * return true if the downloaded file is the same one that is meant to be downloaded, otherwise returns false.
     * @param filePath e.g. "C:\\downloadAndCancel.exe", "TestSet.tmx"
     */
    @Override
    public boolean download(String ...filePath) {
        openBrowse();
        return RunExe.getInstance().download(filePath);
    }

    public boolean download(long timeout, String ...filePath) {
        openBrowse();
        return RunExe.getInstance().download(timeout, filePath);
    }

    /**
     * Download file with AutoIT. Work only on FireFox.
     * Use only this: button.download("C:\\downloadAndSave.exe");
     * return true if the downloaded file is success, otherwise returns false.
     * @param filePath e.g. "C:\\downloadAndSave.exe"
     */
    public boolean download(String filePath) {
        openBrowse();
        return RunExe.getInstance().download(filePath);
    }

    public boolean download(String filePath, long timeout) {
        openBrowse();
        boolean success = RunExe.getInstance().download(filePath, timeout);
        logger.debug("download=" + success);
        return success;
    }

    private void openBrowse(){
        driver.switchTo().window(driver.getWindowHandle());
        focus();
        Actions builder = new Actions(driver);
        builder.moveToElement(currentElement).build().perform();
        builder.click().build().perform();
        driver.switchTo().defaultContent();
    }
}