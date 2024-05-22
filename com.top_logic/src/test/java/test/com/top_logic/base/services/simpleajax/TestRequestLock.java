/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.basic.ReflectionUtils;
import test.com.top_logic.layout.AbstractLayoutTest;

import com.top_logic.base.services.simpleajax.AJAXOutOfSequenceException;
import com.top_logic.base.services.simpleajax.MaxRequestNumberException;
import com.top_logic.base.services.simpleajax.RequestLock;
import com.top_logic.base.services.simpleajax.RequestLock.Options;
import com.top_logic.base.services.simpleajax.RequestLockFactory;
import com.top_logic.base.services.simpleajax.RequestTimeoutException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.BooleanFlag;
import com.top_logic.basic.util.SystemContextThread;
import com.top_logic.layout.basic.AbstractDisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.basic.DummyDisplayContext;

/**
 * Test the {@link RequestLock}. 
 * 
 * @author     <a href="mailto:kha@top-logic.com">Klaus Halfmann</a>
 */
@SuppressWarnings("javadoc")
public class TestRequestLock extends AbstractLayoutTest {

	private static final int WAIT_TIME = 500;
	private static final long JOIN_TIME = 2000;
    
    protected RequestLock lock;
    
    protected volatile boolean writing;
    protected volatile int reading;
    
    protected List<TestRequest> requestsInOrder; 

    /** 
     * Create a new TestRequestLock.
     */
    public TestRequestLock(String testName) {
        super(testName);
    }
    
    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
	protected void setUp() throws Exception {
        super.setUp();
		lock = RequestLockFactory.getInstance().createLock();
        requestsInOrder = new ArrayList<>();
        reading = 0;
        writing = false;
    }
    
    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
	protected void tearDown() throws Exception {
        lock            = null;
        requestsInOrder = null;
        reading = 0;
        writing = false;
        super.tearDown();
    }
    
    /** Test for the {@link RequestLock#reset()}.  */
    public void testReset() throws Exception {
		Integer key = Integer.valueOf(11);
        assertEquals(key.intValue(), lock.reset());
        lock.enterWriter(key);
        lock.exitWriter(key);
    }

    /**
     * Test some simple enter/exit Sequences.
     */
    public void testEnterExit() throws Exception {
        Object resource = new NamedConstant("resource");
        
        Integer key = lock.enterReader(resource);
                      lock.exitReader(key, resource);
		key = Integer.valueOf(1);
                      lock.enterWriter(key);
                      lock.exitWriter(key);
    }
    
    /**
     * Test a more complex Reader/Writer Sequence.
     */
    public void testReadWrite() throws Exception {
        
		ReadRequest read0 = new ReadRequest("read0");
        WriteRequest write1 = new WriteRequest(1);
		ReadRequest read2 = new ReadRequest("read2");
        
        read0.start();
        write1.start();
        read2.start();
        
        join(read0);
        join(write1);
        join(read2);
        
        // Cannot reliably assert to order of Events
        assertTrue(requestsInOrder.contains(read0));
        assertTrue(requestsInOrder.contains(read2));
        assertTrue(requestsInOrder.contains(write1));
        
        read0 .throwCaught();
        write1.throwCaught();
        read2 .throwCaught();

        assertFalse(hasTimedOut(lock));
    }

    /**
     * Test a Writer out of Sequence.
     */
    public void testOutOfSequence() throws Exception {
        
		ReadRequest read0 = new ReadRequest("read0");
        WriteRequest write1 = new WriteRequest(77777);
		ReadRequest read2 = new ReadRequest("read2");
        
        read0.start();
        write1.start();
        read2.start();
        
        join(read0);
        join(write1);
        join(read2);
        
        // Cannot reliably assert to order of Events
        assertTrue(requestsInOrder.contains(read0));
        assertTrue(requestsInOrder.contains(read2));
        assertFalse("Writer must have failed to enter the lock.", requestsInOrder.contains(write1));
        
        read0.throwCaught();
        assertInstanceof(write1.getProblem(), RequestTimeoutException.class);
        read2 .throwCaught();

        assertTrue(hasTimedOut(lock));
    }

    /**
     * Test writers aligend in sequences
     */
    public void testResequence() throws Exception {
        
        WriteRequest write1 = new WriteRequest(1);
        WriteRequest write2 = new WriteRequest(2);
        WriteRequest write3 = new WriteRequest(3);
        WriteRequest write4 = new WriteRequest(4);
        
        /* trying to assert that the writer enter the lock in the wrong order */
        write4.start();
        write3.start();
        Thread.yield();
        write2.start();
        write1.start();
        
        join(write4);
        join(write3);
        join(write2);
        join(write1);
        
        assertEquals(4, requestsInOrder.size());
        // Events must be in sequence now
        assertSame(write1, requestsInOrder.get(0));
        assertSame(write2, requestsInOrder.get(1));
        assertSame(write3, requestsInOrder.get(2));
        assertSame(write4, requestsInOrder.get(3));
        
        write1.throwCaught();
		write2.throwCaught();
		write3.throwCaught();
		write4.throwCaught();

        assertFalse(hasTimedOut(lock));
    }

    /**
     * Provoke an AJAXOutOfSequenceException.
     */
    public void testOfSequenceException() throws Exception {
        
        WriteRequest write1 = new WriteRequest(1);
        
        write1.start();
        join(write1);
        assertSame(write1, requestsInOrder.get(0));
        write1.throwCaught();
        
        // Reshedule "same" Writer (= reload) 
        write1 = new WriteRequest(1);
        write1.start();
        join(write1);

        assertInstanceof(write1.getProblem(), AJAXOutOfSequenceException.class);

        assertFalse(hasTimedOut(lock));
    }

	public void testConfig() {
		Options conf = lock.getOptions();
        assertEquals(257, conf.getReorderTimeout());
        assertEquals(3000, conf.getReaderWaitingTime());
        assertEquals(3000, conf.getWriterWaitingTime());
    }

    public void testConcurrentReadOfDifferentResources() throws Exception {
    	Object resource1 = new NamedConstant("resource1");
    	Object resource2 = new NamedConstant("resource2");
    	final int[] barrier = new int[1];
    	
		ReadRequest read1 = new ReadRequest("read1", resource1, barrier, 2);
		ReadRequest read2 = new ReadRequest("read2", resource2, barrier, 2);
    	
    	read1.start();
    	read2.start();
    	
		join(read1);
		join(read2);
    	
    	read1.throwCaught();
    	read2.throwCaught();
    }

    public void testConcurrentReadOfSameResource() throws InterruptedException {
    	Object sharedResource = new NamedConstant("sharedResource");

		final int[] barrier = new int[1];
		
		ReadRequest read1 = new ReadRequest("read1", sharedResource, barrier, 2);
		ReadRequest read2 = new ReadRequest("read2", sharedResource, barrier, 2);
		
		read1.start();
		read2.start();
		
		join(read1);
		join(read2);
		
		assertNotNull("Must not concurrently enter lock for reading same resource.", read1.getProblem());
		assertNotNull("Must not concurrently enter lock for reading same resource.", read2.getProblem());
		assertInstanceof(read1.getProblem(), BarrierNotEnteredException.class);
		assertInstanceof(read2.getProblem(), BarrierNotEnteredException.class);
    }

    public void testPreventConcurrentWrite() throws InterruptedException {
    	int[] barrier = new int[1];
    	
		WriteRequest write1 = new WriteRequest(1, barrier, 2);
    	WriteRequest write2 = new WriteRequest(2, barrier, 2);
    	
    	write1.start();
    	write2.start();
    	
    	join(write1);
    	join(write2);
    	
		assertInstanceof(write1.getProblem(), BarrierNotEnteredException.class);
		assertInstanceof(write2.getProblem(), BarrierNotEnteredException.class);
    }
    
    /**
	 * Test a more complex Reader/Writer Sequence.
	 */
	public void testWriteBetweenConcurrentReads() throws Exception {
		class Barrier {
			private int _size;

			private final int[] _handle = new int[1];

			public Barrier(int size) {
				_size = size;
			}

			public int getSize() {
				return _size;
			}

			public int[] handle() {
				return _handle;
			}

			public void enterNoWait() throws BarrierNotEnteredException {
				synchronized (_handle) {
					inc();
				}
			}
			
			public void enter() throws BarrierNotEnteredException, InterruptedException {
				synchronized (_handle) {
					inc();

					if (!await(_size, WAIT_TIME)) {
						Logger.info("Giving up wait.", this);
						_handle[0]--;

						throw new BarrierNotEnteredException("Timeout waiting for other reader");
					}
				}
			}

			private void inc() {
				_handle[0]++;
				_handle.notifyAll();
			}

			public boolean awaitEnter(int enterSize) throws InterruptedException {
				return awaitEnter(enterSize, WAIT_TIME);
			}

			public boolean awaitEnter(int enterSize, long timeout) throws InterruptedException {
				synchronized (_handle) {
					return await(enterSize, timeout);
				}
			}

			private boolean await(int enterSize, long timeout) throws InterruptedException {
				long waitTimeout = System.currentTimeMillis() + timeout;
				while (_handle[0] < enterSize) {
					Logger.info("Waiting for other reader.", this);

					long waitTime = waitTimeout - System.currentTimeMillis();
					if (waitTime <= 0) {
						// Timeout waiting for others.
						return false;
					}

					_handle.wait(waitTime);
				}
				return true;
			}

		}

		Barrier read0Barrier = new Barrier(2);
		Barrier read1Barrier = new Barrier(2);
		Barrier read2Barrier = new Barrier(2);
		Barrier write0Barrier = new Barrier(2);

		ReadRequest read0 = new ReadRequest("read0", "foo", read0Barrier.handle(), read0Barrier.getSize());
		ReadRequest read1 = new ReadRequest("read1", "foo", read1Barrier.handle(), read1Barrier.getSize());
		ReadRequest read2 = new ReadRequest("read2", "foo", read2Barrier.handle(), read2Barrier.getSize());
		WriteRequest write0 = new WriteRequest(1, write0Barrier.handle(), write0Barrier.getSize());

		read1.setPriority(Thread.MIN_PRIORITY);
		read2.setPriority(Thread.MIN_PRIORITY);
		write0.setPriority(Thread.MAX_PRIORITY);

		// Start read.
		read0.start();

		// Expect read0 to enter the lock.
		assertTrue(read0Barrier.awaitEnter(1));

		System.out.println("Read0 entered.");

		// Start concurrent read that is blocked until read0 has completed.
		read1.start();
		read2.start();

		assertFalse(read1Barrier.awaitEnter(1, 50));
		assertFalse(read2Barrier.awaitEnter(1, 50));

		// Start concurrent write.
		write0.start();

		assertFalse(write0Barrier.awaitEnter(1, 50));

		// Let initial reader continue, read1 becomes runnable.
		read0Barrier.enter();

		// Expect that either read1 or write0 enters the lock but not both.
		boolean reader1Entered = read1Barrier.awaitEnter(1, 100);
		boolean reader2Entered = read2Barrier.awaitEnter(1, 100);
		boolean writerEntered = write0Barrier.awaitEnter(1, 100);

		System.out.println("reader1 entered: " + reader1Entered);
		System.out.println("reader2 entered: " + reader2Entered);
		System.out.println("writer0 entered: " + writerEntered);

		assertTrue(reader1Entered || reader2Entered || writerEntered);
		assertFalse(reader1Entered && writerEntered);
		assertFalse(reader2Entered && writerEntered);
		assertFalse(reader1Entered && reader2Entered);

		// Let all others complete.
		read1Barrier.enterNoWait();
		read2Barrier.enterNoWait();
		write0Barrier.enterNoWait();

		join(read0);
		join(read1);
		join(read2);
		join(write0);

		read1.throwCaught();
		read2.throwCaught();
		write0.throwCaught();
	}

    public void testPreventToManyReadRequest() throws InterruptedException {
		Set<TestRequest> requests = new HashSet<>();
		final BooleanFlag flag = new BooleanFlag(true);

		ReadRequest readRequest = new ReadRequest("readRequest", flag) {
			
			@Override
			protected void work() throws InterruptedException {
				synchronized (flag) {
					flag.set(false);
					flag.notifyAll();
					while (!flag.get()) {
						flag.wait();
					}
					super.work();
				}
			}
		};
		synchronized (flag) {
			/*
			 * The following code ensures that the readRequest has passed the lock before the test
			 * readers try to enter the lock.
			 */
			readRequest.start();
			while (flag.get()) {
				flag.wait();
			}
			flag.set(true);
			flag.notifyAll();

			/* starting enough readers to produce error. */
			for (int index = 0; index <= getMaxWaitingReaders(); index++) {
				ReadRequest request = new ReadRequest("request" + index, flag);
				request.start();
				requests.add(request);
			}

			long abortTime = System.currentTimeMillis() + 60000;
			boolean maxRequestNumberReached = false;
			reached: while (true) {
				
				/* Test whether a reader was refused with a MaxRequestNumberException */
				for (Iterator<TestRequest> iter = requests.iterator(); iter.hasNext();) {
					Throwable problem = iter.next().getProblem();
					if (problem != null && !(problem instanceof RequestTimeoutException)) {
						assertInstanceof(problem, MaxRequestNumberException.class);
						maxRequestNumberReached = true;
						break reached;
					}
				}
				Thread.sleep(100);
				if (System.currentTimeMillis() > abortTime) {
					fail("No MaxRequestNumberException occured!");
					break;
				}

			}
			assertTrue(maxRequestNumberReached);
		}
		
		join(readRequest);
		Iterator<TestRequest> iter = requests.iterator();
		while (iter.hasNext()) {
			join(iter.next());
		}
	}
    
	public void testPreventToManyWriteRequest() throws InterruptedException {
		final BooleanFlag flag = new BooleanFlag(true);

		List<TestRequest> requests = new ArrayList<>();
		WriteRequest writeRequest = new WriteRequest(1) {
			@Override
			protected void work() throws InterruptedException {
				synchronized (flag) {
					super.work();
				}
			}
		};
		synchronized (flag) {
			writeRequest.start();
			
			/* starting enough writers to produce errors */
			for (int index = 1; index < getMaxWriters(); index++) {
				WriteRequest writer = new WriteRequest(index + 2);
				writer.start();
				requests.add(writer);
			}
			Thread.sleep(10);
			
			WriteRequest writer = new WriteRequest(2);
			writer.start();
			requests.add(0, writer);

			long abortTime = System.currentTimeMillis() + 60000;

			boolean allWritersDied = false;
			while (!allWritersDied) {
				Thread.sleep(100);
				if (System.currentTimeMillis() > abortTime) {
					fail("Not each request causes an error.");
					break;
				}
				
				allWritersDied = true;
				for (int index = 0, size = requests.size(); index < size; index++) {
					if (requests.get(index).getProblem() == null) {
						allWritersDied = false;
						break;
					}
				}
			}
		}

		join(writeRequest);
		
		/*
		 * there must be exactly one thread died due to a MaxRequestNumberException. All others must
		 * died with a RequestTimeoutException.
		 */
		boolean maxNumberExceptions = false;
		for (int index = 0, size = requests.size(); index < size; index++) {
			Throwable problem = requests.get(index).getProblem();
			if (maxNumberExceptions) {
				assertInstanceof(problem, RequestTimeoutException.class);
			} else {
				if (problem instanceof MaxRequestNumberException) {
					maxNumberExceptions = true;
				} else {
					assertInstanceof(problem, RequestTimeoutException.class);
				}
			}
		}
		assertTrue(maxNumberExceptions);
		assertTrue("Some write request timed out during processing of request.", writeRequest._timeout);
	}

	public void testAllowDefinedMaxNumberOfWriters() throws InterruptedException {
		List<TestRequest> requests = startMaximumAllowedParallelRequest();
		waitForEndOfAllRequests(requests);
		assertNoProblemsOccured(requests);
	}

	private List<TestRequest> startMaximumAllowedParallelRequest() throws InterruptedException {
		List<TestRequest> requests = new ArrayList<>();

		WriteRequest writer = new WriteRequest(1);
		writer.start();
		requests.add(writer);

		// Give first write request the chance to really enter request lock at first.
		Thread.sleep(10);

		for (int index = 2; index <= getMaxWriters(); index++) {
			writer = new WriteRequest(index);
			writer.start();
			requests.add(writer);
		}
		return requests;
	}

	private void waitForEndOfAllRequests(List<TestRequest> requests) throws InterruptedException {
		long abortTime = System.currentTimeMillis() + 60000;
		boolean allWritersDied = false;
		while (!allWritersDied) {
			Thread.sleep(100);
			if (System.currentTimeMillis() > abortTime) {
				fail("Not each requests terminated.");
				break;
			}

			allWritersDied = true;
			for (int index = 0, size = requests.size(); index < size; index++) {
				if (requests.get(index).isAlive()) {
					allWritersDied = false;
					break;
				}
			}
		}
	}

	private void assertNoProblemsOccured(List<TestRequest> requests) {
		for (int index = 0, size = requests.size(); index < size; index++) {
			Throwable problem = requests.get(index).getProblem();
			if (problem != null) {
				fail("An problem occured, executing valid parallel amount of writer's request: " + problem.getMessage());
			}
		}
	}

	public void testPreventToLongWaiting() throws InterruptedException {
		final BooleanFlag flag = new BooleanFlag(true);
		int nextSequenceNumber = 1;

		/* many WriteRequest waiting to long */
		WriteRequest writeRequest = new WriteRequest(nextSequenceNumber) {
			@Override
			protected void work() throws InterruptedException {
				synchronized (flag) {
					super.work();
				}
			}
		};
		WriteRequest errorWriter = new WriteRequest(2);
		WriteRequest errorWriter2 = new WriteRequest(3);

		synchronized (flag) {
			/*
			 * No need to manually ensure that the request arrive in the correct order since this
			 * happens in RequestLock using the sequenceNumbers
			 */
			writeRequest.start();
			errorWriter.start();
			errorWriter2.start();
			Thread.sleep(getWriterWaitingTime() * 3);
		}
		join(writeRequest);
		join(errorWriter);
		join(errorWriter2);

		assertInstanceof(errorWriter.getProblem(), RequestTimeoutException.class);
		assertInstanceof(errorWriter2.getProblem(), RequestTimeoutException.class);
		assertTrue(writeRequest._timeout);
		
		nextSequenceNumber = lock.reset();

		/* many ReadRequests waiting to long */
		ReadRequest readRequest = new ReadRequest("readRequest", flag) {
			@Override
			protected void work() throws InterruptedException {
				synchronized (flag) {
					flag.set(false);
					flag.notifyAll();
					while (!flag.get()) {
						flag.wait();
					}
					super.work();
				}
			}
		};
		ReadRequest readRequest2 = new ReadRequest("readRequest2", flag);
		ReadRequest readRequest3 = new ReadRequest("readRequest3", flag);
		synchronized (flag) {
			/* ensure that the readRequest enters lock before */
			readRequest.start();
			while (flag.get()) {
				flag.wait();
			}
			flag.set(true);

			readRequest2.start();
			readRequest3.start();
			flag.wait(getReaderWaitingTime() * 3);
			flag.notifyAll();
		}
		join(readRequest);
		join(readRequest2);
		join(readRequest3);

		assertInstanceof(readRequest2.getProblem(), RequestTimeoutException.class);
		assertInstanceof(readRequest3.getProblem(), RequestTimeoutException.class);

		nextSequenceNumber = lock.reset();

		/* One reader waits to long for an writer */
		writeRequest = new WriteRequest(nextSequenceNumber) {
			@Override
			protected void work() throws InterruptedException {
				synchronized (flag) {
					flag.set(false);
					flag.notifyAll();
					while (!flag.get()) {
						flag.wait();
					}
					super.work();
				}
			}
		};
		readRequest = new ReadRequest("readRequest", flag);
		synchronized (flag) {
			/* ensure that the writer enters lock before */
			writeRequest.start();
			while (flag.get()) {
				flag.wait();
			}
			flag.set(true);
			readRequest.start();
			flag.wait(getReaderWaitingTime() * 3);
			flag.notifyAll();
		}
		assertInstanceof(readRequest.getProblem(), RequestTimeoutException.class);
		join(readRequest);
		join(writeRequest);
	}

	/**
	 * This method tests the failure described in Ticket #641 Comment:9
	 * <p>
	 * Assuming a reader enters the lock and before leaving the lock
	 * {@link com.top_logic.base.services.simpleajax.RequestLock.Options#MAX_WRITERS} many writer
	 * enter the lock. Then they all wait for the reader and the writer counter is set to
	 * {@link com.top_logic.base.services.simpleajax.RequestLock.Options#MAX_WRITERS} if they all
	 * timed out the counter is not reset and when the reader has left the lock no other writer can
	 * enter the lock; it dies with a {@link MaxRequestNumberException}.
	 * 
	 * </p>
	 */
	public void testFreeLockAfterTimeOut() throws InterruptedException {
		final BooleanFlag flag = new BooleanFlag(true);

		/* the reader to force the writer to time out */
		ReadRequest reader = new ReadRequest("reader") {
			@Override
			protected void work() throws InterruptedException {
				synchronized (flag) {
					flag.set(false);
					flag.notifyAll();
					while (!flag.get()) {
						flag.wait();
					}
					super.work();
				}
			}
		};

		synchronized (flag) {
			reader.start();
			while (flag.get()) {
				flag.wait();
			}
			flag.set(true);
			flag.notifyAll();
			List<Thread> requests = new ArrayList<>();
			for (int index = 0; index < getMaxWriters(); index++) {
				WriteRequest writer = new WriteRequest(index + 2);
				writer.start();
				requests.add(writer);
			}
			for (int index = 0; index < getMaxWriters(); index++) {
				Thread thread = requests.get(index);
				thread.join(10000);
				assertFalse("TestRequest: '" + thread.getName() + "' is still alive!", thread.isAlive());
			}
		}
		join(reader);

		int nextSequenceNumber = lock.reset();
		WriteRequest writer = new WriteRequest(nextSequenceNumber);
		writer.start();
		join(writer);
		assertTrue("Writer has Problem '" + writer.getProblem() + "'", writer.getProblem() == null);
	}
	
	/**
	 * This method first enforces a {@link RequestTimeoutException} and the a {@link WriteRequest} enter
	 * the lock without reset. The {@link WriteRequest} is expected also to die due to a
	 * {@link RequestTimeoutException}.
	 */
	public void testTryingEnterLockAfterTimeout() throws InterruptedException {
		final BooleanFlag flag = new BooleanFlag(true);

		/* the reader to force the writer to time out */
		ReadRequest reader = new ReadRequest("reader") {
			@Override
			protected void work() throws InterruptedException {
				synchronized (flag) {
					flag.set(false);
					flag.notifyAll();
					while (!flag.get()) {
						flag.wait();
					}
					super.work();
				}
			}
		};

		synchronized (flag) {
			reader.start();
			while (flag.get()) {
				flag.wait();
			}
			flag.set(true);

			/* Writer to force timeout */
			WriteRequest writer = new WriteRequest(1);
			writer.start();
			flag.wait(getWriterWaitingTime() * 3);
			assertFalse(writer.isAlive());
			assertInstanceof(writer.getProblem(), RequestTimeoutException.class);
			assertTrue(hasTimedOut(lock));

			writer = new WriteRequest(2);
			writer.start();
			join(writer);
			assertInstanceof(writer.getProblem(), RequestTimeoutException.class);

			flag.notifyAll();
		}
		join(reader);
	}
	
	private static void join(Thread t) throws InterruptedException {
		t.join(JOIN_TIME);
		assertFalse("Thread '" + t + "' did not died after " + JOIN_TIME + "ms.", t.isAlive());
	}
	
	private static boolean hasTimedOut(RequestLock lock) {
		final Object timeout = ReflectionUtils.getValue(lock, "timeout");
		assertInstanceof(timeout, Boolean.class);
		return ((Boolean) timeout).booleanValue();
	}

	class BarrierNotEnteredException extends RuntimeException {
		public BarrierNotEnteredException(String message) {
			super(message);
		}
	}

	
    protected abstract class TestRequest extends SystemContextThread {
        
		private final int[] barrier;
		private final int barrierSize;
		
		protected Integer key;
		protected Throwable caught;

		protected TestRequest(String aName) {
			this(aName, null, -1);
		}
		
		protected TestRequest(String aName, int[] barrier, int barrierSize) {
			super(aName);
			this.barrier = barrier;
			this.barrierSize = barrierSize;
			
			this.setDaemon(true);
		}

		@Override
		protected final void internalRun() {
			final AbstractDisplayContext displayContext = DummyDisplayContext.newInstance();
			DefaultDisplayContext.setupDisplayContext(null, displayContext);
			try {
				internalRunWithDisplayContext();
			} finally {
				DefaultDisplayContext.teardownDisplayContext(null);
			}
		}
		
		protected abstract void internalRunWithDisplayContext();

		protected void work() throws InterruptedException {
			if (barrier != null) {
				synchronized (barrier) {
					barrier[0]++;
					barrier.notifyAll();

					long waitTimeout = System.currentTimeMillis() + WAIT_TIME;
					while (true) {
						if (barrier[0] == barrierSize) {
							// All expected others have entered the barrier.
							break;
						}

						Logger.info("Waiting for other reader.", this);
						long waitTime = waitTimeout - System.currentTimeMillis();
						if (waitTime <= 0) {
							// Timeout waiting for others.
							barrier[0]--;

							Logger.info("Giving up wait.", this);
							throw new BarrierNotEnteredException("Timeout waiting for other reader");
						}

						barrier.wait(waitTime);
					}
				}
			}
		}

        /**
         * (Re-)Throw caughr execption if any was caught.
         */
        public void throwCaught() throws Exception {
            if (caught != null) {
				throw new RuntimeException("Problem in '" + getName() + "': ", caught);
            }
        }
        
		public Throwable getProblem() {
			return this.caught;
		}
    
    }
    
    /**
     * Simulate a ReadRequest against the RequestLock.
     */
    protected class ReadRequest extends TestRequest {

        private final Object resource;
        
		public ReadRequest(String name) {
			this(name, new NamedConstant("resource"));
        }

		public ReadRequest(String name, Object resource) {
			this(name, resource, null, -1);
        }
        
		public ReadRequest(String name, Object resource, int[] barrier, int barrierSize) {
			super("TestRequestLock.ReadRequest(" + name + ")", barrier, barrierSize);
            this.resource = resource;
        }
        
        /**
         * Run via enter/ecitReader().
         */
        @Override
		protected void internalRunWithDisplayContext() {
            try {
                key = lock.enterReader(resource);
                try {
                	incReading();
                	try {
                		assertFalse(writing);
                		
                		work();
                		
                		assertFalse(writing);
                	} finally {
                		decReading();
                	}
                } finally {
                	lock.exitReader(key, resource);
                }
            }
            catch (Exception ex) {
                caught = ex;
            }
            catch (Error ex) {
                caught = ex;
            }
        }

		@Override
		protected void work() throws InterruptedException {
			super.work();
			
			sleep(100); // simulate work ;-)
			
			// Readers must synchronize the shared resource, because
			// they are potentially concurrent.
			synchronized (requestsInOrder) {
				requestsInOrder.add(this);
			}
		}
    }

    /**
     * Simulate a WriteRequest against the RequestLock.
     */
    protected class WriteRequest extends TestRequest {

		protected boolean _timeout;
        
    	public WriteRequest(int key) {
    		this(key, null, -1);
    	}
    	
        public WriteRequest(int key, int[] barrier, int barrierSize) {
            super("TestRequestLock.WriteRequest#" + key, barrier, barrierSize);
			this.key = Integer.valueOf(key);
        }
        
        /**
         * Run via enter/exitWriter().
         */
        @Override
        protected void internalRunWithDisplayContext() {
            try {
                lock.enterWriter(key);
                try {
                	assertEquals(0, reading);
                	assertFalse(writing);
                	
                	writing = true;
                	try {
                		work();
                	} finally {
                		writing = false;
                	}
                	
                	assertEquals(0, reading);
                } finally {
					_timeout = lock.exitWriter(key);
                }
            }
            catch (Exception ex) {
                caught = ex;
            }
        }

		@Override
		protected void work() throws InterruptedException {
			super.work();

			sleep(100); // simulate work ;-)
			
			// Write requests are guaranteed to be single-threaded.
			// Therefore no synchronization on shared resource.
			requestsInOrder.add(this);
		}

        
    }

    synchronized void incReading() {
    	reading++;
    }
    
    synchronized void decReading() {
    	reading--;
    }
    
	private int getMaxWriters() {
		return lock.getOptions().getMaxWriters();
	}

	private int getMaxWaitingReaders() {
		return lock.getOptions().getMaxWaitingReaders();
	}

	private long getWriterWaitingTime() {
		return lock.getOptions().getWriterWaitingTime();
	}

	private long getReaderWaitingTime() {
		return lock.getOptions().getReaderWaitingTime();
	}

	/**
	 * Return the suite of Tests to execute.
	 */
    public static Test suite() {
		return suite(TestRequestLock.class);
    }

    /** main function for direct execution.
     */
    public static void main (String[] args) {

        Logger.configureStdout(); // "DEBUG" 
        TestRunner.run(suite());
    }
    
}
