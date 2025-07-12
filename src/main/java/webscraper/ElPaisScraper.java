package webscraper;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.*;

public class ElPaisScraper {

    public static void runScraper(WebDriver driver) {
        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.get("https://elpais.com/");
            Thread.sleep(3000);

            // Accept cookies
            try {
                WebElement acceptBtn = driver.findElement(By.xpath("//span[contains(text(),'Accept')]"));
                ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", acceptBtn);
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
                wait.until(ExpectedConditions.elementToBeClickable(acceptBtn)).click();
                System.out.println("✅ Cookie accepted.");
            } catch (Exception e) {
                System.out.println("⚠️ No cookie popup or unable to click.");
            }

            // Go to Opinión section
            WebElement opinionLink = driver.findElement(By.xpath("//div[@class='sm _df']//a[normalize-space()='Opinión']"));
            opinionLink.click();
            Thread.sleep(3000);

            // Collect article links
            List<WebElement> articleLinks = driver.findElements(By.cssSelector("article h2 a[href*='/opinion/']"));
            System.out.println("📰 Articles found: " + articleLinks.size());

            List<String> articleUrls = new ArrayList<>();
            List<String> translatedTitles = new ArrayList<>();

            for (int i = 0; i < Math.min(5, articleLinks.size()); i++) {
                articleUrls.add(articleLinks.get(i).getAttribute("href"));
            }

            int count = 1;
            for (String url : articleUrls) {
                driver.navigate().to(url);
                Thread.sleep(3000);

                String title = "";
                String content = "";
                String translatedTitle = "";
                String imageStatus = "No image found.";

                try {
                    title = driver.findElement(By.tagName("h1")).getText();
                } catch (Exception e) {
                    title = "Title not found.";
                }

                try {
                    content = driver.findElement(By.tagName("h2")).getText();
                } catch (Exception e) {
                    content = "Content not found.";
                }

                try {
                    translatedTitle = Translator.translateToEnglish(title);
                } catch (Exception e) {
                    translatedTitle = "Translation failed.";
                }

                try {
                    WebElement img = driver.findElement(By.cssSelector("figure img"));
                    String imgUrl = img.getAttribute("src");
                    FileUtils.copyURLToFile(new URL(imgUrl), new File("cover" + count + ".jpg"));
                    imageStatus = "Image downloaded: cover" + count + ".jpg";
                } catch (Exception e) {
                    imageStatus = "No image found for this article.";
                }

                translatedTitles.add(translatedTitle);

                // ✅ Grouped clean output
                System.out.println("\n--- Article " + count + " ---");
                System.out.println("URL: " + url);
                System.out.println("Title: " + title);
                System.out.println("Content: " + (content.length() > 300 ? content.substring(0, 300) + "..." : content));
                System.out.println("Translated Title: " + translatedTitle);
                System.out.println(imageStatus);

                count++;
            }

            // 🔍 Word frequency analysis
            System.out.println("\n🔍 Analyzing Word Frequency Across Translated Titles...");
            String allTitles = String.join(" ", translatedTitles).toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");
            String[] words = allTitles.split("\\s+");

            Map<String, Integer> wordCount = new HashMap<>();
            for (String word : words) {
                if (!word.isBlank()) {
                    wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
                }
            }

            System.out.println("\n📈 Words Repeated More Than Twice:");
            boolean found = false;
            for (Map.Entry<String, Integer> entry : wordCount.entrySet()) {
                if (entry.getValue() > 2) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                    found = true;
                }
            }
            if (!found) {
                System.out.println("No words repeated more than twice.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
