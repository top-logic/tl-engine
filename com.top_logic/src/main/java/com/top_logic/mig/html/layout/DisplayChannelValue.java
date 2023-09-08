/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.io.IOException;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractConstantControl;
import com.top_logic.layout.channel.ComponentChannel;
import com.top_logic.layout.channel.ComponentChannel.ChannelListener;
import com.top_logic.layout.provider.MetaLabelProvider;

/**
 * {@link AbstractConstantControl} displaying the value of a {@link ComponentChannel}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplayChannelValue extends AbstractConstantControl implements ChannelListener {

	/** The {@link ComponentChannel} to get display value from. */
	private final ComponentChannel _channel;

	private ResKey _key;

	/**
	 * Creates a new {@link DisplayChannelValue}.
	 */
	public DisplayChannelValue(ComponentChannel channel) {
		_channel = channel;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SPAN);
		writeControlAttributes(context, out);
		out.endBeginTag();
		writeChannelValue(context, out);
		out.endTag(SPAN);
	}

	/**
	 * Writes the value of the {@link #getChannel()}.
	 * 
	 * @param context
	 *        Rendering context.
	 * @param out
	 *        {@link TagWriter} to write content to.
	 * 
	 * @see #label(DisplayContext, Object)
	 */
	protected void writeChannelValue(DisplayContext context, TagWriter out) {
		Object channelValue = getChannel().get();
		if (channelValue == null) {
			return;
		}
		out.writeText(label(context, channelValue));
	}

	/**
	 * computes the label of the channel value to render in
	 * {@link #writeChannelValue(DisplayContext, TagWriter)}.
	 * 
	 * @param context
	 *        Rendering context.
	 * @param channelValue
	 *        The value of the {@link #getChannel()}. Not <code>null</code>.
	 * 
	 * @return A non null label for the given channel value.
	 */
	protected String label(DisplayContext context, Object channelValue) {
		if (getKey() != null) {
			return context.getResources().getMessage(getKey(), channelValue);
		}
		return MetaLabelProvider.INSTANCE.getLabel(channelValue);
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		getChannel().addListener(this);
	}

	@Override
	protected void internalDetach() {
		getChannel().removeListener(this);
		super.internalDetach();
	}

	@Override
	public void handleNewValue(ComponentChannel sender, Object oldValue, Object newValue) {
		requestRepaint();
	}

	/**
	 * This method returns the channel.
	 * 
	 * @return Returns the channel.
	 */
	public ComponentChannel getChannel() {
		return _channel;
	}

	/**
	 * An optional resource key to display the channel object.
	 * 
	 * <p>
	 * If not <code>null</code>, the key is filled with the channel value as single argument and
	 * used as translation.
	 * </p>
	 */
	public ResKey getKey() {
		return _key;
	}

	/**
	 * Setter for {@link #getKey()}.
	 */
	public void setKey(ResKey key) {
		_key = key;
	}
}

