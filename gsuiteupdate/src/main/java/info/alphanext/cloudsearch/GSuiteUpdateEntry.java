package info.alphanext.cloudsearch;

import java.time.YearMonth;
import java.util.List;

public class GSuiteUpdateEntry {

    private final int entryDocId;
    private final String entryTitle;
    private final YearMonth entryPublishDate;
    private final String entryMainBody;
    private final List<String> entryTags;
    private final String entryURL;
    
    public GSuiteUpdateEntry(int entryDocId, String entryTitle, YearMonth entryPublishDate, String entryMainBody, 
    List<String> entryTags, String entryURL)
    {
        this.entryDocId = entryDocId;
        this.entryTitle = entryTitle;
        this.entryPublishDate = entryPublishDate;
        this.entryMainBody = entryMainBody;
        this.entryTags = entryTags;
        this.entryURL = entryURL;
    }

    public String getEntryUrl()
    {
        return this.entryURL;
    }

    public int getEntryDocId()
    {
        return this.entryDocId;
    }

    public String getEntryTitle()
    {
        return this.entryTitle;
    }

    public YearMonth getEntryPublishDate()
    {
        return this.entryPublishDate;
    }

    public String getEntryMainBody()
    {
        return this.entryMainBody;
    }

    public List<String> getEntryTags()
    {
        return this.entryTags;
    }
}