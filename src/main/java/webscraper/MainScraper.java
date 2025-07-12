package webscraper;

import org.openqa.selenium.By;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class MainScraper {
    public static void main(String[] args) {
        // Set path to ChromeDriver
        System.setProperty("webdriver.chrome.driver", "C://Users//Prashanth.SR//Downloads//Selenium New//chromedriver-win64//chromedriver.exe");

        WebDriver driver = new ChromeDriver();

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().window().maximize();

            // Step 1: Go to homepage
            driver.get("https://elpais.com/");
            Thread.sleep(3000);

            // Step 2: Accept cookies
            try {
                WebElement acceptBtn = driver.findElement(By.xpath("//span[contains(text(),'Accept')]"));
                acceptBtn.click();
            } catch (NoSuchElementException e) {
                System.out.println("No cookie banner.");
            }

            // Step 3: Navigate to Opinión section
            WebElement opinionLink = driver.findElement(By.xpath("//div[@class='sm _df']//a[normalize-space()='Opinión']"));
            opinionLink.click();
            Thread.sleep(3000);

            // Step 4: Collect first 5 article URLs
            List<WebElement> articleLinks = driver.findElements(By.cssSelector("article h2 a[href*='/opinion/']"));
            System.out.println("Articles found: " + articleLinks.size());

            List<String> articleUrls = new ArrayList<>();
            int maxArticles = Math.min(5, articleLinks.size());
            List<String> translatedTitles = new ArrayList<>();

            for (int i = 0; i < maxArticles; i++) {
                articleUrls.add(articleLinks.get(i).getAttribute("href"));
            }

            // Step 5: Visit each article one by one
            int count = 1;
            for (String url : articleUrls) {
                System.out.println("\nOpening article " + count + ": " + url);
                driver.navigate().to(url);
                Thread.sleep(3000);

                String title = "";
                String content = "";

                try {
                    title = driver.findElement(By.tagName("h1")).getText();
                } catch (Exception e) {
                    System.out.println("Title not found.");
                }

                try {
                    content = driver.findElement(By.tagName("h2")).getText(); // You can also try <p> or article text divs
                } catch (Exception e) {
                    System.out.println("Content not found.");
                }

                System.out.println("\n--- Article " + count + " ---");
                System.out.println("Title: " + title);
                System.out.println("Content: " + (content.length() > 300 ? content.substring(0, 300) + "..." : content));
                
                String translatedTitle = Translator.translateToEnglish(title);
                System.out.println("Translated Title: " + translatedTitle);
                
                translatedTitles.add(translatedTitle);


                try {
                    WebElement img = driver.findElement(By.cssSelector("figure img"));
                    String imgUrl = img.getAttribute("src");
                    FileUtils.copyURLToFile(new URL(imgUrl), new File("cover" + count + ".jpg"));
                    System.out.println("Image downloaded: cover" + count + ".jpg");
                } catch (Exception e) {
                    System.out.println("No image found for this article.");
                }

                count++;
            }
            
         System.out.println("\n🔍 Analyzing Word Frequency Across Translated Titles...");

         // Join all titles into one string
         String allTitles = String.join(" ", translatedTitles);

         // Normalize text: lowercase and remove punctuation
         allTitles = allTitles.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "");

         // Split into words
         String[] words = allTitles.split("\\s+");

         // Count frequency
         Map<String, Integer> wordCount = new HashMap<>();
         for (String word : words) {
             if (word.isBlank()) continue;
             wordCount.put(word, wordCount.getOrDefault(word, 0) + 1);
         }

         // Print words repeated more than twice
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
            driver.quit(); // close browser when done
        }
    }
}
