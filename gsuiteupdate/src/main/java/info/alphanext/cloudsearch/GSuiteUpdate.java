package info.alphanext.cloudsearch;

import java.util.ArrayList;
import java.util.List;

public class GSuiteUpdate {
    
    private static GSuiteUpdate gSuiteUpdateInstance = null;

    private List<GSuiteUpdateEntry> gsuiteUpdateEntries = new ArrayList<>();
    
    private GSuiteUpdate() {
    }

    public static GSuiteUpdate getGSuiteUpdateInstance()
    {
        if(gSuiteUpdateInstance == null)
            gSuiteUpdateInstance = new GSuiteUpdate();

        return gSuiteUpdateInstance;
    }

    public List<GSuiteUpdateEntry> getGSuiteUpdateEnties()
    {
        return gsuiteUpdateEntries; 
    }

    public int getGSuiteUpdateEntryNum()
    {
        int entryNum = 0;

        if(gsuiteUpdateEntries != null)
            entryNum = gsuiteUpdateEntries.size();

        return entryNum;
    }

    public void addGSuiteUpdateEntry(GSuiteUpdateEntry entry)
    {
        gsuiteUpdateEntries.add(entry);
    }
}