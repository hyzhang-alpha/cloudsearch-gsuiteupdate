package info.alphanext.cloudsearch;

import com.google.api.client.http.ByteArrayContent;
import com.google.api.services.cloudsearch.v1.model.Item;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterable;
import com.google.enterprise.cloudsearch.sdk.CheckpointCloseableIterableImpl;
import com.google.enterprise.cloudsearch.sdk.InvalidConfigurationException;
import com.google.enterprise.cloudsearch.sdk.RepositoryException;
import com.google.enterprise.cloudsearch.sdk.StartupException;
import com.google.enterprise.cloudsearch.sdk.config.ConfigValue;
import com.google.enterprise.cloudsearch.sdk.config.Configuration;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingItemBuilder.FieldOrValue;
import com.google.enterprise.cloudsearch.sdk.indexing.IndexingService;
import com.google.enterprise.cloudsearch.sdk.indexing.template.*;
import com.google.enterprise.cloudsearch.sdk.indexing.Acl;


import java.time.YearMonth;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.Iterator;
import java.util.stream.IntStream;


public class GsuiteUpdateRepository implements Repository {

    /**
     * Log output
     */
    private Logger log = Logger.getLogger(GsuiteUpdateRepository.class.getName());
    private List<GSuiteUpdateEntry> gsuiteUpdateEntries = null;
    private int numberOfDocuments;

    GsuiteUpdateRepository() {
    }

    /**
     * Initializes the connection to GSuiteUpdate
     *
     * @param context the {@link RepositoryContext}, not used here
     */
    @Override
    public void init(RepositoryContext context) throws StartupException {
        log.info("Initializing Repository - G Suite Update");

        gsuiteUpdateEntries = GSuiteUpdate.getGSuiteUpdateInstance().getGSuiteUpdateEnties();
        if (gsuiteUpdateEntries == null)
            throw new InvalidConfigurationException("Failed to retrive data from crawling results.");
        else
            numberOfDocuments = gsuiteUpdateEntries.size();

    }

    /**
     * Performs any data repository shut down code here.
     */
    @Override
    public void close() {
        log.info("Closing repository");
    }

    /**
     * Gets all the data repository documents.
     *
     * This is the core of the {@link Repository} implemented code for a full
     * traversal connector. A complete traversal of the entire data repository is
     * performed here.
     *
     * For this sample there are only a small set of statically created documents
     * defined.
     *
     * @param checkpoint save state from last iteration
     * @return An iterator of {@link RepositoryDoc} instances
     */
    @Override
    public CheckpointCloseableIterable<ApiOperation> getAllDocs(byte[] checkpoint) {
        log.info("Start retrieving all posts.");

        Iterator<ApiOperation> allDocs = IntStream.range(0, numberOfDocuments).mapToObj(this::buildDocument).iterator();

        // [START cloud_search_content_sdk_checkpoint_iterator]
        CheckpointCloseableIterable<ApiOperation> iterator = new CheckpointCloseableIterableImpl.Builder<>(allDocs)
                .build();
        // [END cloud_search_content_sdk_checkpoint_iterator]
        return iterator;
    }

    /**
     * Creates a GSuite Update document for indexing.
     *
     * @param id unique local id for the document
     * @return the fully formed document ready for indexing
     */
    private ApiOperation buildDocument(int index) {
        
        GSuiteUpdateEntry gSuiteUpdateEntry = gsuiteUpdateEntries.get(index);

        // Make the document publicly readable within the domain
        Acl acl = new Acl.Builder().setReaders(Collections.singletonList(Acl.getCustomerPrincipal())).build();

        Hasher hasher = Hashing.farmHashFingerprint64().newHasher();
        hasher.putUnencodedChars(gSuiteUpdateEntry.getEntryUrl());
        String resourceName = hasher.hash().toString();

        FieldOrValue<String> title = FieldOrValue.withValue(gSuiteUpdateEntry.getEntryTitle());
        FieldOrValue<String> url = FieldOrValue.withValue(gSuiteUpdateEntry.getEntryUrl());
    
        // Structured data based on the schema
        Multimap<String, Object> structuredData = ArrayListMultimap.create();

        YearMonth postDateMonth = gSuiteUpdateEntry.getEntryPublishDate();
        String strPostYearMonth = postDateMonth.getYear() + "-" + postDateMonth.getMonthValue();
        structuredData.put("postYearMonth", strPostYearMonth);

        Date datePostDateRaw = new Date(postDateMonth.getYear(), postDateMonth.getMonthValue() - 1, 1);
        structuredData.put("postDateRaw", datePostDateRaw);

        for (String tag : gSuiteUpdateEntry.getEntryTags()) {
            structuredData.put("productTag", tag);
        }
    
        Item item = IndexingItemBuilder.fromConfiguration(resourceName)
            .setTitle(title)
            .setSourceRepositoryUrl(url)
            .setItemType(IndexingItemBuilder.ItemType.CONTENT_ITEM)
            .setObjectType("gsuiteUpdatePost")
            .setAcl(acl)
            .setValues(structuredData)
            .setVersion(Longs.toByteArray(System.currentTimeMillis()))
            .build();
    
        // Index the file content too
        String entryBody = gSuiteUpdateEntry.getEntryMainBody();
        ByteArrayContent byteContent = ByteArrayContent.fromString("text/plain", entryBody);
        
        log.info("Document Built Successfully - " + title);

        return new RepositoryDoc.Builder()
            .setItem(item)
            .setContent(byteContent, IndexingService.ContentFormat.TEXT)
            .setRequestMode(IndexingService.RequestMode.SYNCHRONOUS)
            .build();
        
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This method is not required by the FullTraversalConnector and is
     * unimplemented.
     */
    @Override
    public CheckpointCloseableIterable<ApiOperation> getChanges(byte[] checkpoint) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This method is not required by the FullTraversalConnector and is
     * unimplemented.
     */
    @Override
    public CheckpointCloseableIterable<ApiOperation> getIds(byte[] checkpoint) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This method is not required by the FullTraversalConnector and is
     * unimplemented.
     */
    @Override
    public ApiOperation getDoc(Item item) {
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * This method is not required by the FullTraversalConnector and is
     * unimplemented.
     */
    @Override
    public boolean exists(Item item) {
        return false;
    }

}