package demo;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
// import io.github.bonigarcia.wdm.WebDriverManager;
import demo.wrappers.Wrappers;

public class TestCases {
    ChromeDriver driver;

    /*
     * TODO: Write your tests here with testng @Test annotation. 
     * Follow `testCase01` `testCase02`... format or what is provided in instructions
     */

     @Test
     public void testCase01() throws InterruptedException{
        Wrappers wrappers = new Wrappers(driver);
        driver.get("https://www.scrapethissite.com/pages/");

        // Navigate to the page with the hockey teams table
        wrappers.clickElement(By.linkText("Hockey Teams: Forms, Searching and Pagination"));
        wrappers.waitForVisibility(By.xpath("//tbody/tr[last()]"));

        // ObjectMapper for Jackson
        ObjectMapper mapper = new ObjectMapper();

        // ArrayList to store the team data
        List<Map<String, Object>> teamDataList = new ArrayList<>();
        long epochTime = System.currentTimeMillis() / 1000L; 

        // Iterate through 4 pages
        for (int page = 1; page <= 4; page++) {
            // Locate the table rows
            List<WebElement> tableRows = wrappers.findElements(By.xpath("//table/tbody/tr"));

            // Iterate over each row in the table
            for (int i = 1; i < tableRows.size(); i++) {
                String teamName = wrappers.findElement(By.xpath("(//td[1])["+i+"]")).getText();
                String year = wrappers.findElement(By.xpath("(//td[2])["+i+"]")).getText();
                String winAvgs = wrappers.findElement(By.xpath("(//td[6])["+i+"]")).getText();

                double winAvg = Double.parseDouble(winAvgs);

                // Only collect teams with win % less than 40%
                if (winAvg < 0.40) {
                    Map<String, Object> teamData = new HashMap<>();
                    teamData.put("epochTime", epochTime);
                    teamData.put("teamName", teamName);
                    teamData.put("year", year);
                    teamData.put("winAvgs", winAvg);

                    teamDataList.add(teamData);
                }
            }

     
            if (page <= 4) {
                WebElement nextButton = wrappers.findElement(By.xpath("//ul[@class='pagination']/li/a[contains(text(),'"+page+"')]"));
                if (nextButton.isDisplayed()) {
                    nextButton.click();
                    wrappers.waitForVisibility(By.xpath("//tbody/tr[last()]")); 
                } else {
                    break; 
                }
            }
        }

        // Convert the ArrayList to JSON and write it to a file
        try {
            File jsonFile = new File("hockey-team-data.json");
            mapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, teamDataList);
            System.out.println("Data written to hockey-team-data.json");
        } catch (IOException e) {
            e.printStackTrace();
        }

     }

     @Test
     public void testCase02() throws IOException {
         Wrappers wrappers = new Wrappers(driver);
         driver.get("https://www.scrapethissite.com/pages/");
     
         // Navigate to the Oscar Winning Films page
         wrappers.clickElement(By.linkText("Oscar Winning Films: AJAX and Javascript"));
         wrappers.waitForVisibility(By.xpath("//a[@class='year-link']"));
     
         // Initialize necessary variables
         ObjectMapper mapper = new ObjectMapper();
         List<Map<String, Object>> movieList = new ArrayList<>();
         long epochTime = System.currentTimeMillis() / 1000L; // Epoch time for each scrape
     
         // Get all year elements
         List<WebElement> years = wrappers.findElements(By.xpath("//a[@class='year-link']"));
     
         // Iterate over the years
         for (WebElement yearLink : years) {
             String year = yearLink.getText();
             yearLink.click(); // Click the year
     
             // Wait for the table to load
             wrappers.waitForVisibility(By.xpath("//tbody[@id='table-body']/tr[last()]"));
             boolean isWinner;
             // Get the rows of the table (top 5 movies)
             List<WebElement> rows = wrappers.findElements(By.xpath("//tbody[@id='table-body']//tr"));
             for (int i = 0; i < Math.min(5, rows.size()); i++) { // Limit to top 5 movies
                 WebElement row = rows.get(i);
                 String title = row.findElement(By.xpath(".//td[1]")).getText();
                 String nominations = row.findElement(By.xpath(".//td[2]")).getText();
                 String awards = row.findElement(By.xpath(".//td[3]")).getText();

                 try {
                    isWinner = row.findElement(By.xpath(".//td[4]/i")).isDisplayed(); // Best Picture winner flag
                 } catch (Exception e) {
                    isWinner = false;
                 }
                 
     
                 // Store movie data in a map
                 Map<String, Object> movieData = new HashMap<>();
                 movieData.put("epochTime", epochTime);
                 movieData.put("year", year);
                 movieData.put("title", title);
                 movieData.put("nominations", nominations);
                 movieData.put("awards", awards);
                 movieData.put("isWinner", isWinner);
     
                 // Add to the list of movies
                 movieList.add(movieData);
             }

             wrappers.waitForVisibility(By.xpath("//a[@class='year-link']"));
         }
     
         // Convert the movie list to a JSON file
         File outputFile = new File("oscar-winner-data.json");
         mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, movieList);
     
         // Assert that the file exists and is not empty
         Assert.assertTrue(outputFile.exists(), "JSON file should exist");
         Assert.assertTrue(outputFile.length() > 0, "JSON file should not be empty");
     
         System.out.println("Data written to JSON file successfully.");
     }
     
     
    /*
     * Do not change the provided methods unless necessary, they will help in automation and assessment
     */
    @BeforeTest
    public void startBrowser()
    {
        System.setProperty("java.util.logging.config.file", "logging.properties");

        System.out.println("Browser : Chrome Stated");

        // NOT NEEDED FOR SELENIUM MANAGER
        // WebDriverManager.chromedriver().timeout(30).setup();

        ChromeOptions options = new ChromeOptions();
        LoggingPreferences logs = new LoggingPreferences();

        logs.enable(LogType.BROWSER, Level.ALL);
        logs.enable(LogType.DRIVER, Level.ALL);
        options.setCapability("goog:loggingPrefs", logs);
        options.addArguments("--remote-allow-origins=*");

        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY, "build/chromedriver.log"); 

        driver = new ChromeDriver(options);

        driver.manage().window().maximize();
    }

    @AfterTest
    public void endTest()
    {
        // driver.close();
        driver.quit();

    }
}