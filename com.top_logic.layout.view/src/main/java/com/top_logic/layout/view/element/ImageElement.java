/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.util.ResKey;
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
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that displays a {@link BinaryData} image via the {@link ReactPhotoViewerControl}
 * ({@code TLPhotoViewer}).
 *
 * <p>
 * The image is read from an input channel (e.g. a QR-code PNG that a
 * {@link com.top_logic.layout.view.command.ViewAction} placed on a dialog channel) and kept in sync
 * with that channel: when the channel value changes, the displayed image is updated.
 * </p>
 *
 * <p>
 * Only image data (content type {@code image/...}) is displayed; any other binary value shows
 * nothing, since the browser cannot render it as a picture.
 * </p>
 */
@InApp
public class ImageElement implements UIElement {

	/**
	 * Configuration for {@link ImageElement}.
	 */
	@TagName("image")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		/** Configuration name for {@link #getAlt()}. */
		String ALT = "alt";

		@Override
		@ClassDefault(ImageElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel whose {@link BinaryData} value is displayed as an image.
		 *
		 * <p>
		 * Only image data (content type {@code image/...}) is displayed; any other binary value
		 * shows nothing.
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * What the image shows, for a reader who cannot see it.
		 *
		 * <p>
		 * Worth saying wherever the image carries information rather than decoration - a QR code
		 * enrolling an authenticator, a chart, a scan. Left unset, the image is announced as a
		 * photograph, which is what this element displays by default.
		 * </p>
		 */
		@Name(ALT)
		@Nullable
		ResKey getAlt();
	}

	/** Prefix of the content type of any image. */
	private static final String IMAGE_TYPE_PREFIX = "image/";

	private final ChannelRef _inputRef;

	private final ResKey _alt;

	/**
	 * Creates a new {@link ImageElement} from configuration.
	 */
	@CalledByReflection
	public ImageElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_alt = config.getAlt();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		SimpleBinaryDataValue model = new SimpleBinaryDataValue(image(null));
		ReactPhotoViewerControl control = new ReactPhotoViewerControl(context, model);
		if (_alt != null) {
			control.setAlt(Resources.getInstance().getString(_alt));
		}
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
		return value instanceof BinaryData data && isImage(data) ? data : null;
	}

	/**
	 * Whether the given data is a picture the browser can display.
	 *
	 * @implNote The content type is matched case-insensitively against the {@code image} top-level
	 *           media type, tolerating parameters such as {@code image/svg+xml; charset=utf-8}.
	 */
	private static boolean isImage(BinaryData data) {
		String contentType = data.getContentType();
		if (contentType == null) {
			return false;
		}
		return contentType.regionMatches(true, 0, IMAGE_TYPE_PREFIX, 0, IMAGE_TYPE_PREFIX.length());
	}

}
