/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.CustomPropertiesDecorator;
import test.com.top_logic.basic.CustomPropertiesSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.io.FileSystemCache;
import com.top_logic.layout.editor.DynamicComponentService;

/**
 * Tests that the {@link DynamicComponentService} applies the file system updates exactly once when
 * definitions are requested from several threads concurrently.
 * 
 * <p>
 * The {@link FileSystemCache} is a {@link CountingFileSystemCache}, which publishes one update per
 * request of a definition.
 * </p>
 */
public class TestDynamicComponentServiceConcurrentUpdates extends TestCase {

	private static final int THREADS = 8;

	private static final int REQUESTS_PER_THREAD = 20_000;

	private static final long JOIN_TIMEOUT = 60_000;

	private static final String UNKNOWN_TEMPLATE = "unknown.template.xml";

	/**
	 * Requests definitions from several threads, while each request publishes a new update.
	 */
	public void testConcurrentRequests() throws InterruptedException {
		FileSystemCache fileSystemCache = FileSystemCache.getCache();
		assertTrue("Test configuration not active: " + fileSystemCache,
			fileSystemCache instanceof CountingFileSystemCache);
		CountingFileSystemCache cache = (CountingFileSystemCache) fileSystemCache;

		DynamicComponentService service = DynamicComponentService.getInstance();
		service.getComponentDefinitions();
		cache.resetCounters();

		Queue<Throwable> problems = new ConcurrentLinkedQueue<>();
		CountDownLatch start = new CountDownLatch(1);
		List<Thread> threads = new ArrayList<>();
		for (int n = 0; n < THREADS; n++) {
			Thread thread = new Thread(() -> {
				try {
					start.await();
					for (int request = 0; request < REQUESTS_PER_THREAD; request++) {
						assertNull(service.getComponentDefinition(UNKNOWN_TEMPLATE));
					}
				} catch (Throwable ex) {
					problems.add(ex);
				}
			}, TestDynamicComponentServiceConcurrentUpdates.class.getSimpleName() + "-" + n);
			threads.add(thread);
			thread.start();
		}
		start.countDown();
		for (Thread thread : threads) {
			thread.join(JOIN_TIMEOUT);
			assertFalse("Thread did not terminate: " + thread.getName(), thread.isAlive());
		}

		if (!problems.isEmpty()) {
			AssertionError error =
				new AssertionError(problems.size() + " requests failed, first: " + problems.peek());
			problems.forEach(error::addSuppressed);
			throw error;
		}
		assertEquals(THREADS * REQUESTS_PER_THREAD, cache.getPublished());
		assertEquals("Each published update must be applied exactly once.", cache.getPublished(),
			cache.getConsumed());
	}

	/**
	 * The test suite with a {@link CountingFileSystemCache} as {@link FileSystemCache}.
	 */
	public static Test suite() {
		Test innerTest = ServiceTestSetup.createSetup(new TestSuite(TestDynamicComponentServiceConcurrentUpdates.class),
			DynamicComponentService.Module.INSTANCE);
		String customFile = CustomPropertiesDecorator.createFileName(TestDynamicComponentServiceConcurrentUpdates.class);
		innerTest = new CustomPropertiesSetup(innerTest, customFile, true);
		return TLTestSetup.createTLTestSetup(innerTest);
	}

}
