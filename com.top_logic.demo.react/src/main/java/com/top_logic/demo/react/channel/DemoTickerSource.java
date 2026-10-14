/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.react.channel;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.config.constraint.annotation.Constraint;
import com.top_logic.basic.config.constraint.impl.Positive;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelSink;
import com.top_logic.layout.view.channel.ChannelSource;

/**
 * Demo of a {@link ChannelSource}: a simulated server-side feed of temperature telegrams.
 *
 * <p>
 * Each displayed view instance subscribing to it gets a feed of its own, which receives one
 * telegram per period and publishes the state of the feed: the number of telegrams received since
 * the subscription, the time of the last one, and the texts of the last few telegrams. The state
 * before the first telegram is published right on subscription.
 * </p>
 *
 * <pre>
 * &lt;source-channel name="ticker" update-interval="250"&gt;
 *   &lt;source class="com.top_logic.demo.react.channel.DemoTickerSource" period="100"/&gt;
 * &lt;/source-channel&gt;
 * </pre>
 *
 * @implNote The feed is a periodic task on the {@link SchedulerService}; disposing the
 *           subscription cancels it. Each state is a new immutable {@link Map}, so the channel
 *           sees a new value with each telegram: the telegram count under {@link #TICK}, the time
 *           of the last telegram under {@link #AT}, and the texts of the last {@link #RECENT_SIZE}
 *           telegrams, the latest first, under {@link #RECENT}.
 */
public class DemoTickerSource extends AbstractConfiguredInstance<DemoTickerSource.Config>
		implements ChannelSource {

	/** Key of the number of telegrams received since the subscription in a published state. */
	public static final String TICK = "tick";

	/** Key of the time of the last telegram in a published state. */
	public static final String AT = "at";

	/** Key of the texts of the last telegrams, the latest first, in a published state. */
	public static final String RECENT = "recent";

	/** The number of telegram texts a published state holds at most. */
	public static final int RECENT_SIZE = 5;

	/**
	 * Configuration of a {@link DemoTickerSource}.
	 */
	public interface Config extends PolymorphicConfiguration<DemoTickerSource> {

		/** Configuration name for {@link #getPeriod()}. */
		String PERIOD = "period";

		/**
		 * The time in milliseconds between two telegrams.
		 */
		@Name(PERIOD)
		@LongDefault(100)
		@Constraint(Positive.class)
		long getPeriod();

	}

	/**
	 * Creates a {@link DemoTickerSource} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DemoTickerSource(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Registration subscribe(ViewContext context, ChannelSink sink) {
		Feed feed = new Feed(sink);
		feed.publishState();
		long period = getConfig().getPeriod();
		ScheduledFuture<?> task =
			SchedulerService.getInstance().scheduleAtFixedRate(feed, period, period, TimeUnit.MILLISECONDS);
		return Registration.onDispose(() -> task.cancel(false));
	}

	/**
	 * The telegrams of one subscription.
	 *
	 * <p>
	 * Its state is touched by the subscribing thread before the task is scheduled and by the
	 * scheduler afterwards, which runs a periodic task never concurrently with itself.
	 * </p>
	 */
	private static final class Feed implements Runnable {

		private final ChannelSink _sink;

		private final Deque<String> _recent = new ArrayDeque<>();

		private long _tick;

		private Date _at = new Date();

		Feed(ChannelSink sink) {
			_sink = sink;
		}

		@Override
		public void run() {
			try {
				receiveTelegram();
				publishState();
			} catch (RuntimeException ex) {
				Logger.error("Demo ticker failed to publish telegram " + _tick + ".", ex, DemoTickerSource.class);
			}
		}

		private void receiveTelegram() {
			_tick++;
			_at = new Date();
			double temperature = 18 + ThreadLocalRandom.current().nextDouble(8);
			_recent.addFirst(String.format(Locale.ENGLISH, "Telegram %d: %.1f °C", _tick, temperature));
			while (_recent.size() > RECENT_SIZE) {
				_recent.removeLast();
			}
		}

		void publishState() {
			_sink.publish(Map.of(
				TICK, Long.valueOf(_tick),
				AT, _at,
				RECENT, List.copyOf(_recent)));
		}
	}

}
