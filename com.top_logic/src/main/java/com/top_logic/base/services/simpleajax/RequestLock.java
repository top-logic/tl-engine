/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.services.simpleajax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;

/**
 * Lock for synchronizing concurrent requests from a single browser instance.
 * 
 * <p>
 * This lock differentiates between readers and writers. A reader is an activity
 * that only observes the application state (e.g. generates a page). A writer is
 * an activity that modifies the application state. The lock ensures that only a
 * single writer activity is allowed to proceed. In contrast, multiple readers
 * may enter the lock concurrently and generate contents in parallel.
 * </p>
 * 
 * <p>
 * This lock may not be re-entered. No two calls to a {@link #enterReader(Object)} or
 * {@link #enterWriter(Integer)} method must happen in sequence without an
 * corresponding interleaving call to {@link #exitReader(Integer, Object)} or
 * {@link #exitWriter(Integer)}.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class RequestLock {

	/**
	 * Global configuration options of {@link RequestLock}.
	 */
	public interface Options {

		/**
		 * @see #getMaxWriters()
		 */
		String MAX_WRITERS = "max-writers";

		/**
		 * @see #getMaxWaitingReaders()
		 */
		String MAX_WAITING_READERS = "max-waiting-readers";

		/**
		 * @see #getReorderTimeout()
		 */
		String REORDER_TIMEOUT = "reorder-timeout";

		/**
		 * @see #getWriterWaitingTime()
		 */
		String WRITER_WAITING_TIME = "writer-waiting-time";

		/**
		 * @see #getReaderWaitingTime()
		 */
		String READER_WAITING_TIME = "reader-waiting-time";

		/**
		 * Default value for {@link #getReorderTimeout()}, if nothing is configured.
		 */
		long DEFAULT_REORDER_TIMEOUT = 2500;

		/**
		 * Default value for {@link #getWriterWaitingTime()}, if nothing is configured.
		 */
		long DEFAULT_WRITER_WAITING_TIME = 30000;

		/**
		 * Default value for {@link #getReaderWaitingTime()}, if nothing is configured.
		 */
		long DEFAULT_READER_WAITING_TIME = 15000;

		/**
		 * Default value for {@link #getMaxWriters()}, if nothing is configured.
		 */
		int DEFAULT_MAX_WRITERS = 5;

		/**
		 * Default value for {@link #getMaxWaitingReaders()}, if nothing is configured.
		 */
		int DEFAULT_MAX_WAITING_READERS = 3;

		/**
		 * The number of writers which can try to enter this lock.
		 * 
		 * <p>
		 * Attempting to enter the lock with more writers, will produce a
		 * {@link MaxRequestNumberException}.
		 * </p>
		 */
		@Name(MAX_WRITERS)
		@IntDefault(DEFAULT_MAX_WRITERS)
		int getMaxWriters();

		/**
		 * The number of readers which can wait to read the same resource.
		 * 
		 * <p>
		 * If some reader for a resource <code>x</code> enters this lock, then the given number of
		 * additional readers for the resource <code>x</code> can try to enter the lock. The next
		 * reader for the same resource will produce a {@link MaxRequestNumberException}.
		 * </p>
		 */
		@Name(MAX_WAITING_READERS)
		@IntDefault(DEFAULT_MAX_WAITING_READERS)
		int getMaxWaitingReaders();

		/**
		 * Timeout in milliseconds that is waited, if an AJAX request is received with a not yet
		 * valid sequence number.
		 */
		@Name(REORDER_TIMEOUT)
		@LongDefault(DEFAULT_REORDER_TIMEOUT)
		long getReorderTimeout();

		/**
		 * Timeout in millisecond that a writer is waiting to get the right to pass the lock.
		 */
		@Name(WRITER_WAITING_TIME)
		@LongDefault(DEFAULT_WRITER_WAITING_TIME)
		long getWriterWaitingTime();

		/**
		 * Timeout in millisecond that a reader is waiting to get the right for reading some
		 * resource.
		 */
		@Name(READER_WAITING_TIME)
		@LongDefault(DEFAULT_READER_WAITING_TIME)
		long getReaderWaitingTime();

	}

	private final Options _options;

	/**
	 * The last valid sequence number presented to and accepted by this lock.
	 * 
	 * <p>
	 * Sequence numbers are created by the client in ascending order. The server
	 * uses the sequence number presented in a write request to detect and wait
	 * for delayed requests. A sequence number of a request is accepted by this
	 * lock, if it is {@link #sequenceNumber} plus <code>1</code>, or if the
	 * request timeout (see {@link Options#getReorderTimeout()}) has elapsed and
	 * the current request is the next waiting request (see
	 * {@link #waitingRequests}).
	 * </p>
	 */
	private int sequenceNumber;

    /**
	 * Priority queue of sequence numbers.
	 * 
	 * <p>
	 * Only a write request with the smallest sequence number may proceed. All
	 * arriving write requests put their sequence number into this queue and
	 * only the request that has the first sequence number in the queue can
	 * proceed, if the sequence number is accepted (see {@link #sequenceNumber}).
	 * </p>
	 */
	private PriorityQueue<Integer> waitingRequests = new PriorityQueue<>();
    
    /**
	 * The number of concurrent write requests that have entered this lock.
	 * 
	 * <p>
	 * This lock may either be entered by multiple readers, but by a single
	 * writer (see {@link #writerCnt}). Therefore, either {@link #readerCnt} or
	 * {@link #writerCnt} is zero.
	 * </p>
	 */
    private int readerCnt = 0;
    
    /**
	 * The number of concurrent read requests that have entered this lock.
	 * 
	 * <p>
	 * This lock may only be entered by a single writer, if no reader has
	 * currently entered this lock. Scheduling multiple concurrent write
	 * requests is done through {@link #waitingRequests}.
	 * </p>
	 */
    private int writerCnt = 0;
    
    /**
	 * Disallows entering this lock as a writer to prevent commands based on
	 * server-side state that is inconsistent to its client-side view.
	 */
    private boolean timeout = false;

    /** Set of resources for which currently reader exist */
    private Set<Object> readers = new HashSet<>();
    
    /** This map holds the number of waiting readers for some resource. */
    private Map<Object, Integer> waitingReadersByObject = new HashMap<>();

	/**
	 * Creates a {@link RequestLock}.
	 * 
	 * @param options
	 *        The options for this lock.
	 * @param seed
	 *        The first sequence number expected is the given seed plus one. This can be used to
	 *        minimize the chance for successful CSRF attacks be initializing with a random value.
	 */
	public RequestLock(Options options, int seed) {
		_options = options;
		sequenceNumber = seed;
	}

	/**
	 * The global options of this instance.
	 */
	public Options getOptions() {
		return _options;
	}

    /**
	 * Enter this lock as a writer.
	 * 
	 * <p>
	 * The caller must make sure to call {@link #exitWriter(Integer)} (under any
	 * circumstances) after finishing the work that was guarded by this lock
	 * (even if an exception is thrown somewhere in the code after the call to
	 * {@link #enterWriter(Integer)} returned.
	 * </p>
	 * 
	 * <p>
	 * This method must only be used in the following way:
	 * </p>
	 * 
	 * <pre>
	 * {@link #enterWriter(Integer)}(seq);
	 * try {
	 *     ...
	 * } finally {
	 *     {@link #exitWriter(Integer)}(seq);
	 * }
	 * </pre>
	 * 
	 * @param key
	 *     The sequence number of the request.
	 */
	public synchronized void enterWriter(Integer key) 
		throws InterruptedException, AJAXOutOfSequenceException, RequestTimeoutException, MaxRequestNumberException 
	{
		int newSequenceNumber = key.intValue();
		int expectedSequenceNumber = this.sequenceNumber + 1;
		
		if (newSequenceNumber < expectedSequenceNumber) {
			// This request has arrived to late and was canceled.
			throw new AJAXOutOfSequenceException(
				"Received sequence number " + newSequenceNumber + 
				", but expected was " + expectedSequenceNumber + ".");
			
		}
		
		if (this.timeout) {
			throw new RequestTimeoutException("Thread '" + Thread.currentThread() + "' can not enter lock as writer, since another thread timed out");
		}

		if (writerCnt >= _options.getMaxWriters()) {
			this.timeout = true;
			String message =
				"Number of writers has trespassed " + _options.getMaxWriters() + ". '" + Thread.currentThread()
					+ "' will be ignored.";
			Logger.warn(message, RequestLock.class);
			throw new MaxRequestNumberException(message);
		}
		
		// Enter the lock as a writer.
		writerCnt++;
		try {
			
			// Register the request.
			waitingRequests.add(key);
			
			long writerWaitingTime = _options.getWriterWaitingTime();
			if (readerCnt > 0) {
				Logger.debug("Write request waiting for reader to finish processing, sequence=" + newSequenceNumber + ".", this);
				long startWaitingTime = System.currentTimeMillis();
				long abortTime = startWaitingTime + writerWaitingTime;
				do {
					long endWaitingTime = System.currentTimeMillis();
					if (endWaitingTime > abortTime) {
						String message = "Write request with sequence number '" + key + "' has timed out after " + (endWaitingTime - startWaitingTime) + "ms.";
						Logger.warn(message, RequestLock.class);
						throw new RequestTimeoutException(message);
					}
					wait(writerWaitingTime);
				} while (readerCnt > 0);
				Logger.debug("Write request continued, sequence=" + newSequenceNumber + ".", this);
			}
			
			if (newSequenceNumber > expectedSequenceNumber) {
				Logger.debug("Write request waiting for valid sequence number. sequence=" + newSequenceNumber + ", expected=" + expectedSequenceNumber + ".", this);
				
				waitForValidSequenceNumber:
				while (newSequenceNumber > expectedSequenceNumber) {
					// The sequence number of this request is not yet valid. There
					// is at least one missing request. Await the timeout.
					long delay = _options.getReorderTimeout();
					long startWaitingTime = System.currentTimeMillis();
					long endWaitingTime = -1;
					long theTimeout = startWaitingTime + delay;
				
					waitForTimeout:
					while (true) {
						this.wait(delay);
		
						// Re-fetch the updated current sequence number.
						expectedSequenceNumber = this.sequenceNumber + 1;
					
						if (newSequenceNumber == expectedSequenceNumber) {
							// The current sequence number has become valid, the
							// missing request has arrived and processed. Continue
							// processing.
							break waitForValidSequenceNumber;
						}
					
						// Compute the new delay (in case this thread was awoken before
						// time.
						endWaitingTime = System.currentTimeMillis();
						delay = theTimeout - endWaitingTime;
					
						if (delay <= 0) {
							// The timeout has been reached. The missing request has
							// still not yet arrived.
							break waitForTimeout;
						}
					}
					
					// Check, whether the current request is the timeout master.
					if (key.equals(waitingRequests.peek())) {
						// This request is the timeout master. The timeout has
						// been reached. 
						String message = "Timeout, missing sequence number: " + 
							expectedSequenceNumber + ", current sequence number: " + newSequenceNumber + 
							", dropping all request until reload. Thread '" + Thread.currentThread() + "' waited " + (endWaitingTime - startWaitingTime) + "ms";
						Logger.warn(message, this);
						// The missing request potentially has data relevant for
						// executing the current request. To prevent
						// inconsistencies under all circumstances,
						// this and all following requests must not be executed,
						// because an earlier request is missing. 
						throw new RequestTimeoutException(message);
					} 
					if (this.timeout) {
						throw new RequestTimeoutException("There is another thread which produced a timeout. Thread '" + Thread.currentThread() + "' waited " + (endWaitingTime - startWaitingTime) + "ms");
					}
		
					// There is a request with a smaller sequence number. This request
					// is responsible for enforcing the timeout.
					continue;
				}
				
				Logger.debug("Write request continued, sequence=" + newSequenceNumber, this);
			}
		
			assert newSequenceNumber >= expectedSequenceNumber : 
				"A request that is registered in time does never time out.";
	
			this.sequenceNumber = newSequenceNumber;
			
			// Wait until the current request is the one that should be
			// processed next.
			if (! key.equals(waitingRequests.peek())) {
				long startWaitingTime = System.currentTimeMillis();
				long abortTime = startWaitingTime + writerWaitingTime;
				Logger.debug("Writer waiting for concurrent writer to finish processing, sequence=" + newSequenceNumber + ".", this);
				do {
					long endWaitingTime = System.currentTimeMillis();
					if (endWaitingTime > abortTime) {
						StringBuilder warning = new StringBuilder();
						warning.append("Write request '");
						warning.append(Thread.currentThread());
						warning.append("' with sequence number '");
						warning.append(key);
						warning.append("' waiting to execute has timed out after ");
						warning.append(endWaitingTime - startWaitingTime);
						warning.append("ms. Currently executed write request has key '");
						warning.append(waitingRequests.peek());
						warning.append("'.");
						String message = warning.toString();
						Logger.warn(message, RequestLock.class);
						throw new RequestTimeoutException(message);
					}
					wait(writerWaitingTime);
					if (this.timeout) {
						String message = "Write request '" + Thread.currentThread() + "' with sequence number '" + key + "' waiting to execute has timed out, since another request has timed out before.";
						Logger.warn(message, RequestLock.class);
						throw new RequestTimeoutException(message);
					}
				} while (! key.equals(waitingRequests.peek()));
				Logger.debug("Write request continued, sequence=" + newSequenceNumber + ".", this);
			}
			
		} catch (Throwable ex) {
			// Make sure to leave this lock in a consistent state: Either return
			// normally and have the current key entered in the waiting queue,
			// or exit abnormally and leave this lock untouched.
			writerCnt--;
			notifyAll();
			waitingRequests.remove(key);
			if (ex instanceof RuntimeException) {
				throw (RuntimeException) ex;
			}
			else if (ex instanceof Error) {
				throw (Error) ex;
			}
			else if (ex instanceof InterruptedException) {
				throw (InterruptedException) ex;
			}
			else if (ex instanceof RequestTimeoutException) {
				this.timeout = true;
				throw (RequestTimeoutException) ex;
			}
			else {
				throw new UnreachableAssertion(
					"No other exception can be caught at this point.", ex);
			}
		}
	}
    
	/**
	 * Exit this lock as a writer.
	 * 
	 * <p>
	 * This method must be called by the thread that previously called {@link #enterWriter(Integer)}
	 * with the same argument.
	 * </p>
	 * 
	 * @return Whether a timeout occurred during procession of the writer with the given key.
	 */
	public synchronized boolean exitWriter(Integer key) {
		assert key.equals(waitingRequests.peek());

		waitingRequests.remove(key);
		writerCnt--;

		assert writerCnt >= 0;
		
		notifyAll();

		return timeout;
	}
	
	/**
	 * Enters this lock as a reader.
	 * 
	 * @param resource
     *        Key that identifies the resource that is being read. Each resource can
     *        only be read in a single thread.
     * 
	 * @return A key that must be presented to {@link #exitReader(Integer, Object)} for exiting the
	 *         lock.
	 */
	public synchronized Integer enterReader(Object resource) throws InterruptedException, MaxRequestNumberException, RequestTimeoutException {
		long readerWaitingTime = _options.getReaderWaitingTime();
		boolean concurrentRead = readers.contains(resource);

		if (concurrentRead) {
			Logger.warn("Multiple concurrent reads for the same resource: " + resource, RequestLock.class);

			/*
			 * Since there is already a reader for the resource the number of waiting readers will
			 * be increased.
			 */
			Object object = waitingReadersByObject.get(resource);
			if (object == null) {
				waitingReadersByObject.put(resource, Integer.valueOf(1));
			} else {
				int numberOfWaitingReadersForResource = ((Integer) object).intValue();
				if (numberOfWaitingReadersForResource >= _options.getMaxWaitingReaders()) {
					String message =
						"Number of waiting readers has trespassed " + _options.getMaxWaitingReaders() + ". '"
							+ Thread.currentThread()
							+ "' will be ignored.";
					Logger.warn(message, RequestLock.class);
					throw new MaxRequestNumberException(message);
				} else {
					waitingReadersByObject.put(resource, Integer.valueOf((numberOfWaitingReadersForResource + 1)));
				}
			}

		}
		
		if (concurrentRead || writerCnt > 0) {
			Logger.debug("Read of resource '" + resource + "' (" + Thread.currentThread() + ") waiting.", RequestLock.class);

			long startWaitingTime = System.currentTimeMillis();
			long abortTime = startWaitingTime + readerWaitingTime;
			do {
				if (System.currentTimeMillis() > abortTime) {
					String message =
						"Read of resource '" + resource + "' (" + Thread.currentThread() + ") timed out.";
					Logger.warn(message, RequestLock.class);
					throw new RequestTimeoutException(message);
				}
				wait(readerWaitingTime);
			} while  (readers.contains(resource) || writerCnt > 0);

			Logger.debug("Read of resource '" + resource + "' (" + Thread.currentThread() + ") continued.", RequestLock.class);
		}
		readers.add(resource);

		readerCnt++;

		return Integer.valueOf(this.sequenceNumber);
	}

	/**
	 * Exits this lock as a writer.
	 * 
	 * @param key
	 *            The result received from {@link #enterReader(Object)}.
	 * @param resource
	 *            Same resource identifier as passed to the corresponding
	 *            {@link #enterReader(Object)} call.
	 */
	public synchronized void exitReader(Integer key, Object resource) {
		// The reader key was intended to enable resetting the
		// readerCnt and writerCnt in a call to reset(). This would reactivate a
		// stale lock caused by a blocking thread that has previously entered this
		// lock. Under that circumstances, a lately exiting thread must be
		// detected to prevent corrupting the lock state.

		readerCnt--;
		assert readerCnt >= 0;

		boolean wasPreviouslyEntered = readers.remove(resource);
		assert wasPreviouslyEntered : "Wrong lock object passed.";
		
		/*
		 * Check whether a there is a waiting reader. If there is one then it will do its work and
		 * the number of waiting readers needs to decrease
		 */
		if (waitingReadersByObject.containsKey(resource)) {
			Object waitingReaderNumbers = waitingReadersByObject.get(resource);
			assert waitingReaderNumbers instanceof Integer && ((Integer) waitingReaderNumbers).intValue() > 0;
			int newValue = ((Integer) waitingReaderNumbers).intValue() - 1;
			if (newValue == 0) {
				waitingReadersByObject.remove(resource);
			} else {
				waitingReadersByObject.put(resource, Integer.valueOf(newValue));
			}
		}

		notifyAll();
	}

	/**
	 * Marks sequence numbers that are currently used as invalid and returns the
	 * next valid sequence number.
	 * 
	 * @return The next valid sequence number.
	 */
	public synchronized int reset() {
		// Causes dead-lock while rendering MainLayout, because the thread that
		// renders MainLayout has already entered this lock.
		//
		//		while ((writerCnt > 0) || (readerCnt > 0)) {
		//			wait();
		//		}

		// Mark all (the next ten) sequence numbers as invalid. This ensures
		// that requests that have been generated before a call to reset and that
		// arrive after the call to reset are canceled.
		this.sequenceNumber += 10;
		this.timeout = false;
		notifyAll();

		Logger.debug("Resetting request lock.", this);

		return sequenceNumber + 1;
	}

}
