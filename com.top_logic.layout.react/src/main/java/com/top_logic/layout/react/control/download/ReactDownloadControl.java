/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.download;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataValue;
import com.top_logic.basic.listener.Listener;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A React control that provides a download button for binary data stored on the server.
 *
 * <p>
 * On the client side, this control renders a {@code TLDownload} React component that displays the
 * file name and a download icon. When clicked, it fetches data from the {@code /react-api/data}
 * endpoint and triggers a browser file download.
 * </p>
 *
 * <p>
 * The control observes a shared {@link BinaryDataValue} model. When the model data changes (e.g.
 * because a file was uploaded or audio was recorded), the download UI updates automatically.
 * </p>
 *
 * <p>
 * If {@link #setClearable(boolean) clearable} is enabled, a clear button is rendered next to the
 * file name, allowing the user to remove the current data by setting the model to {@code null}.
 * </p>
 */
public class ReactDownloadControl extends ReactControl implements DataProvider {

	/** State key indicating whether download data is available. */
	private static final String HAS_DATA = "hasData";

	/** State key whose value increments each time the download data is replaced. */
	private static final String DATA_REVISION = "dataRevision";

	/** State key for the suggested file name. */
	private static final String FILE_NAME = "fileName";

	/** State key indicating whether the clear button is shown. */
	private static final String CLEARABLE = "clearable";

	private final BinaryDataValue _model;

	private final Listener<BinaryData> _modelListener = this::handleModelChanged;

	private int _dataRevision;

	/**
	 * Creates a new {@link ReactDownloadControl}.
	 *
	 * @param model
	 *        The shared data model to observe.
	 */
	public ReactDownloadControl(ReactContext context, BinaryDataValue model) {
		super(context, null, "TLDownload");
		_model = model;
		updateDataState(model.getData());
		setClearable(false);
		model.addListener(_modelListener);
	}

	/**
	 * Enables or disables the clear button on the client.
	 *
	 * @param clearable
	 *        Whether the clear button should be shown.
	 */
	public void setClearable(boolean clearable) {
		putState(CLEARABLE, clearable);
	}

	@Override
	public BinaryData getDownloadData(String key) {
		return _model.getData();
	}

	@Override
	protected void onCleanup() {
		_model.removeListener(_modelListener);
	}

	private void handleModelChanged(BinaryData data) {
		_dataRevision++;
		updateDataState(data);
	}

	private void updateDataState(BinaryData data) {
		putState(HAS_DATA, data != null);
		putState(DATA_REVISION, _dataRevision);
		putState(FILE_NAME, data != null ? data.getName() : null);
	}

	/**
	 * Handles the clear event from the React client, removing the current data.
	 */
	@ReactCommand("clear")
	void handleClear() {
		_model.setData(null);
	}

}
