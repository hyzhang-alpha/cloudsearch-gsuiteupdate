package info.alphanext.cloudsearch;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;
import java.time.YearMonth;

import org.apache.http.Header;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class BasicCrawler extends WebCrawler {

    private static final Pattern IMAGE_EXTENSIONS = Pattern.compile(".*\\.(bmp|gif|jpg|png)$");

    private final AtomicInteger numSeenImages;

    /**
     * Creates a new crawler instance.
     *
     * @param numSeenImages This is just an example to demonstrate how you can pass objects to crawlers. In this
     * example, we pass an AtomicInteger to all crawlers and they increment it whenever they see a url which points
     * to an image.
     */
    public BasicCrawler(AtomicInteger numSeenImages) {
        this.numSeenImages = numSeenImages;
    }

    /**
     * You should implement this function to specify whether the given url
     * should be crawled or not (based on your crawling logic).
     */
    @Override
    public boolean shouldVisit(Page referringPage, WebURL url) {
        String href = url.getURL().toLowerCase();
        // Ignore the url if it has an extension that matches our defined set of image extensions.
        if (IMAGE_EXTENSIONS.matcher(href).matches()) {
            numSeenImages.incrementAndGet();
            return false;
        }

        // Only accept the url if it is in the "gsuiteupdate.googleblog.com" domain and protocol is "http".
        return href.startsWith("https://gsuiteupdates.googleblog.com/");
    }

    /**
     * This function is called when a page is fetched and ready to be processed
     * by your program.
     */
    @Override
    public void visit(Page page) {
        int docid = page.getWebURL().getDocid();
        String url = page.getWebURL().getURL();
        String domain = page.getWebURL().getDomain();
        String path = page.getWebURL().getPath();
        String subDomain = page.getWebURL().getSubDomain();
        String parentUrl = page.getWebURL().getParentUrl();
        String anchor = page.getWebURL().getAnchor();

        logger.debug("Docid: {}", docid);
        logger.info("URL: {}", url);
        logger.debug("Domain: '{}'", domain);
        logger.debug("Sub-domain: '{}'", subDomain);
        logger.debug("Path: '{}'", path);
        logger.debug("Parent page: {}", parentUrl);
        logger.debug("Anchor text: {}", anchor);

        if (page.getParseData() instanceof HtmlParseData) {
            HtmlParseData htmlParseData = (HtmlParseData) page.getParseData();
            
            String text = htmlParseData.getText();
            String html = htmlParseData.getHtml();

            // Retreive label

            int currentPosition = 0;
            int maxPosition = html.length();
            
            List<String> listTags = new ArrayList<>();
            String tagInstance = "";
            int beginIndex = -1;
            int endIndex = -1;

            while(currentPosition < maxPosition)
            {       
                tagInstance = "";
                beginIndex = html.indexOf("rel='tag'>", currentPosition);
                endIndex = html.indexOf("</a>", beginIndex);

                if(beginIndex !=-1 && endIndex != -1)
                {
                    tagInstance = html.substring(beginIndex + 10, endIndex);
                    if(tagInstance != "")
                        listTags.add(tagInstance.trim());
                }
                if(beginIndex == -1)
                    break;
                
                currentPosition = endIndex + 1;
            }
            
            for(String iTag : listTags)
            {
                logger.debug("hyzhangTag: {}", iTag);
            }
            // End retreive label
            
            // Retreive Blog Body

            String strPostBody = "";
            int postbodyBeginIndex = html.indexOf("<div class='post-body'>", 0);
            int postbodyEndIndex = html.indexOf("<div class='share'>", postbodyBeginIndex);
            
            if(postbodyBeginIndex != -1 && postbodyEndIndex != -1)
            {
                strPostBody = html.substring(postbodyBeginIndex, postbodyEndIndex);
                strPostBody = strPostBody.replaceAll("\\<.*?\\>", "");
                strPostBody = strPostBody.replace("\n", " ");
            }

            logger.debug("Blog Body: {}", strPostBody);

            // End retreive Blog Body

            // Retrive Pubish Date
            String strYear = "1900";
            String strMonth = "01";
    
            // Sample format "/2020/02/autocorrect-docs-ga.html"
            if (path.matches("/[0-9]{4}/[0-9]{2}/.*"))
            {
                strYear = path.substring(1, 5);
                strMonth = path.substring(6, 8);            
            }
    
            int intPublishYear = Integer.parseInt(strYear);
            int intPublishMonth = Integer.parseInt(strMonth);

            YearMonth publishDate = YearMonth.of(intPublishYear, intPublishMonth);
            // End retreive Publish Date

            Set<WebURL> links = htmlParseData.getOutgoingUrls();

            logger.debug("Text length: {}", text.length());
            logger.debug("Html length: {}", html.length());
            logger.debug("Number of outgoing links: {}", links.size());

            GSuiteUpdateEntry gsuiteUpdateEntry = new GSuiteUpdateEntry(docid, anchor, publishDate, strPostBody, listTags, url);
            GSuiteUpdate.getGSuiteUpdateInstance().addGSuiteUpdateEntry(gsuiteUpdateEntry);
        }

        Header[] responseHeaders = page.getFetchResponseHeaders();
        if (responseHeaders != null) {
            logger.debug("Response headers:");
            for (Header header : responseHeaders) {
                logger.debug("\t{}: {}", header.getName(), header.getValue());
            }
        }
    }
}
