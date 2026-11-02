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
import com.top_logic.layout.react.control.audio.ReactAudioPlayerControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;

/**
 * {@link UIElement} that plays back {@link BinaryData} audio via the
 * {@link ReactAudioPlayerControl} ({@code TLAudioPlayer}).
 *
 * <p>
 * The audio is read from an input channel and kept in sync with that channel: when the channel
 * value changes, e.g. because an {@link AudioRecorderElement} recorded something new, the player
 * plays the new audio.
 * </p>
 */
@InApp
public class AudioPlayerElement implements UIElement {

	/**
	 * Configuration for {@link AudioPlayerElement}.
	 */
	@TagName("audio-player")
	@TypeIcon("css:bi bi-music-note-beamed")
	public interface Config extends UIElement.Config {

		/** Configuration name for {@link #getInput()}. */
		String INPUT = "input";

		@Override
		@ClassDefault(AudioPlayerElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Channel whose {@link BinaryData} value (an audio recording) is played back.
		 */
		@Name(INPUT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getInput();
	}

	private final ChannelRef _inputRef;

	private final String _cssClass;

	/**
	 * Creates a new {@link AudioPlayerElement} from configuration.
	 */
	@CalledByReflection
	public AudioPlayerElement(InstantiationContext context, Config config) {
		_inputRef = config.getInput();
		_cssClass = config.getCssClass();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		SimpleBinaryDataValue model = new SimpleBinaryDataValue();
		ReactAudioPlayerControl control = new ReactAudioPlayerControl(context, model);
		if (_inputRef != null) {
			Registration binding = BinaryChannelBinding.bind(context.resolveChannel(_inputRef), model);
			control.addCleanupAction(binding::dispose);
		}
		control.setCssClass(_cssClass);
		return control;
	}

}
