/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.SimpleBinaryDataValue;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.photo.ReactPhotoViewerControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;

/**
 * {@link UIElement} that displays a {@link BinaryData} image via the {@link ReactPhotoViewerControl}
 * ({@code TLPhotoViewer}).
 *
 * <p>
 * The image is read from an input channel (e.g. a QR-code PNG that a
 * {@link com.top_logic.layout.view.command.ViewAction} placed on a dialog channel) and kept in sync
 * with that channel: when the channel value changes, the displayed image is updated.
 * </p>
 */
public class ImageElement implements UIElement {

	/**
	 * Configuration for {@link ImageElement}.
	 */
	@TagName("image")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(ImageElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel whose {@link BinaryData} value is displayed as an image.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	/**
	 * Creates a new {@link ImageElement} from configuration.
	 */
	@CalledByReflection
	public ImageElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		SimpleBinaryDataValue model = new SimpleBinaryDataValue(image(null));
		ReactPhotoViewerControl control = new ReactPhotoViewerControl(context, model);
		if (_inputRef != null) {
			ViewChannel channel = context.resolveChannel(_inputRef);
			model.setData(image(channel.get()));
			ChannelListener listener = (sender, oldValue, newValue) -> model.setData(image(newValue));
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
		}
		return control;
	}

	private static BinaryData image(Object value) {
		return value instanceof BinaryData ? (BinaryData) value : null;
	}

}
