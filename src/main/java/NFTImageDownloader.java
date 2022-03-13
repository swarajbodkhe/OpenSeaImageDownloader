import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class NFTImageDownloader {
    static String collectionCount = "(//div[contains(@class,'CollectionStatsBar')]//span)[1]/div";
    static String imgloader = "//div[contains(@class,'Image--loader')][1]";
    static String imgToBeCaptured = "//div[contains(@class,'AssetCardContentreact')]//img";
    static int imgcounter=0;
    public static void main(String[] args) throws IOException {

        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        Properties property = readPropertiesFile("src/main/resources/nftgen.properties");
        WebDriverWait wt = new WebDriverWait(driver, 5);
        JavascriptExecutor js = (JavascriptExecutor) driver;
        int actualImagesToBeDownloaded= Integer.parseInt(property.getProperty("imgCount"));
        driver.manage().window().maximize();
        String folderpath=property.getProperty("folderpath");
        try {
            driver.navigate().to(property.getProperty("url"));
            wt.until(ExpectedConditions.visibilityOf(driver.findElement(By.xpath(collectionCount))));
            String count = driver.findElement(By.xpath(collectionCount)).getText();
            driver.findElement(By.xpath("//i[contains(@class,'material-icons') and @value='apps']/parent::div")).click();
            int finalCount = 0;
            double cnt=0;
            if (count.contains("K")) {
                finalCount = (int)Double.parseDouble(count.replace("K", "")) * 1000;
                if (actualImagesToBeDownloaded < finalCount) {
                    finalCount = actualImagesToBeDownloaded;
                }
            } else {
                finalCount = Integer.parseInt(count);
            }
            Set<String> imgurls = new HashSet<>();
            js.executeScript("window.scrollBy(0,700)");
            try {
                wt.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(imgToBeCaptured)));
            }catch (TimeoutException te){
                js.executeScript("window.scrollBy(0,700)");
            }
            List<WebElement> imgUrlElements = wt.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(imgToBeCaptured)));
            int i = 0;
            while (i < (finalCount / 5) + 1) {
                js.executeScript("window.scrollBy(0,500)");
                try {
                    wt.until(ExpectedConditions.stalenessOf(driver.findElement(By.xpath(imgToBeCaptured))));
                } catch (TimeoutException te) {
                    imgUrlElements.addAll(wt.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(imgToBeCaptured))));
                }
                for (WebElement e :
                        imgUrlElements) {
                    try {
                        wt.until(ExpectedConditions.elementToBeClickable(e));
                    } catch (Exception ex) {
                        wt.until(ExpectedConditions.refreshed(ExpectedConditions.stalenessOf(e)));

                    }
                    imgurls.add(e.getAttribute("src"));
                }
                imgUrlElements.clear();
                if (imgurls.size()>=finalCount){
                    break;
                }
                i++;
            }
            driver.close();
            downloadImagesFromUrl(folderpath,imgurls);
        } finally {
            driver.quit();
        }

    }

    public static Properties readPropertiesFile(String fileName) throws IOException {
        FileInputStream fis = null;
        Properties prop = null;
        try {
            fis = new FileInputStream(fileName);
            prop = new Properties();
            prop.load(fis);
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            fis.close();
        }
        return prop;
    }

    public static void downloadImagesFromUrl(String folderpath,Set<String> listofItems) {
        URL imageURL = null;
        String[] arr=listofItems.toArray(new String[listofItems.size()]);
        int countF=imgcounter;
        for (int i = 0; i < arr.length; i++) {
            try {
                //generate url
                imageURL = new URL(arr[i]);

                try(InputStream in = new URL(arr[i]).openStream()){
                    Files.copy(in, Paths.get(folderpath+"/"+ countF+".png"));

                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            countF++;
        }
        imgcounter=countF;
    }
}
