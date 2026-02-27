/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.photo;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataValue;
import com.top_logic.basic.listener.Listener;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactControl;

/**
 * A React control that displays a captured photo inline.
 *
 * <p>
 * On the client side, this control renders a {@code TLPhotoViewer} React component that fetches the
 * image from the {@code /react-api/data} endpoint and displays it using an {@code <img>} element.
 * </p>
 *
 * <p>
 * The control observes a shared {@link BinaryDataValue} model. When the model data changes (e.g.
 * because a new photo was captured), the viewer UI updates automatically.
 * </p>
 */
public class ReactPhotoViewerControl extends ReactControl implements DataProvider {

	/** State key indicating whether photo data is available. */
	private static final String HAS_PHOTO = "hasPhoto";

	/** State key whose value increments each time photo data is replaced. */
	private static final String DATA_REVISION = "dataRevision";

	private final BinaryDataValue _model;

	private final Listener<BinaryData> _modelListener = this::handleModelChanged;

	private int _dataRevision;

	/**
	 * Creates a new {@link ReactPhotoViewerControl}.
	 *
	 * @param model
	 *        The shared data model to observe.
	 */
	public ReactPhotoViewerControl(BinaryDataValue model) {
		super(null, "TLPhotoViewer");
		_model = model;
		BinaryData data = model.getData();
		putState(HAS_PHOTO, data != null);
		putState(DATA_REVISION, _dataRevision);
		model.addListener(_modelListener);
	}

	@Override
	public BinaryData getDownloadData() {
		return _model.getData();
	}

	@Override
	protected void internalDetach() {
		_model.removeListener(_modelListener);
		super.internalDetach();
	}

	private void handleModelChanged(BinaryData data) {
		_dataRevision++;
		putState(HAS_PHOTO, data != null);
		putState(DATA_REVISION, _dataRevision);
	}

}
