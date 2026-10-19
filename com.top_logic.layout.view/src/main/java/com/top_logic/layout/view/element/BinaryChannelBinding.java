/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataValue;
import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * Two-way binding of a {@link ViewChannel} to the {@link BinaryDataValue} a control records,
 * plays, captures or offers for download.
 *
 * <p>
 * The model follows the channel: it takes the channel's {@link BinaryData} value, and is cleared
 * by any other value. The channel follows the model: what the control writes into its model - a
 * recording, a captured photo, a cleared download - is written to the channel.
 * </p>
 *
 * <p>
 * The two directions do not echo each other endlessly: a {@link BinaryDataValue} ignores the
 * identical data it already holds, and a {@link ViewChannel} ignores a value equal to the one it
 * already holds (for {@link BinaryData}, one of the same content).
 * </p>
 *
 * @see AudioRecorderElement
 * @see AudioPlayerElement
 * @see PhotoCaptureElement
 * @see DownloadElement
 */
public final class BinaryChannelBinding {

	private BinaryChannelBinding() {
		// Static utility.
	}

	/**
	 * Binds the given model to the given channel in both directions.
	 *
	 * <p>
	 * The model is set to the current value of the channel right away.
	 * </p>
	 *
	 * @param channel
	 *        The channel holding the data in the view.
	 * @param model
	 *        The model of the control that displays or produces the data.
	 * @return The {@link Registration} that ends the binding in both directions.
	 */
	public static Registration bind(ViewChannel channel, BinaryDataValue model) {
		model.setData(binary(channel.get()));
		ChannelListener channelListener = (sender, oldValue, newValue) -> model.setData(binary(newValue));
		Registration channelRegistration = channel.addListener(channelListener);
		Listener<BinaryData> modelListener = channel::set;
		model.addListener(modelListener);
		return Registration.onDispose(() -> {
			channelRegistration.dispose();
			model.removeListener(modelListener);
		});
	}

	private static BinaryData binary(Object value) {
		return value instanceof BinaryData ? (BinaryData) value : null;
	}

}
