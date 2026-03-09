/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.audio;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataValue;
import com.top_logic.basic.listener.Listener;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

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
 * The control observes a shared {@link BinaryDataValue} model. When the model data changes (e.g.
 * because a new recording was made), the player UI updates automatically.
 * </p>
 */
public class ReactAudioPlayerControl extends ReactControl implements DataProvider {

	/** State key indicating whether audio data is available. */
	private static final String HAS_AUDIO = "hasAudio";

	/** State key whose value increments each time audio data is replaced. */
	private static final String DATA_REVISION = "dataRevision";

	private final BinaryDataValue _model;

	private final Listener<BinaryData> _modelListener = this::handleModelChanged;

	private int _dataRevision;

	/**
	 * Creates a new {@link ReactAudioPlayerControl}.
	 *
	 * @param model
	 *        The shared data model to observe.
	 */
	public ReactAudioPlayerControl(ReactContext context, BinaryDataValue model) {
		super(context, null, "TLAudioPlayer");
		_model = model;
		BinaryData data = model.getData();
		putState(HAS_AUDIO, data != null);
		putState(DATA_REVISION, _dataRevision);
		model.addListener(_modelListener);
	}

	@Override
	public BinaryData getDownloadData() {
		return _model.getData();
	}

	@Override
	protected void onCleanup() {
		_model.removeListener(_modelListener);
	}

	private void handleModelChanged(BinaryData data) {
		_dataRevision++;
		putState(HAS_AUDIO, data != null);
		putState(DATA_REVISION, _dataRevision);
	}

}
