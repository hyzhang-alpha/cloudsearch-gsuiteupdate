package info.alphanext.cloudsearch;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.enterprise.cloudsearch.sdk.indexing.IndexingApplication;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingConnector;
import com.google.enterprise.cloudsearch.sdk.indexing.template.FullTraversalConnector;
import com.google.enterprise.cloudsearch.sdk.indexing.template.Repository;

//import info.alphanext.cloudsearch.GSuiteUpdate;

public class GsuiteUpdateConnector {
    public static void main(String[] args) throws Exception {
        // Crawl the website

        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder("/tmp/crawler4j/");
        config.setPolitenessDelay(100);
        config.setMaxDepthOfCrawling(3);
        config.setMaxPagesToFetch(-1);
        config.setIncludeBinaryContentInCrawling(false);
        config.setResumableCrawling(false);
        //config.setHaltOnError(true);

        // Instantiate the controller for this crawl.
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);

        controller.addSeed("https://gsuiteupdates.googleblog.com/");
        int numberOfCrawlers = 20;

        // To demonstrate an example of how you can pass objects to crawlers, we use an AtomicInteger that crawlers
        // increment whenever they see a url which points to an image.
        AtomicInteger numSeenImages = new AtomicInteger();

        // The factory which creates instances of crawlers.
        CrawlController.WebCrawlerFactory<BasicCrawler> factory = () -> new BasicCrawler(numSeenImages);
        
        // Start the crawl. This is a blocking operation, meaning that your code
        // will reach the line after this only when crawling is finished.
        controller.start(factory, numberOfCrawlers);
        
        //generateFakedata();

        int crawlPageNumber = GSuiteUpdate.getGSuiteUpdateInstance().getGSuiteUpdateEntryNum();
        //logger.info("Crawling Complete, total websites to index: {}", crawlPageNumber);
        System.out.println("Crawling Complete, total websites to index: " + crawlPageNumber);

        //List<GSuiteUpdateEntry> gsuiteUpdateEntries = GSuiteUpdate.getGSuiteUpdateInstance().gsuiteUpdateEntries;

        // Index the data from crawling results
        Repository repository = new GsuiteUpdateRepository();
        IndexingConnector connector = new FullTraversalConnector(repository);
        
        IndexingApplication application = new IndexingApplication.Builder(connector, args)
            .build();
        application.start();
      }

      private static void generateFakedata() {

        GSuiteUpdateEntry post1 = new GSuiteUpdateEntry(99990, "How to conduct a Google Cloud Search in G Suite",
                  YearMonth.of(2020, 05),
                  "To conduct a GCS search in G Suite, you need to go to cloudsearch.google.com",
                  new ArrayList<String>(Arrays.asList("Cloud Search", "G Suite")), "google.com");

        GSuiteUpdateEntry post2 = new GSuiteUpdateEntry(99991, "How to create slide in G Suite",
                  YearMonth.of(2020, 07),
                  "Slies is a cool product in G Suite family",
                  new ArrayList<String>(Arrays.asList("Slides", "G Suite")), "image.google.com");

        GSuiteUpdate.getGSuiteUpdateInstance().addGSuiteUpdateEntry(post1);
        GSuiteUpdate.getGSuiteUpdateInstance().addGSuiteUpdateEntry(post2);
      }
}