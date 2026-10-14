/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelConfig;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.channel.ChannelSink;
import com.top_logic.layout.view.channel.ChannelSource;
import com.top_logic.layout.view.channel.SourceChannelConfig;
import com.top_logic.layout.view.channel.SourceChannelFactory;
import com.top_logic.layout.view.channel.SourceViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * Tests for the {@link SourceChannelConfig source channel}: its declaration and the subscription
 * of a {@link SourceViewChannel} to its {@link ChannelSource} while attached.
 */
public class TestSourceChannel extends TestCase {

	/** How long a test waits for a delivery before it counts as hanging. */
	private static final long TIMEOUT = 20_000;

	/** How long a test waits for a value that must not arrive. */
	private static final long QUIET_PERIOD = 500;

	/**
	 * A {@link ChannelSource} recording its subscriptions.
	 */
	public static class TestSource implements ChannelSource {

		/** The sinks subscribed, in subscription order. */
		final List<ChannelSink> _sinks = Collections.synchronizedList(new ArrayList<>());

		/** The number of subscriptions disposed. */
		int _disposed;

		@Override
		public Registration subscribe(ViewContext context, ChannelSink sink) {
			_sinks.add(sink);
			sink.publish("initial");
			return Registration.onDispose(() -> _disposed++);
		}

		/** The sink of the last subscription. */
		ChannelSink lastSink() {
			return _sinks.get(_sinks.size() - 1);
		}

		/** Publishes the given value into the last sink from a thread of its own. */
		void publishFromOtherThread(Object value) throws InterruptedException {
			ChannelSink sink = lastSink();
			Thread thread = new Thread(() -> sink.publish(value));
			thread.start();
			thread.join();
		}
	}

	/**
	 * A {@link ChannelSource} that cannot be subscribed to.
	 */
	public static class FailingSource implements ChannelSource {
		@Override
		public Registration subscribe(ViewContext context, ChannelSink sink) {
			throw new IllegalStateException("Source not available (expected by test).");
		}
	}

	/**
	 * Tests that a {@code <source-channel>} declaration is parsed and creates a
	 * {@link SourceViewChannel}.
	 */
	public void testDeclaration() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestSourceChannel.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		BinaryContent source =
			new ClassRelativeBinaryContent(TestSourceChannel.class, "test-source-channel.view.xml");
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertEquals(1, config.getChannels().size());
		ChannelConfig channelConfig = config.getChannels().get(0);
		assertTrue(channelConfig instanceof SourceChannelConfig);
		assertEquals("x", channelConfig.getName());
		SourceChannelConfig sourceConfig = (SourceChannelConfig) channelConfig;
		assertEquals(0, sourceConfig.getUpdateInterval());
		assertEquals(TestSource.class, sourceConfig.getSource().getImplementationClass());

		ChannelFactory factory = context.getInstance(channelConfig);
		context.checkErrors();
		assertTrue(factory instanceof SourceChannelFactory);
		ViewChannel channel = factory.createChannel(null);
		assertTrue(channel instanceof SourceViewChannel);
		assertFalse(((SourceViewChannel) channel).isSubscribed());
	}

	/**
	 * Tests that the channel subscribes on attach, receives what the source publishes from another
	 * thread, ends the subscription on detach, subscribes anew on the next attach, and ends it for
	 * good on release.
	 */
	public void testSubscriptionLifecycle() throws InterruptedException {
		TestSource source = new TestSource();
		SourceViewChannel channel = new SourceViewChannel("x", null, source, 0);
		assertFalse(channel.isSubscribed());
		assertTrue(source._sinks.isEmpty());

		CountDownLatch initial = awaiting(channel, "initial");
		channel.attach(null);
		assertTrue(channel.isSubscribed());
		assertEquals(1, source._sinks.size());
		await(initial);

		CountDownLatch first = awaiting(channel, "first");
		source.publishFromOtherThread("first");
		await(first);
		assertEquals("first", channel.get());

		// Attaching twice does not subscribe twice.
		channel.attach(null);
		assertEquals(1, source._sinks.size());

		channel.detach();
		assertFalse(channel.isSubscribed());
		assertEquals(1, source._disposed);

		CountDownLatch late = awaiting(channel, "late");
		source.publishFromOtherThread("late");
		assertFalse("No value arrives after detach.", late.await(QUIET_PERIOD, TimeUnit.MILLISECONDS));
		assertEquals("first", channel.get());

		// Detaching twice disposes once.
		channel.detach();
		assertEquals(1, source._disposed);

		CountDownLatch again = awaiting(channel, "initial");
		channel.attach(null);
		assertTrue(channel.isSubscribed());
		assertEquals(2, source._sinks.size());
		await(again);

		channel.release();
		assertFalse(channel.isSubscribed());
		assertEquals(2, source._disposed);
	}

	/**
	 * Tests that a source channel stays writable directly, whether subscribed or not.
	 */
	public void testWritable() throws InterruptedException {
		TestSource source = new TestSource();
		SourceViewChannel channel = new SourceViewChannel("x", null, source, 0);

		assertTrue(channel.set("direct"));
		assertEquals("direct", channel.get());

		CountDownLatch initial = awaiting(channel, "initial");
		channel.attach(null);
		await(initial);

		assertTrue(channel.set("written"));
		assertEquals("written", channel.get());
		channel.release();
	}

	/**
	 * Tests that a source whose subscription fails leaves the channel unsubscribed and usable.
	 */
	public void testFailingSource() {
		SourceViewChannel channel = new SourceViewChannel("x", null, new FailingSource(), 0);

		channel.attach(null);
		assertFalse(channel.isSubscribed());

		assertTrue(channel.set("value"));
		assertEquals("value", channel.get());

		channel.detach();
		channel.release();
		assertFalse(channel.isSubscribed());
	}

	/** A latch counted down once the channel receives the given value. */
	private static CountDownLatch awaiting(ViewChannel channel, Object expected) {
		CountDownLatch result = new CountDownLatch(1);
		channel.addListener((sender, oldValue, newValue) -> {
			if (expected.equals(newValue)) {
				result.countDown();
			}
		});
		return result;
	}

	private static void await(CountDownLatch latch) throws InterruptedException {
		assertTrue("Expected a delivery within " + TIMEOUT + " ms.", latch.await(TIMEOUT, TimeUnit.MILLISECONDS));
	}

	/**
	 * Test suite requiring the {@link TypeIndex} and the {@link SchedulerService} the values are
	 * delivered on.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSourceChannel.class, TypeIndex.Module.INSTANCE,
				SchedulerService.Module.INSTANCE));
	}
}
