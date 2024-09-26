/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.lucene;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.basic.util.ExponentialBackoff;
import com.top_logic.basic.util.StopWatch;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.merge.MergeConflictException;

/**
 * Daemon thread for adding/deleting knowledge objects to index machine.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class LuceneThread extends Thread {
    
	private static final boolean DEBUG = Logger.isDebugEnabled(LuceneThread.class);
    
	private final double AFTER_ERROR_START_SLEEP_TIME = 1000; // In milliseconds
	
	private final double AFTER_ERROR_MAX_SLEEP_TIME = 1000 * 60 * 15; // 15 minutes in milliseconds
	
	/** The {@link LuceneIndex} which created and uses this thread */
	private final LuceneIndex luceneIndex;

	/**
	 * The queue containing the documents to be indexed.
	 * <p>
	 * Use only synchronized on monitor {@link #monitor}
	 * </p>
	 */
    private final LinkedList<ContentObject>  addQueue;

    /** The queue containing the documents to be deleted
    * <p>
    * Use only synchronized on monitor {@link #monitor}
    * </p>
    */
    private final LinkedList<ObjectKey> deleteQueue;

	/** Whether this thread is currently waiting for new work and queues are empty. */
	private volatile boolean _idle = false;

    /** Is the thread going to stop its work? */
    private volatile boolean stopping;

	private volatile boolean isIndexUntrustworthy = false;

	private final Object monitor;
	
	private final AtomicInteger _failsInSuccession = new AtomicInteger();

	private ExponentialBackoff sleepTimeGenerator =
		new ExponentialBackoff(AFTER_ERROR_START_SLEEP_TIME, 1.5, AFTER_ERROR_MAX_SLEEP_TIME);

	private final HistoryManager _historyManager;

	/**
	 * @param luceneIndex
	 *        the index which created this thread
	 * @param monitor
	 *        the Object which is used by the {@link LuceneIndex} to synchronize accesses to this
	 *        thread
	 * @param hm
	 *        The {@link HistoryManager} to update session revision to ensure that data of newly
	 *        created items are found.
	 */
    public LuceneThread(LuceneIndex luceneIndex, Object monitor, HistoryManager hm) {
        super("LuceneThread");
		this.luceneIndex = luceneIndex;
		this.monitor = monitor;
		_historyManager = hm;
        this.addQueue    = new LinkedList<>();
        this.deleteQueue = new LinkedList<>();
    }

    @Override
	public void run() {
		Logger.info(Thread.currentThread().getName() + " has started.", LuceneThread.class);
		work();
		handleStop();
		Logger.info(Thread.currentThread().getName() + " has finished.", LuceneThread.class);
	}

	private void work() {
		while ((!isStopping()) && !abortWorkingForShutdown()) {
			waitForWork();

			workSurvivingExceptions();
		}
		do {
			// Make sure the queues are empty:
			workSurvivingExceptions();
		} while (hasLastRunFailed() && !abortWorkingForShutdown());
	}

	private boolean hasLastRunFailed() {
		return getFailsInSuccession() > 0;
	}

	private void succeeded() {
		_failsInSuccession.set(0);
		sleepTimeGenerator = new ExponentialBackoff(AFTER_ERROR_START_SLEEP_TIME, 1.5, AFTER_ERROR_MAX_SLEEP_TIME);
	}

	private void failed(Throwable exception) {
		_failsInSuccession.incrementAndGet();

		String message = "Lucene failed. (" + _failsInSuccession + " times in succession.)"
			+ " It will close its readers & writers, create new ones, and try to continue working."
			+ " But indexing, deleting and queries might fail or produce invalid results from now on."
			+ " Cause: " + exception.getMessage();
		indexIsUntrustworthy(new RuntimeException(message, exception));
		if (exception instanceof ThreadDeath) {
			Logger.error("Lucene indexing Thread was killed by ThreadDeath!", exception, LuceneThread.class);
			throw (ThreadDeath) exception;
		}
		long sleepTime = Math.round(sleepTimeGenerator.next());
		Logger.info(
			"Because of the error that just happend, to avoid spamming the logs with errors, Lucene will sleep for "
				+ StopWatch.toStringMillis(sleepTime) + ".", LuceneThread.class);
		LuceneUtils.internalSleep(sleepTime);
	}

	private void workSurvivingExceptions() {
		try {
			workHandlingReaderWriter();
			succeeded();
		} catch (Throwable exception) {
			failed(exception);
		}
	}

	private void workHandlingReaderWriter() {
		try (IndexWriter writer = luceneIndex.createWriterWaitingAndRetrying()) {
			try (IndexReader reader = luceneIndex.openReaderWaitingAndRetrying()) {
				workInSystemContext(reader, writer);
			} catch (IOException exception) {
				String message = "Failed to close Lucene reader!"
					+ " This is probably an aftereffect of another problem."
					+ " If so, the original problem will be logged after this one.";
				Logger.error(message, exception, LuceneThread.class);
			}
		} catch (IOException exception) {
			String message = "Failed to close Lucene writer!"
				+ " This is probably an aftereffect of another problem."
				+ " If so, the original problem will be logged after this one.";
			Logger.error(message, exception, LuceneThread.class);
		}
	}

	private void workInSystemContext(final IndexReader reader, final IndexWriter writer) {
		ThreadContext.inSystemContext(LuceneThread.class, new Computation<Void>() {

			@Override
			public Void run() {
				workOnQueues(reader, writer);
				return null;
			}

		});
	}

	private void workOnQueues(IndexReader reader, IndexWriter writer) {
		StopWatch deleteStopWatch = StopWatch.createStartedWatch();
		int deleted = deleteQueue(writer, reader);
		if (DEBUG && deleted > 0) {
			Logger.debug("#" + deleted + " objects deleted in " + deleteStopWatch, LuceneThread.class);
		}
		
		StopWatch indexStopWatch = StopWatch.createStartedWatch();
		int indexed = indexQueue(writer, reader);
		if (DEBUG && indexed > 0) {
			Logger.debug("#" + indexed + " objects indexed in " + indexStopWatch, LuceneThread.class);
		}
	}
	
	/**
	 * This method checks whether some work is to do and in case noting is to do
	 * it forces the current thread to wait.
	 */
	private void waitForWork() {
		synchronized (monitor) {
			while (areQueuesEmpty() && ! isStopping()) {
				_idle = true;
				try {
					monitor.wait();
				} catch (InterruptedException wakeupCall) {
					Logger.warn("Thread was interrupted!", wakeupCall, LuceneThread.class);
				}
			}
			_idle = false;
		}
	}

	boolean areQueuesEmpty() {
		return deleteQueue.isEmpty() && addQueue.isEmpty();
	}

	boolean isIdle() {
		return _idle;
	}

    /** 
     * Cleanup when we stop.
     */
    protected void handleStop() {
        int deletes;
        int adds;
        synchronized (monitor) {
            deletes = deleteQueue.size();
            adds    = addQueue.size();
        }
        Logger.info("Finished indexing/deleting with LuceneThread", this);
        if ((deletes > 0) || (adds > 0)) {
            String message = "Lucene has stopped, but there are still '" + deletes
        		+ "' elements to delete and '" + adds + "' elements to index."
        		+ " Therefore: Index is incomplete!";
            indexIsUntrustworthy(new IllegalStateException(message));
        }
    }
    
    /**
     * Stop the indexing service queue.
     */
    public void stopIndexing() {
		stopping = true;
		synchronized (monitor) {
    		monitor.notifyAll();
		}
    }

    /**
     * Is this indexing/delete thread going to stop?
     * 
     * @return true if so
     */ 
    boolean isStopping() {
   		return stopping;
    }
    
    /** 
     * number of outstanding Objects in the Queues.
     */
    public int queueSizes() {
    	synchronized (monitor) {
    		return deleteQueue.size() + addQueue.size();
    	}
    }
    
    /**
     * Add a document (KO) to the indexing queue
     * 
     * @param    anObject    The object to be indexed.
     */
    public void add(ContentObject anObject) {
        if (isStopping()) {
			String message = "Module dependencies are wrong."
				+ " Something actually depends on Lucene but is not configured like that."
				+ " Details: "
				+ " Someone tried to tell Lucene to index an object, but Lucene is already shutting down."
				+ " When Lucene is shutting down, everything (every module) depending on Lucene should already be shut down."
				+ " And only modules depending on Lucene are allowed to tell Lucene to index something."
				+ " Object, that should be indexed: " + anObject;
            IllegalStateException exception = new IllegalStateException(message);
            indexIsUntrustworthy(exception);
			throw exception;
        }
        if (anObject == null) {
        	throw new NullPointerException("Lucene can not index null.");
        }
        
        synchronized (monitor) {
            addQueue.addLast(anObject);
			_idle = false;
            monitor.notifyAll();
        }
    }

	private ObjectKey getKey(ContentObject object) {
		try {
			return object.getKnowledgeObject().tId();
		}
		catch (Throwable exception) {
			throw new ContentRetrievalFailedException("Trying to get the object key failed!", exception);
		}
	}
    
    /**
     * Add an object to the delete queue to be deleted
     * 
     * @param    key    The object to be indexed.
     */
    public void delete(ObjectKey key) {
		if (isStopping()) {
			String message = "Module dependencies are wrong."
				+ " Something actually depends on Lucene but is not configured like that."
				+ " Details: "
				+ " Someone tried to tell Lucene to delete an object from the index, but Lucene is already shutting down."
				+ " When Lucene is shutting down, everything (every module) depending on Lucene should already be shut down."
				+ " And only modules depending on Lucene are allowed to tell Lucene to delete something."
				+ " Key of Object, that should be delete: " + key;
            IllegalStateException exception = new IllegalStateException(message);
            indexIsUntrustworthy(exception);
			throw exception;
		}
		if (key == null) {
			throw new NullPointerException("Lucene can not delete null.");
		}
		
		synchronized (monitor) {
			removeFromAddQueue(key);
			/* Must always add key to delete queue, because "addQueue" is also used for
			 * modifications, i.e. the object may be indexed before. */
			deleteQueue.addLast(key);
			_idle = false;
			monitor.notifyAll();
		}
    }

	private boolean removeFromAddQueue(ObjectKey key) {
		switch(addQueue.size()) {
			case 0:
				break;
			case 1:
				ContentObject first = addQueue.getFirst();
				if (first.getKnowledgeObject().tId().equals(key)) {
					addQueue.clear();
					Logger.info("Object deleted before indexing: '" + key + "'", this);
					return true;
				}
				break;
			default:
				Iterator<ContentObject> addItems = addQueue.iterator();
				do {
					ContentObject next = addItems.next();
					if (next.getKnowledgeObject().tId().equals(key)) {
						addItems.remove();
						Logger.info("Object deleted before indexing: '" + key + "'", this);
						return true;
					}
				} while (addItems.hasNext());

		}
		return false;
	}

    // Private and protected methods

    /**
	 * Index all objects in the two queues.
	 * 
	 * @return The number of newly indexed objects.
	 */
    int indexQueue(IndexWriter writer, IndexReader reader) {
    	StopWatch stopWatch = StopWatch.createStartedWatch();
    	
        ContentObject theObject = removeFirstToAdd();
        if (theObject == null) {
        	// stop indexing
        	return 0;
        }
        int count = 0;
        
        while (theObject != null) {
			if (abortWorkingForShutdown()) {
				Logger.error("Lucene aborted indexing as timeout was reached. The index is therefore incomplete. REBUILD the INDEX!", LuceneThread.class);
				break;
			}
			
            try {
				indexContent(theObject, writer, reader);
                count ++;
            }
            catch (ContentRetrievalFailedException ex) {
				String message = "Indexing of an object failed. It will be skipped. Object: " + theObject;
				Logger.error(message, ex, LuceneThread.class);
            }
            
			if (stopWatch.getElapsedMillis() > LuceneIndex.INDEX_MILLIS) {
				String message = "Indexing of object " + theObject + " took unexpectedly long: " + stopWatch;
				Logger.info(message, this);  
            }
			stopWatch = StopWatch.createStartedWatch();
            theObject = removeFirstToAdd();
        }
        return count;
    }

	/** 
	 * This method returns the next element to add. <code>null</code> indicates to stop adding now.
	 */
	private ContentObject removeFirstToAdd() {
		ContentObject toAdd;
		synchronized (monitor) {
        	if (!deleteQueue.isEmpty()) {
        		return null; // delete has higher priority;
        	}
			if (addQueue.isEmpty()) {
				return null;
			} else {
				do {
					ContentObject addObject = addQueue.removeFirst();
					if (addObject.getKnowledgeObject().isAlive()) {
						toAdd = addObject;
						break;
					}
					/* The object was deleted in between. This may happen, because between the
					 * deletion of the object and the request for deletion (i.e. adding to delete
					 * queue and removing from add queue), some time may went by. */
					if (addQueue.isEmpty()) {
						return null;
					}
				} while (true);
			}
		}
		updateSessionRevision();
		return toAdd;
	}

	private void updateSessionRevision() {
		try {
			_historyManager.updateSessionRevision();
		} catch (MergeConflictException ex) {
			// Must actually not occur, because LuceneThread has no outgoing changes.
			Logger.error("MergeConflict: ", ex, LuceneThread.class);
		}
	}

	private void indexContent(ContentObject content, IndexWriter writer, IndexReader reader) {
	    KnowledgeObject ko = content.getKnowledgeObject();
	    
        ObjectKey key = getKey(content);
		if (LuceneUtils.containsDocument(key, reader)) {
            LuceneUtils.deleteDocument(key, writer);
        }

		if (!ko.isAlive()) {
			// No longer valid, deleted in the meantime.
			return;
		}
        
        luceneIndex.addDocumentOnly(ko, getContent(content), getDescription(content), writer);
	}

	private String getDescription(ContentObject content) {
		try {
			return content.getDescription();
		}
		catch (Throwable exception) {
			throw new ContentRetrievalFailedException("Retrieving the description failed!", exception);
		}
	}

	private String getContent(ContentObject content) {
		try {
			return content.getContent();
		}
		catch (Throwable exception) {
			throw new ContentRetrievalFailedException("Retrieving the content failed!", exception);
		}
	}

    /**
     * Delete all objects from index currently occurring in queue.
     * 
     * @return The number of deletions processed.
     */
    int deleteQueue(IndexWriter writer, IndexReader reader) {
		StopWatch stopWatch = StopWatch.createStartedWatch();
    	
    	ObjectKey key = removeFirstToDelete();
        if (key == null) {
        	// nothing to delete
        	return 0;
        }
        
        int count = 0;
        
        while (key != null) {
        	if (abortWorkingForShutdown()) {
        		Logger.error("Lucene aborted indexing as timeout was reached. The index is therefore incomplete. REBUILD the INDEX!", LuceneThread.class);
        		break;
        	}
        	
            try {
                this.deleteContent(key, writer, reader);
                count ++;
            }
            catch (ContentRetrievalFailedException ex) {
				String message = "Deleting of an object failed. It will be skipped. Object: " + key;
				Logger.error(message, ex, LuceneThread.class);
            }
            
			if (stopWatch.getElapsedMillis() > LuceneIndex.DELETE_MILLIS) {
				String message = "Deleting of object with key " + key + " took unexpectedly long: " + stopWatch;
				Logger.info(message, this);  
            }
			stopWatch = StopWatch.createStartedWatch();
        	key = removeFirstToDelete();
        }
        
        return count;
    }

    /** 
     * This method returns the next element to delete. <code>null</code> indicates that all elements are deleted.
     */
    private ObjectKey removeFirstToDelete() {
		ObjectKey toDelete;
    	synchronized (monitor) {
    		if (deleteQueue.isEmpty()) {
    			return null; // nothing to delete
    		} else {
				toDelete = deleteQueue.removeFirst();
    		}
    	}
		updateSessionRevision();
		return toDelete;
    }
    
    private void deleteContent(ObjectKey key, IndexWriter writer, IndexReader reader) {
        if (!LuceneUtils.containsDocument(key, reader)) {
        	// no document? -> nothing to be done here
            return;
        }
    
        LuceneUtils.deleteDocument(key, writer);
    }

	private void indexIsUntrustworthy(Throwable cause) {
		isIndexUntrustworthy = true;
		
		String message = "Lucene index might be corrrupted."
			+ " Please FIX the PROBLEM and REBUILD the INDEX. Cause: " + cause.getMessage();
		Logger.error(message, cause, LuceneThread.class);
	}

	/**
	 * Has an error occurred that might have corrupted the index?
	 */
	public boolean isIndexUntrustworthy() {
		return isIndexUntrustworthy;
	}
	
	private boolean abortWorkingForShutdown() {
		if ( ! luceneIndex.getTimestampShutdownRequest().hasValue()) {
			return false;
		}
		return System.currentTimeMillis() > luceneIndex.getTimestampShutdownRequest().get() + luceneIndex.getTimeoutAbortIndexingForShutdown();
	}
	
	/**
	 * The number of times Lucene has failed in succession.
	 */
	public int getFailsInSuccession() {
		return _failsInSuccession.get();
	}

}