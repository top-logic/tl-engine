/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.SimpleBinaryDataValue;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.photo.ReactPhotoCaptureControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * {@link UIElement} that takes a photo with the camera of the user via the
 * {@link ReactPhotoCaptureControl} ({@code TLPhotoCapture}).
 *
 * <p>
 * The photo is uploaded as {@link BinaryData} and written to the input channel, where an
 * {@link ImageElement} shows it and a {@link DownloadElement} offers it for download.
 * </p>
 */
@InApp
public class PhotoCaptureElement implements UIElement {

	/**
	 * Configuration for {@link PhotoCaptureElement}.
	 */
	@TagName("photo-capture")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(PhotoCaptureElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel the captured {@link BinaryData} photo is written to.
		 *
		 * <p>
		 * A new photo replaces the value of the channel.
		 * </p>
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	private final String _cssClass;

	/**
	 * Creates a new {@link PhotoCaptureElement} from configuration.
	 */
	@CalledByReflection
	public PhotoCaptureElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();
		ReactPhotoCaptureControl control = new ReactPhotoCaptureControl(context, model);
		if (_inputRef != null) {
			Registration binding = BinaryChannelBinding.bind(context.resolveChannel(_inputRef), model);
			control.addCleanupAction(binding::dispose);
		}
		control.setCssClass(_cssClass);
		return control;
	}

}
