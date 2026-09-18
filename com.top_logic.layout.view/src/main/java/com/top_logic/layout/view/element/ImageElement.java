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
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.image.ReactImageControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.util.Resources;

/**
 * {@link UIElement} that displays a picture through the {@link ReactImageControl}.
 *
 * <p>
 * The picture is read from an input channel (e.g. a QR-code PNG that a
 * {@link com.top_logic.layout.view.command.ViewAction} placed on a dialog channel) and kept in sync
 * with that channel: when the channel value changes, the displayed picture is updated.
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
		 * Channel whose value is displayed as a picture.
		 *
		 * <p>
		 * The value is either picture data (content type {@code image/...}) or a text naming the
		 * address the picture is loaded from. Any other value - binary data the browser cannot
		 * render as a picture, or an unrelated object - shows nothing.
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();

		/**
		 * What the picture shows, for a reader who cannot see it.
		 *
		 * <p>
		 * Worth saying wherever the picture carries information rather than decoration - a QR code
		 * enrolling an authenticator, a chart, a scan. Left unset, it is announced as a picture and
		 * nothing more.
		 * </p>
		 */
		@Name(ALT)
		@Nullable
		ResKey getAlt();
	}

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
		ReactImageControl control = new ReactImageControl(context);
		if (_alt != null) {
			control.setAlt(Resources.getInstance().getString(_alt));
		}
		if (_inputRef != null) {
			ViewChannel channel = context.resolveChannel(_inputRef);
			control.setValue(channel.get());
			ChannelListener listener = (sender, oldValue, newValue) -> control.setValue(newValue);
			channel.addListener(listener);
			control.addCleanupAction(() -> channel.removeListener(listener));
		}
		return control;
	}

}
