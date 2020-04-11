package info.alphanext.cloudsearch;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

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
}