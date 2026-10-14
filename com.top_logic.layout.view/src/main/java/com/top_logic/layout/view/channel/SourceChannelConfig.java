/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.channel;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;

/**
 * Configuration of a channel fed by a server-side {@link ChannelSource}.
 *
 * <p>
 * A source channel holds a value like a plain value channel - commands and controls may write it -
 * and additionally receives the values its {@link #getSource() source} publishes: the messages of
 * a bus, the state of a queue, the ticks of a timer. The channel is subscribed to the source while
 * the view declaring it is displayed.
 * </p>
 *
 * <pre>
 * &lt;channels&gt;
 *   &lt;source-channel name="status" update-interval="500"&gt;
 *     &lt;source class="my.app.StatusSource"/&gt;
 *   &lt;/source-channel&gt;
 * &lt;/channels&gt;
 * </pre>
 *
 * @implNote The source's {@link ChannelSource#subscribe(com.top_logic.layout.view.ViewContext, ChannelSink)}
 *           is called each time the view is displayed, with a {@link ChannelPublisher} as sink that
 *           delivers the published values into the window displaying the view.
 */
@TagName("source-channel")
public interface SourceChannelConfig extends ChannelConfig {

	/** Configuration name for {@link #getSource()}. */
	String SOURCE = "source";

	/** Configuration name for {@link #getUpdateInterval()}. */
	String UPDATE_INTERVAL = "update-interval";

	@Override
	@ClassDefault(SourceChannelFactory.class)
	Class<? extends ChannelFactory> getImplementationClass();

	/**
	 * The source publishing the values of the channel.
	 *
	 * <p>
	 * The source is created once and serves every view instance declaring the channel.
	 * </p>
	 */
	@Name(SOURCE)
	@Mandatory
	PolymorphicConfiguration<? extends ChannelSource> getSource();

	/**
	 * The shortest time in milliseconds between two values delivered to the channel; zero to
	 * deliver every value.
	 *
	 * <p>
	 * A source publishing faster than this is followed at this pace: a value published while an
	 * earlier one is still held back replaces it, and the last value of a burst is always
	 * delivered.
	 * </p>
	 */
	@Name(UPDATE_INTERVAL)
	@LongDefault(250)
	long getUpdateInterval();

}
