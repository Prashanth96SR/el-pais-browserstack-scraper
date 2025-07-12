package webscraper;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BrowserStackTest {

    public static final String USERNAME = "prashanth_WbDZSG";
    public static final String AUTOMATE_KEY = "jqEshFCtKq3dov2b1gkx";
    public static final String BROWSERSTACK_URL =
            "https://" + USERNAME + ":" + AUTOMATE_KEY + "@hub-cloud.browserstack.com/wd/hub";

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(5); // Using 5 parallel threads

        Runnable[] tests = new Runnable[]{
        		() -> runTest("Chrome", "latest", "Windows", "11"),
                () -> runTest("Firefox", "latest", "Windows", "10"),
                () -> runTest("Edge", "latest", "Windows", "11"),
                () -> runTest("Safari", "latest", "OS X", "Ventura"),
                () -> runTest("Chrome", "latest", "android", "13.0")
        };

        for (Runnable test : tests) {
            executor.submit(test);
        }

        executor.shutdown();
    }

    public static void runTest(String browser, String browserVersion, String platform, String platformVersion) {
        try {
            MutableCapabilities caps = new MutableCapabilities();
            caps.setCapability("browserName", browser);
            caps.setCapability("browserVersion", browserVersion);

            Map<String, Object> bstackOptions = new HashMap<>();
            bstackOptions.put("os", platform);
            bstackOptions.put("osVersion", platformVersion);
            bstackOptions.put("buildName", "CrossBrowserScrapeBuild");
            bstackOptions.put("sessionName", browser + " on " + platform + " " + platformVersion);
            bstackOptions.put("projectName", "ElPais Scraper");

            if (platform.toLowerCase().contains("android")) {
                bstackOptions.put("deviceName", "Google Pixel 7");
                bstackOptions.put("realMobile", "true");
            }

            caps.setCapability("bstack:options", bstackOptions);

            WebDriver driver = new RemoteWebDriver(new URL(BROWSERSTACK_URL), caps);

            System.out.println("✅ Opened on: " + browser);
            ElPaisScraper.runScraper(driver);  // Reusable logic call
            driver.quit(); // Important!

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
