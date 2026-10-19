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
 * A change that comes from the channel is never written back to it: a channel value that is no
 * {@link BinaryData}, e.g. the address of a picture, clears the model and stays on the channel.
 * What the control writes reaches the channel once; the channel ignores a value equal to the one
 * it already holds (for {@link BinaryData}, one of the same content).
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
		return new Binding(channel, model).start();
	}

	private static BinaryData binary(Object value) {
		return value instanceof BinaryData ? (BinaryData) value : null;
	}

	/**
	 * State of one binding: whether the model is being updated from the channel, so that the
	 * resulting notification of the model is not written back to the channel.
	 */
	private static final class Binding implements ChannelListener, Listener<BinaryData> {

		private final ViewChannel _channel;

		private final BinaryDataValue _model;

		private boolean _updating;

		Binding(ViewChannel channel, BinaryDataValue model) {
			_channel = channel;
			_model = model;
		}

		Registration start() {
			updateModel(_channel.get());
			Registration channelRegistration = _channel.addListener(this);
			_model.addListener(this);
			return Registration.onDispose(() -> {
				channelRegistration.dispose();
				_model.removeListener(this);
			});
		}

		@Override
		public void handleNewValue(ViewChannel sender, Object oldValue, Object newValue) {
			updateModel(newValue);
		}

		@Override
		public void notify(BinaryData data) {
			if (_updating) {
				return;
			}
			_channel.set(data);
		}

		private void updateModel(Object value) {
			_updating = true;
			try {
				_model.setData(binary(value));
			} finally {
				_updating = false;
			}
		}

	}

}
