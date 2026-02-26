/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.audio;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactControl;

/**
 * A React control that plays audio data stored on the server.
 *
 * <p>
 * On the client side, this control renders a {@code TLAudioPlayer} React component that fetches
 * audio from the {@code /react-api/data} endpoint and plays it using the browser's {@code Audio}
 * API.
 * </p>
 *
 * <p>
 * The audio data is set via {@link #setAudioData(BinaryData)} and served to the client through the
 * {@link DataProvider} interface.
 * </p>
 */
public class ReactAudioPlayerControl extends ReactControl implements DataProvider {

	/** State key indicating whether audio data is available. */
	private static final String HAS_AUDIO = "hasAudio";

	private BinaryData _audioData;

	/**
	 * Creates a new {@link ReactAudioPlayerControl}.
	 *
	 * @param audioData
	 *        The initial audio data, or {@code null} if no audio is available yet.
	 */
	public ReactAudioPlayerControl(BinaryData audioData) {
		super(null, "TLAudioPlayer");
		_audioData = audioData;
		putState(HAS_AUDIO, audioData != null);
	}

	/**
	 * Sets the audio data to play.
	 *
	 * <p>
	 * If the control is already rendered, a state patch is sent to the client so that the play
	 * button is enabled or disabled accordingly.
	 * </p>
	 *
	 * @param audioData
	 *        The audio data, or {@code null} to clear.
	 */
	public void setAudioData(BinaryData audioData) {
		_audioData = audioData;
		putState(HAS_AUDIO, audioData != null);
	}

	@Override
	public BinaryData getDownloadData() {
		return _audioData;
	}

}
