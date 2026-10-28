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
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.SimpleBinaryDataValue;
import com.top_logic.basic.listener.Registration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.audio.ReactAudioRecorderControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * {@link UIElement} that records audio from the microphone of the user via the
 * {@link ReactAudioRecorderControl} ({@code TLAudioRecorder}).
 *
 * <p>
 * The recording is uploaded as {@link BinaryData} and written to the input channel, where an
 * {@link AudioPlayerElement} plays it back and a {@link DownloadElement} offers it for download.
 * </p>
 */
@InApp
public class AudioRecorderElement implements UIElement {

	/**
	 * Configuration for {@link AudioRecorderElement}.
	 */
	@TagName("audio-recorder")
	@TypeIcon("css:bi bi-mic")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(AudioRecorderElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel the recorded {@link BinaryData} is written to.
		 *
		 * <p>
		 * A new recording replaces the value of the channel.
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
	 * Creates a new {@link AudioRecorderElement} from configuration.
	 */
	@CalledByReflection
	public AudioRecorderElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();
		ReactAudioRecorderControl control = new ReactAudioRecorderControl(context, model);
		if (_inputRef != null) {
			Registration binding = BinaryChannelBinding.bind(context.resolveChannel(_inputRef), model);
			control.addCleanupAction(binding::dispose);
		}
		control.setCssClass(_cssClass);
		return control;
	}

}
