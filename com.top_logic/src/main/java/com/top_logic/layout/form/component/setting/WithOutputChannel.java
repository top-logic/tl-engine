/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component.setting;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.channel.ChannelSPI;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.DefaultChannelSPI;
import com.top_logic.layout.channel.linking.impl.ChannelLinking;
import com.top_logic.layout.component.IComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link LayoutComponent} plug-in interface for supporting an output channel.
 * 
 * <p>
 * Note: When implementing this interface in a {@link LayoutComponent}, one must delegate to
 * {@link #linkChannels(Log)} from the {@link LayoutComponent#linkChannels(Log)} implementation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WithOutputChannel extends IComponent {

	/**
	 * Configuration options for {@link WithOutputChannel}.
	 */
	public interface Config extends IComponent.ComponentConfig {

		/**
		 * {@link WithOutputChannel} channel linking.
		 */
		@Name(OUTPUT_CHANNEL)
		ModelSpec getOutput();

	}

	/**
	 * Name of the corresponding {@link ComponentChannel} delivering the component output.
	 */
	String OUTPUT_CHANNEL = "output";

	/**
	 * Default {@link ChannelSPI} for creating a corresponding {@link ComponentChannel}.
	 */
	DefaultChannelSPI OUTPUT_SPI = new DefaultChannelSPI(OUTPUT_CHANNEL, null);

	/**
	 * The current component's output value.
	 */
	default Object getOutput() {
		return outputChannel().get();
	}

	/**
	 * Sets the component's output.
	 * 
	 * @param newValue
	 *        The output to deliver through the output channel.
	 * 
	 * @see #getOutput()
	 */
	default void setOutput(Object newValue) {
		outputChannel().set(newValue);
	}

	/**
	 * The {@link ComponentChannel} managing the component's output.
	 */
	default ComponentChannel outputChannel() {
		return getChannel(OUTPUT_CHANNEL);
	}

	@Override
	default void linkChannels(Log log) {
		LayoutComponent self = (LayoutComponent) this;
		ChannelLinking channelLinking = self.getChannelLinking(((Config) getConfig()).getOutput());
		outputChannel().linkChannel(log, self(), channelLinking);
	}

}
