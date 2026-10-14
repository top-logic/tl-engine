/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.layout.view.channel.ChannelPublisher;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Tests for {@link ChannelPublisher} delivering to a channel of a view that is not displayed.
 */
public class TestChannelPublisher extends TestCase {

	/** How long a test waits for a delivery before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	/** An update interval no test waits for: a value delivered within the timeout was not held back. */
	private static final long LONG_INTERVAL = 60_000;

	private ViewChannel _channel;

	/** Every value the channel received, in the order it received them. */
	private final List<Object> _received = Collections.synchronizedList(new ArrayList<>());

	/** The threads the values were delivered on. */
	private final List<Thread> _threads = Collections.synchronizedList(new ArrayList<>());

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_channel = new DefaultViewChannel("test");
		_channel.addListener((sender, oldValue, newValue) -> {
			_received.add(newValue);
			_threads.add(Thread.currentThread());
		});
	}

	/**
	 * Tests that a published value reaches the channel, and not on the publishing thread.
	 */
	public void testSinglePublish() throws InterruptedException {
		CountDownLatch delivered = awaiting("value");
		ChannelPublisher publisher = new ChannelPublisher(null, _channel, 0);

		publisher.publish("value");

		await(delivered);
		assertEquals("value", _channel.get());
		assertEquals(List.of("value"), _received);
		assertNotSame("Listeners do not run on the publishing thread.", Thread.currentThread(), _threads.get(0));
	}

	/**
	 * Tests that a burst of values within the update interval is conflated, and that its last value
	 * is delivered.
	 */
	public void testBurstIsConflated() throws InterruptedException {
		int last = 20;
		CountDownLatch delivered = awaiting(Integer.valueOf(last));
		ChannelPublisher publisher = new ChannelPublisher(null, _channel, 500);

		for (int n = 0; n <= last; n++) {
			publisher.publish(Integer.valueOf(n));
		}

		await(delivered);
		assertEquals(Integer.valueOf(last), _channel.get());
		List<Object> received = List.copyOf(_received);
		assertTrue("At most the value delivered at once and the last one arrive: " + received,
			received.size() <= 2);
		assertAscending(received);
	}

	/**
	 * Tests that without an update interval every value is delivered, in the order it was published.
	 */
	public void testZeroIntervalDeliversEveryValue() throws InterruptedException {
		int count = 100;
		CountDownLatch delivered = awaiting(Integer.valueOf(count - 1));
		ChannelPublisher publisher = new ChannelPublisher(null, _channel, 0);

		List<Object> expected = new ArrayList<>();
		for (int n = 0; n < count; n++) {
			Integer value = Integer.valueOf(n);
			expected.add(value);
			publisher.publish(value);
		}

		await(delivered);
		assertEquals(expected, _received);
	}

	/**
	 * Tests that nothing is delivered after the publisher was closed, neither a value held back nor
	 * one published afterwards.
	 */
	public void testNoDeliveryAfterClose() throws InterruptedException {
		long interval = 200;
		CountDownLatch delivered = awaiting("first");
		ChannelPublisher publisher = new ChannelPublisher(null, _channel, interval);

		publisher.publish("first");
		await(delivered);
		publisher.publish("held back");
		publisher.close();
		publisher.close();
		publisher.publish("after close");
		publisher.publishNow("urgent after close");

		// The held-back value would have been delivered by now.
		Thread.sleep(interval * 3);
		assertEquals(List.of("first"), _received);
	}

	/**
	 * Tests that a value published with {@link ChannelPublisher#publishNow(Object)} does not wait
	 * for the update interval and replaces a value held back before it, even while an earlier value
	 * is being delivered, so that the channel ends with the value published last.
	 */
	public void testPublishNowSupersedesHeldBackValue() throws InterruptedException {
		CountDownLatch firstApplying = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		CountDownLatch delivered = awaiting("final");
		ChannelPublisher publisher = new ChannelPublisher(null, LONG_INTERVAL, value -> {
			if ("first".equals(value)) {
				firstApplying.countDown();
				awaitQuietly(release);
			}
			_channel.set(value);
		}, false);

		publisher.publish("first");
		await(firstApplying);
		publisher.publish("held back");
		publisher.publishNow("final");
		release.countDown();

		await(delivered);
		assertEquals(List.of("first", "final"), _received);
		assertEquals("final", _channel.get());
	}

	/**
	 * Tests that a value published with {@link ChannelPublisher#publishNow(Object)} right after
	 * a delivery is not held back by the update interval.
	 */
	public void testPublishNowIgnoresInterval() throws InterruptedException {
		CountDownLatch first = awaiting("first");
		CountDownLatch delivered = awaiting("final");
		ChannelPublisher publisher = new ChannelPublisher(null, _channel, LONG_INTERVAL);

		publisher.publish("first");
		await(first);
		publisher.publish("held back");
		publisher.publishNow("final");

		await(delivered);
		assertEquals(List.of("first", "final"), _received);
	}

	/**
	 * Tests that a failure while applying a value does not stop later deliveries.
	 */
	public void testFailingApplyDoesNotStopDelivery() throws InterruptedException {
		CountDownLatch delivered = awaiting("good");
		ChannelPublisher publisher = new ChannelPublisher(null, 0, value -> {
			if ("bad".equals(value)) {
				throw new RuntimeException("Failure expected by the test.");
			}
			_channel.set(value);
		}, false);

		publisher.publish("bad");
		publisher.publish("good");

		await(delivered);
		assertEquals(List.of("good"), _received);
	}

	/** A latch counted down once the channel receives the given value. */
	private CountDownLatch awaiting(Object expected) {
		CountDownLatch result = new CountDownLatch(1);
		_channel.addListener((sender, oldValue, newValue) -> {
			if (expected.equals(newValue)) {
				result.countDown();
			}
		});
		return result;
	}

	private static void await(CountDownLatch latch) throws InterruptedException {
		assertTrue("Expected a delivery within " + TIMEOUT + " ms.", latch.await(TIMEOUT, TimeUnit.MILLISECONDS));
	}

	private static void awaitQuietly(CountDownLatch latch) {
		try {
			latch.await(TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private static void assertAscending(List<Object> values) {
		for (int n = 1; n < values.size(); n++) {
			assertTrue("An older value is never delivered after a newer one: " + values,
				((Integer) values.get(n - 1)).intValue() < ((Integer) values.get(n)).intValue());
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the {@link SchedulerService} the values are
	 * delivered on.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestChannelPublisher.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}
}
