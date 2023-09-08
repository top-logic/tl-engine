/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.lucene;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOError;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.miscellaneous.LimitTokenCountAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.TieredMergePolicy;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.LockObtainFailedException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.tools.LockWaitMonitor;
import com.top_logic.basic.util.ExponentialBackoff;
import com.top_logic.convert.FormatConverterFactory;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.knowledge.analyze.lucene.AnalyzerFactory;
import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.indexing.IndexException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.SearchException;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Representation of the Lucene index.
 * <p>
 * This class contains methods for creating a Lucene index, getting Lucene index readers, writers
 * and searchers. This class is used by the
 * {@link com.top_logic.knowledge.searching.lucene.LuceneSearchEngine}.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@ServiceDependencies({
	FormatConverterFactory.Module.class,
	MimeTypes.Module.class,
	PersonManager.Module.class,
	PersistencyLayer.Module.class,
})
public final class LuceneIndex extends ConfiguredManagedClass<LuceneIndex.LuceneConfig> {
	
	/**
	 * Configuration for {@link LuceneIndex}.
	 */
	public static interface LuceneConfig extends ConfiguredManagedClass.Config<LuceneIndex> {

		/**
		 * The document index location.
		 */
		String getIndexLocation();

		/**
		 * Factory that creates the used document {@link Analyzer}.
		 */
		AnalyzerFactory.AnalyzerFactoryConfig<?> getDocumentAnalyzerFactory();

		/**
		 * After waiting that many millisecond a request for a Lucene writer lock will fail.
		 */
		@IntDefault(Integer.MAX_VALUE)
		int getTimeoutLockRequestReader();

		/**
		 * After waiting that many millisecond a request for a Lucene writer lock will fail.
		 */
		@IntDefault(Integer.MAX_VALUE)
		int getTimeoutLockRequestWriter();

		/**
		 * After waiting that many millisecond a request for a Lucene writer lock will be logged as
		 * unusually long.
		 */
		@IntDefault(15 * 60 * 1000)
		int getWarnTimeoutLockRequestReader();

		/**
		 * After waiting that many millisecond a request for a Lucene writer lock will be logged as
		 * unusually long.
		 */
		@IntDefault(15 * 60 * 1000)
		int getWarnTimeoutLockRequestWriter();

		/**
		 * Number of millisecond to wait before aborting the indexing process after the
		 * {@link LuceneIndexingService} was {@link #shutDown() shut down}.
		 * 
		 * <p>
		 * Note: Aborting the indexing process does not work, if the indexer is waiting for a reader
		 * or writer lock. Therefore, set {@link #getTimeoutLockRequestReader} and
		 * {@link #getTimeoutLockRequestWriter}, too.
		 * </p>
		 * 
		 * <p>
		 * <b>Warning:</b> The index will be incomplete when indexing is aborted! After a forced
		 * shutdown, the index must be administratively rebuilt.
		 * </p>
		 */
		@IntDefault(Integer.MAX_VALUE)
		int getTimeoutAbortIndexingForShutdown();

		/**
		 * If the queue is longer than that, the administrator should be warn.
		 */
		@IntDefault(1000)
		int getWarnQueueSize();

	}
	
	private static final boolean DEBUG = Logger.isDebugEnabled(LuceneIndex.class);
    
	private static final File[] NO_LOCK_FILES = new File[0];

    /** The name of the KO field in a Lucene document */
    public static final String FIELD_KO_ID            = "id";
    
    /** The document type of the document to index */
    public static final String FIELD_DOCTYPE          = "docType";

    /** The name of the description field in a Lucene document */
    public static final String FIELD_DESCRIPTION      = "description";
    
    /** The maximum size of the field description */
    public static final int FIELD_DESCRIPTION_MAXSIZE = 200;

    /** The name of the contents field in a Lucene document */
    public static final String FIELD_CONTENTS         = "contents";
    
    /** The key for the lucene lock file directory, the same as the indexing dir. */
    public static final String LOCK_DIRECTORY_KEY     = "org.apache.lucene.lockdir";

    /** Maximum time to index a Document before it will be logged */
    static final int INDEX_MILLIS             = 1000 * 3; // 3 Seconds

    /**  Maximum time to delete a Document before it will be logged */
    static final int DELETE_MILLIS            = 1000;     // one Second
    
    /** Accept Files ending with ".lock". */
    static final FilenameFilter LOCK_FILTER = new FilenameFilter() {
        @Override
		public boolean accept(File aFile, String aName) {
            return aName.toLowerCase().endsWith(".lock");
        }
    };


    private static final int DEFAULT_MAX_FILED_LENGTH_VALUE = 10000;

    /** The directory location of the Lucene index */
    private volatile File indexLocation;

    /** The index adding thread responsible for indexing/deleting documents */
    private LuceneThread myThread;

	/**
	 * Shared analyzer
	 * 
	 * @see #getDefaultDocumentAnalyzer()
	 */
    private Analyzer documentAnalyzer;
    
    /**
     * Is used for timeouts when the shutdown was requested but there is still work to do.
     */
    private Maybe<Long> timestampShutdownRequest = Maybe.none();
    
	/** @see #wasReindexed() */
	private boolean _reindexed = false;

	private final LuceneConfig config;

	/**
	 * Protected constructor for singleton pattern. Initialization with a given
	 * index location. Creates the Lucene index in case it does not exist yet.
	 */
	public LuceneIndex(InstantiationContext context, LuceneConfig config) throws ConfigurationException {
		super(context, config);
		this.config = config;
		
		{
			this.indexLocation = new File(config.getIndexLocation());
			// KHA: This will fail once a SecurityManger is installed ...
			System.setProperty(LOCK_DIRECTORY_KEY, this.indexLocation.getAbsolutePath());
			if (DEBUG) {
				Logger.debug("Lucene index location set to: " + indexLocation.getAbsolutePath(), LuceneIndex.class);
			}
		}
		
		this.documentAnalyzer = createAnalyzer(context);

		// Assure index directory exists and there is no lock
		boolean indexExists = indexExists();
		if (!indexExists) {
			try {
				createIndex();
				setReindexed(true);
			} catch (Exception ex) {
				throw new ConfigurationException("Unable to create index in '" + this.indexLocation + "'", ex);
			}
		}
		
		createAndStartThread();
	}

	void init(InstantiationContext context) {
		if ((myThread != null) || (documentAnalyzer != null)) {
			return; // Is apparently already started. Nothing to do
		}
		Logger.info("Initiating Lucene", LuceneIndex.class);
		this.documentAnalyzer = createAnalyzer(context);
		if (documentAnalyzer == null) {
			return;
		}

		createAndStartThread();
	}

	private void createAndStartThread() {
		HistoryManager historyManager = HistoryUtils.getHistoryManager(PersistencyLayer.getKnowledgeBase());
		this.myThread = new LuceneThread(this, this, historyManager);
		this.myThread.start();
		Logger.info("Initiated Lucene", LuceneIndex.class);
	}
    
    /**
     * Closes the given indexWriter without throwing {@link IOException}. 
     * 
     * @param indexWriter must not be <code>null</code>
     * 
     * @see IndexWriter#close()
     */
	static void closeWriter(IndexWriter indexWriter) {
		 try {
			indexWriter.close();
		} catch (IOException ioe) {
			Logger.error("Failed to close Lucene writer.", ioe, LuceneIndex.class);
		}
	}

	/**
	 * Convenience shortcut for {@link #openReaderWaitingAndRetrying(File)}
	 * with the parameter "locationOfIndex" set to {@link #indexLocation}.
	 */
	public IndexReader openReaderWaitingAndRetrying() {
		return openReaderWaitingAndRetrying(indexLocation);
	}
	
    /**
	 * Test and wait for the index reader to be unlocked by the administrator or nasty thread
	 * blocking this delete thread.
	 * 
	 * If timeout of {@link LuceneConfig#getTimeoutLockRequestReader()} milliseconds is reached, the
	 * administrator is informed, that this thread is waiting unexpectedly long for the lock.
	 */
	public IndexReader openReaderWaitingAndRetrying(File locationOfIndex) {
		LockWaitMonitor.INSTANCE.requestingLock("Lucene Reader Lock at: " + locationOfIndex,
			config.getTimeoutLockRequestReader(), config.getWarnTimeoutLockRequestReader());
		try {
			ExponentialBackoff timeoutGenerator = new ExponentialBackoff(500, Math.sqrt(2), 15 * 1000);
	        Maybe<IndexReader> reader = tryOpenReaderNow(locationOfIndex);
	        while ( ! reader.hasValue()) {
	    		LockWaitMonitor.INSTANCE.requestStillWaiting();
				timeoutGenerator.sleep();
	            reader = tryOpenReaderNow(locationOfIndex);
	        }
	        return reader.get();
		} finally {
			LockWaitMonitor.INSTANCE.requestFinished();
		}
	}
	
	/**
	 * Convenience short-cut for {@link #tryOpenReaderNow(File)}
	 * with the parameter "locationOfIndex" set to {@link #indexLocation}.
	 */
	public Maybe<IndexReader> tryOpenReaderNow() {
		return tryOpenReaderNow(indexLocation);
	}
	
	/**
	 * Tries to open an {@link IndexReader} for the index at the given location.
	 * If the lock could not be obtained (an {@link LockObtainFailedException} is thrown), Maybe.none() is returned.
	 * Other {@link Throwable} are not handled.
	 */
	public Maybe<IndexReader> tryOpenReaderNow(File locationOfIndex) {
		try {
			IndexReader reader = openReaderNowOrFail(locationOfIndex);
			if (DEBUG) {
				String message = "Succeeded to open Lucene reader.";
				Logger.debug(message, LuceneIndex.class);
			}
			return Maybe.toMaybe(reader);
		}
		catch (LockObtainFailedException exception) {
			String message = "Failed to obtain Lucene reader lock: " + exception.getMessage();
			Logger.info(message, LuceneIndex.class);
			return Maybe.none();
		}
	}
	
	/**
	 * Convenience short-cut for {@link #openReaderNowOrFail(File)}
	 * with the parameter "locationOfIndex" set to {@link #indexLocation}.
	 */
	public IndexReader openReaderNowOrFail() throws LockObtainFailedException {
		return openReaderNowOrFail(indexLocation);
	}
	
	/**
	 * Tries to open an {@link IndexReader} for the index at the given location.
	 * If the lock could not be obtained, an {@link LockObtainFailedException} is thrown.
	 * If any other IOException is thrown, an {@link IndexException} is thrown.
	 * Other {@link Throwable} are not handled.
	 * 
	 * @throws LockObtainFailedException Most probably, because someone else holds the lock.
	 * @throws IndexException If any {@link IOError} other than {@link LockObtainFailedException} is thrown.
	 */
	public IndexReader openReaderNowOrFail(File locationOfIndex) throws LockObtainFailedException {
		try {
			return LuceneUtils.openIndexReader(locationOfIndex);
		} catch (LockObtainFailedException normalResponse) {
			throw normalResponse;
		} catch (IOException ex) {
			throw new IndexException ("Failed to open the Lucene reader: " + ex.getMessage(), ex);
		}
	}
    
	/**
	 * Convenience shortcut for {@link #createWriterWaitingAndRetrying(boolean)}
	 * with the parameter "createIndex" being set to false.
	 */
	public IndexWriter createWriterWaitingAndRetrying() {
		return createWriterWaitingAndRetrying(false);
	}
    
	/**
	 * Convenience shortcut for {@link #createWriterWaitingAndRetrying(File, boolean)}
	 * with the parameter "location" being set to {@link #indexLocation}.
	 */
	public IndexWriter createWriterWaitingAndRetrying(boolean createIndex) {
		return createWriterWaitingAndRetrying(indexLocation, createIndex);
	}
	
    /**
	 * Test and wait for the index writer to be unlocked by the thread currently holding the lock.
	 * 
	 * If timeout of {@link LuceneConfig#getTimeoutLockRequestWriter()} milliseconds is reached, the
	 * administrator is informed, that this thread is waiting unexpectedly long for the lock.
	 */
	public IndexWriter createWriterWaitingAndRetrying(File location, boolean createIndex) {
		LockWaitMonitor.INSTANCE.requestingLock("Lucene Writer Lock at: " + location,
			config.getTimeoutLockRequestWriter(), config.getWarnTimeoutLockRequestWriter());
		try {
			ExponentialBackoff timeoutGenerator = new ExponentialBackoff(500, Math.sqrt(2), 15 * 1000);
			Maybe<IndexWriter> writer = tryCreateWriterNow(location, createIndex);
			while ( ! writer.hasValue()) {
				LockWaitMonitor.INSTANCE.requestStillWaiting();
				timeoutGenerator.sleep();
				
			    writer = tryCreateWriterNow(location, createIndex);
			}
			return writer.get();
		}
		finally {
			LockWaitMonitor.INSTANCE.requestFinished();
		}
	}
	
	/**
	 * Convenience shortcut for {@link #tryCreateWriterNow(File, boolean)}
	 * with the parameter "location" being set to {@link #indexLocation}.
	 */
	public Maybe<IndexWriter> tryCreateWriterNow(boolean createIndex) {
		return tryCreateWriterNow(indexLocation, createIndex);
	}
	
	/**
	 * Try to create a new {@link IndexWriter} for the given location and with the default shared analyzer.
	 * If the lock could not be obtained (an {@link LockObtainFailedException} is thrown), Maybe.none() is returned.
	 * Other {@link Throwable} are not handled.
	 * 
	 * <b>The {@link Analyzer} is shared so don't close {@link IndexWriter#getAnalyzer() it}.</b>
	 */
	public Maybe<IndexWriter> tryCreateWriterNow(File location, boolean createIndex) {
		try {
			IndexWriter writer = createWriterNowOrFail(location, createIndex);
			if (DEBUG) {
				String message = "Succeeded to create Lucene writer.";
				Logger.debug(message, LuceneIndex.class);
			}
			return Maybe.toMaybe(writer);
		}
		catch (LockObtainFailedException exception) {
			String message = "Failed to obtain Lucene writer lock: " + exception.getMessage();
			Logger.info(message, LuceneIndex.class);
			return Maybe.none();
		}
	}
	
	/**
	 * Convenience shortcut for {@link #createWriterNowOrFail(File, boolean)}
	 * with the parameter "location" being set to {@link #indexLocation}.
	 */
	public IndexWriter createWriterNowOrFail(boolean createIndex) throws LockObtainFailedException {
		return createWriterNowOrFail(indexLocation, createIndex);
	}
	
	/**
	 * Try to create a new {@link IndexWriter} for the given location and with the default shared analyzer.
	 * If the lock could not be obtained, an {@link LockObtainFailedException} is thrown.
	 * If any other IOException is thrown, an {@link IndexException} is thrown.
	 * Other {@link Throwable} are not handled.
	 * 
	 * <b>The {@link Analyzer} is shared so don't close {@link IndexWriter#getAnalyzer() it}.</b>
	 * 
	 * @param location
	 *        the directory to write to
	 * @param createIndex
	 *        whether a new index should be created
	 * @throws LockObtainFailedException Most probably, because someone else holds the lock.
	 * @throws IndexException If any {@link IOError} other than {@link LockObtainFailedException} is thrown.
	 */
	public IndexWriter createWriterNowOrFail(File location, boolean createIndex) throws LockObtainFailedException {
		try {
			Analyzer theAnalyzer = getDefaultDocumentAnalyzer();
			LimitTokenCountAnalyzer limitedAnalyzer = new LimitTokenCountAnalyzer(theAnalyzer, DEFAULT_MAX_FILED_LENGTH_VALUE);
			IndexWriterConfig indexWriterConfig = new IndexWriterConfig(limitedAnalyzer);
			// Version onOrAfter 3.1
			indexWriterConfig.setMergePolicy(new TieredMergePolicy());
			indexWriterConfig.setOpenMode(
				createIndex ? IndexWriterConfig.OpenMode.CREATE : IndexWriterConfig.OpenMode.APPEND);
			return new IndexWriter(LuceneUtils.openDirectory(location.toPath()), indexWriterConfig);
		}
		catch (LockObtainFailedException normalResponse) {
			throw normalResponse;
		}
		catch (IOException exception) {
			throw new IndexException("Failed to create a Lucene writer: " + exception.getMessage(), exception);
		}
	}
    
	/**
     * Get a Lucene index searcher for searching in a Lucene index.
     * 
     * @return The Lucene index searcher
     * @throws SearchException if the index could not be opened
     */
    public IndexSearcher getSearcher() throws SearchException {
    	return new IndexSearcher(openReaderWaitingAndRetrying());
    }
    
    /** 
     * number of outstanding Objects in the Queues.
     */
    public int queueSizes() {
    	if (myThread == null) {
    		return 0;
    	}
		return this.myThread.queueSizes();
    }

	/**
	 * Whether Lucene waits for work.
	 */
	public boolean isIdle() {
		return myThread == null || myThread.isIdle();
	}

    /** 
     * if {@link #myThread} {@link Thread#isAlive()}.
     */
    public boolean isAlive() {
   		return (myThread != null) && myThread.isAlive();
    }

    /** 
     * if {@link #myThread} {@link LuceneThread#isStopping()} or already stopped.
     */
    public boolean isStopping() {
   		return (myThread == null) || myThread.isStopping();
    }

	/**
	 * Mark the given {@link ContentObject} to add to the index.
	 */
    public void addContent(ContentObject content) {
    	if (myThread == null) {
			Logger.error("Lucene is not running. Cannot index content.", LuceneIndex.class);
			return;
    	}
    	this.myThread.add(content);
    }
    
	/**
	 * Mark the given {@link ContentObject} to remove from the index.
	 */
    public void deleteContent(ObjectKey key) {
    	if (myThread == null) {
    		Logger.error("Lucene is not running. Cannot delete content.", LuceneIndex.class);
    		return;
    	}
        this.myThread.delete(key);
    }

    /**
     * Checks if the given document is known in the Lucene index.
     *
     * @param key the key of the knowledge object to be checked
     * @return true if the given document is known in the Lucene index.
     * 
     * @throws IndexException in case of an index error
     */
    public boolean containsContent(ObjectKey key) {
        return this.containsDocument(key);
    }

    private boolean containsDocument(ObjectKey key) {
		boolean result = false;
		try (IndexReader reader = openReaderWaitingAndRetrying()) {
			result = LuceneUtils.containsDocument(key, reader);
		} catch (IOException exception) {
			Logger.error("Failed to close Lucene reader!", exception, LuceneIndex.class);
        }
		return result;
    }
    
    /**
     * Checks (with an heuristic) if a Lucene index exists
     *
     * @return true if the Lucene index seems to exist
     */
    public boolean indexExists() {
        String[] theFiles = this.indexLocation.list();
        return theFiles != null && theFiles.length > 0;
    }
    
	/**
	 * Create the Lucene index. Will delete any old data!
	 * <b>Use with caution.</b>
	 */
	public void createIndex() {
		createIndexLocation();
		createIndexInternal();
		
		if ( ! indexExists()) {
			throw new RuntimeException("Failed to create Lucene index! No error was thrown, but the index appears to not exist. Configured location of index: '" +  indexLocation + "'");
		}
	}
	
	private void createIndexInternal() {
		try {
			// Index will be created during construction of index writer
			createWriterWaitingAndRetrying(true).close();
		}
		catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

    /**
     * Remove all lock files in the Lucene index directory.
     * 
     * @deprecated The locks exist for a reason. Removing them with this method may result in a corrupted, useless index.
     * 
     * @return true, if any lock files where found - may still fail to delete them.
     */
	@Deprecated
    public boolean unlockIndex() {
        // o.k., we only have to delete the ".lock" files in the Lucene index dir.
        File[] theLockFiles = getLockFiles();
        for (File file : theLockFiles) {
            if (! file.delete()) {
                Logger.error("Failed to delete '" + file + "'", this);
            }
        }
        return theLockFiles.length > 0;
    }

    /** 
     * Return all Files in {@link #indexLocation} ending with ".lock".
     */
    protected File[] getLockFiles() {
    	if (!indexLocation.exists()) {
    		return NO_LOCK_FILES;
    	}
        return this.indexLocation.listFiles(LOCK_FILTER);
    }

    /**
     * Check if there are any lock files in the {@link #indexLocation}
     * 
     * @return true there is any file named ".lock"
     */
    public boolean hasLocks() {
        return getLockFiles().length > 0;
    }

	/**
	 * Force a reset of the {@link LuceneIndex}. All indexes will be reset to initial empty state.
	 * <b>Use with caution.</b>
	 * 
	 * @see #createIndex()
	 */
    public void resetIndex() {
		shutDown();
		deleteIndex(); // Cleanup
		createIndex();
		init(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
    }
    
    /**
	 * Deletes the stored index.  <b>Use with caution.</b>
	 */
	public void deleteIndex() {
		if (indexLocation.exists()) {
			FileUtilities.deleteR(indexLocation);
			if (indexLocation.exists()) { 
				throw new RuntimeException("Failed to create Lucene index! Recursive delete failed for: '" +  indexLocation + "'");
			}
		}
	}
	
	private void createIndexLocation() {
		indexLocation.mkdirs();
		if ( ! indexLocation.exists()) { 
			throw new RuntimeException("Failed to create Lucene index! 'mkdirs' failed for: '" +  indexLocation + "'");
		}
	}
	
	/**
	 * Creates the Lucene document analyzer. Sets the configuration for the analyzer depending on
	 * the setting in the the configuration. Stopwords and the German stem exclusion table (if the
	 * {@link GermanAnalyzer} is the default document analyzer) will be set.
	 * 
	 * @return The Lucene document analyzer.
	 * 
	 */
	private Analyzer createAnalyzer(InstantiationContext context) {
		AnalyzerFactory<?> analyzerFactory = context.getInstance(getConfig().getDocumentAnalyzerFactory());
		return analyzerFactory.createAnalzyer(context);

    }

	/**
	 * Returns the {@link Analyzer} that is used to analyze content.
	 * 
	 * <p>
	 * <b>In each method call the same analyzer is returned so don't close it
	 * after usage</b>
	 * </p>
	 * 
	 * @return the configured Analyzer
	 */
	public Analyzer getDefaultDocumentAnalyzer() {
		return this.documentAnalyzer;
	}

    /**
	 * Get the {@link KnowledgeObject} for the given KO ID.
	 * 
	 * @param koID
	 *        The knowledge object ID for which to get the KO
	 * @param docType
	 *        The document type of the KO
	 * @param aKB
	 *        The knowledge base used
	 * @return the knowledge object for the given KO ID
	 */
	public static final KnowledgeObject getKO(TLID koID, String docType, KnowledgeBase aKB) {
        return aKB.getKnowledgeObject(docType, koID);
    }

	/**
	 * Add document to Lucene index. The document's content is given through the
	 * additional parameter 'content'.
	 * 
	 * @param aDoc
	 *        A document to be added to the Lucene index
	 * @param content
	 *        The content of the document
	 * @param description
	 *        The content of the document
	 * @throws IndexException
	 *         in case adding a document failed
	 */
	protected void addDocumentOnly(KnowledgeObject aDoc, String content, String description, IndexWriter writer) {
		if (content == null) {
			Logger.info("Document has no readable content and cannot be added to index: " + aDoc, LuceneIndex.class);
			return;
		}

		try {
			// convert the content of the knowledge object into a lucene
			// document and add it to the document index
			writer.addDocument(LuceneUtils.createLuceneDocument(aDoc, content, description));

		} catch (IOException ioe) {
			String message = "addDocument(KnowledgeObject, String) failed for document '" + aDoc.tId() + "'.";
			throw new IndexException(message, ioe);
		}
	}
	

	/**
	 * Creates a new {@link QueryParser} with the given {@link Analyzer}, the used Lucene version, and the given default field.
	 */
	public QueryParser getQueryParser(String defaultField, Analyzer analyzer) {
		QueryParser parser = new QueryParser(defaultField, analyzer);
		// Version onOrAfter 2.9
		parser.setEnablePositionIncrements(true);
		// Version onOrAfter 3.1.9
		parser.setAutoGeneratePhraseQueries(false);
		parser.setAllowLeadingWildcard(true);
		return parser;
	}

	/**
	 * Creates a new {@link QueryParser} for the given default field and the
	 * default analyzer.
	 * 
	 * <p>
	 * <b>The shared analyzer is used, so don't close
	 * {@link QueryParser#getAnalyzer() it} after usage</b>
	 * </p>
	 * 
	 * @see #getDefaultDocumentAnalyzer()
	 * @see #getQueryParser(String, Analyzer)
	 */
	public QueryParser getQueryParser(String defaultField) {
		return getQueryParser(defaultField, getDefaultDocumentAnalyzer());
	}
	
	/** @see LuceneThread#isIndexUntrustworthy() */
	public boolean isIndexUntrustworthy() {
		return (myThread != null) && myThread.isIndexUntrustworthy();
	}

	/** @see LuceneThread#getFailsInSuccession() */
	public int getFailsInSuccession() {
		if (myThread == null) {
			return 0;
		}
		return myThread.getFailsInSuccession();
	}

	/** @see #timestampShutdownRequest */
	public Maybe<Long> getTimestampShutdownRequest() {
		return timestampShutdownRequest;
	}

	/** @see LuceneConfig#getTimeoutLockRequestReader() */
	public int getTimeoutLockRequestReader() {
		return config.getTimeoutLockRequestReader();
	}

	/** @see LuceneConfig#getTimeoutLockRequestWriter() */
	public int getTimeoutLockRequestWriter() {
		return config.getTimeoutLockRequestWriter();
	}

	/** @see LuceneConfig#getWarnTimeoutLockRequestReader() */
	public int getWarnTimeoutLockRequestReader() {
		return config.getWarnTimeoutLockRequestReader();
	}

	/** @see LuceneConfig#getWarnTimeoutLockRequestWriter() */
	public int getWarnTimeoutLockRequestWriter() {
		return config.getWarnTimeoutLockRequestWriter();
	}

	/** @see LuceneConfig#getTimeoutAbortIndexingForShutdown() */
	public int getTimeoutAbortIndexingForShutdown() {
		return config.getTimeoutAbortIndexingForShutdown();
	}

	/** @see LuceneConfig#getWarnQueueSize() */
	public int getWarnQueueSize() {
		return config.getWarnQueueSize();
	}

	/**
	 * Singleton pattern: static method for getting the {@link LuceneIndex}.
	 *
	 * @return {@link LuceneIndex} the reference to this singleton class
	 */
    public static LuceneIndex getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }

    @Override
	protected void shutDown() {
		Logger.info("Stopping Lucene Indexer", LuceneIndex.class);
		
		timestampShutdownRequest = Maybe.some(System.currentTimeMillis());
		if (myThread != null) {
			try {
				myThread.stopIndexing();
				if ( ! Thread.currentThread().equals(myThread)) {
					myThread.join();
				}
			}
			catch (Exception exception) {
				Logger.error("Stopping Lucene failed: " + exception.getMessage(), exception, this);
			}
			myThread = null;
		}
		
		// Calling Analyzer.close() twice leads to an NPE. And we cannot ask the analyzer if it is already closed.
		// Therefore, we set it to null after closing it.
		if (documentAnalyzer != null) {
			documentAnalyzer.close();
			documentAnalyzer = null;
		}
		timestampShutdownRequest = Maybe.none();
		
		Logger.info("Stopped Lucene Indexer", LuceneIndex.class);
		super.shutDown();
	}
	
	/**
	 * If the index didn't exist and had to be newly created.
	 */
	public boolean wasReindexed() {
		return (_reindexed);
	}

	/**
	 * @see #wasReindexed()
	 */
	public void setReindexed(boolean reindex) {
		_reindexed = reindex;
	}

	/**
	 * {@link TypedRuntimeModule} for access to the {@link LuceneIndex}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class Module extends TypedRuntimeModule<LuceneIndex> {

		/**
		 * Sole module instance.
		 */
		public static final Module INSTANCE = new Module();

		private Module() {
			// singleton
		}

		@Override
		public Class<LuceneIndex> getImplementation() {
			return LuceneIndex.class;
		}

	}
	
}
