/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletContext;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.InteractionContext;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.StreamIOConverter;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;

/**
 * Test case for {@link StreamIOConverter}.
 */
@SuppressWarnings("javadoc")
public class TestStreamIOConverter extends TestCase {

	private static final long TIMEOUT_SECONDS = 30;

	private static final String CONTENT = "Produced content";

	/**
	 * Starts the producer task only after the calling interaction is already invalid and removed
	 * from its thread, as it happens when the request is answered before the producer thread
	 * starts.
	 */
	public void testProduceAfterInteractionInvalidated() throws Exception {
		ThreadContextManager manager = ThreadContextManager.getManager();
		SubSessionContext subSession = manager.newSubSessionContext();
		ServletContext servletContext = dummyServletContext();

		// Occupy all scheduler threads, so that the producer task stays queued.
		SchedulerService scheduler = SchedulerService.getInstance();
		int poolSize = scheduler.getConfig().getCorePoolSize();
		CountDownLatch blockersRunning = new CountDownLatch(poolSize);
		CountDownLatch releaseBlockers = new CountDownLatch(1);
		try {
			for (int n = 0; n < poolSize; n++) {
				scheduler.execute(() -> {
					blockersRunning.countDown();
					try {
						releaseBlockers.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
					}
				});
			}
			assertTrue("Scheduler threads not occupied.", blockersRunning.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));

			InteractionContext interaction =
				ThreadContextManager.setupInteractionContext(subSession, servletContext, null, null);
			RecordingSource source = new RecordingSource();
			InputStream in;
			try {
				in = StreamIOConverter.convert(source);
			} finally {
				interaction.invalidate();
				manager.removeInteraction();
			}
			assertNull(ThreadContextManager.getInteraction());

			releaseBlockers.countDown();
			source.release();
			checkProducedInContext(in, source, interaction, subSession, servletContext);
		} finally {
			releaseBlockers.countDown();
		}
	}

	private static void checkProducedInContext(InputStream in, RecordingSource source, InteractionContext interaction,
			SubSessionContext subSession, ServletContext servletContext) throws IOException {
		String result = StreamUtilities.readAllFromStream(in, StandardCharsets.UTF_8.name());

		source.assertNoProblem();
		assertEquals(CONTENT, result);
		assertTrue(source._hadInteraction);
		assertNotSame(interaction, source._interaction);
		assertSame(subSession, source._subSession);
		assertSame(servletContext, source._servletContext);
		assertNull(source._request);
		assertNull(source._response);
	}

	/**
	 * Produces data when the caller has no interaction at all.
	 */
	public void testProduceWithoutInteraction() throws Exception {
		assertNull(ThreadContextManager.getInteraction());

		RecordingSource source = new RecordingSource();
		InputStream in = StreamIOConverter.convert(source);
		source.release();
		String result = StreamUtilities.readAllFromStream(in, StandardCharsets.UTF_8.name());

		source.assertNoProblem();
		assertEquals(CONTENT, result);
		assertFalse(source._hadInteraction);
		assertNull(source._subSession);
	}

	private static ServletContext dummyServletContext() {
		return (ServletContext) Proxy.newProxyInstance(TestStreamIOConverter.class.getClassLoader(),
			new Class<?>[] { ServletContext.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "hashCode":
						return System.identityHashCode(proxy);
					case "equals":
						return proxy == args[0];
					case "toString":
						return "DummyServletContext";
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

	/**
	 * {@link BinaryDataSource} waiting for {@link #release()} before recording the context of the
	 * producing thread and writing {@link TestStreamIOConverter#CONTENT}.
	 */
	private static class RecordingSource implements BinaryDataSource {

		private final CountDownLatch _released = new CountDownLatch(1);

		volatile boolean _hadInteraction;

		volatile InteractionContext _interaction;

		volatile SubSessionContext _subSession;

		volatile ServletContext _servletContext;

		volatile Object _request;

		volatile Object _response;

		volatile Throwable _problem;

		void release() {
			_released.countDown();
		}

		void assertNoProblem() {
			if (_problem != null) {
				throw new AssertionError("Producer failed.", _problem);
			}
		}

		@Override
		public String getName() {
			return "test.txt";
		}

		@Override
		public long getSize() {
			return -1;
		}

		@Override
		public String getContentType() {
			return "text/plain";
		}

		@Override
		public void deliverTo(OutputStream out) throws IOException {
			try {
				if (!_released.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
					throw new AssertionError("Producer was not released.");
				}
				InteractionContext interaction = ThreadContextManager.getInteraction();
				_hadInteraction = interaction != null;
				_interaction = interaction;
				_subSession = ThreadContextManager.getSubSession();
				if (interaction != null) {
					_servletContext = interaction.asServletContext();
					_request = interaction.asRequest();
					_response = interaction.asResponse();
				}
				out.write(CONTENT.getBytes(StandardCharsets.UTF_8));
			} catch (Throwable ex) {
				_problem = ex;
				if (ex instanceof IOException) {
					throw (IOException) ex;
				}
				throw new IOException(ex);
			}
		}
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestStreamIOConverter.class,
			SchedulerService.Module.INSTANCE, ThreadContextManager.Module.INSTANCE));
	}
}
