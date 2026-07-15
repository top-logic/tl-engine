/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.pdf;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataValue;
import com.top_logic.basic.listener.Listener;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A React control that displays a PDF document inline.
 *
 * <p>
 * On the client side, this control renders a {@code TLPdfViewer} React component that fetches the
 * PDF from the {@code /react-api/data} endpoint and displays it in an {@code <iframe>} using the
 * browser's native PDF viewer.
 * </p>
 *
 * <p>
 * The control observes a shared {@link BinaryDataValue} model. When the model data changes, the
 * viewer UI updates automatically. This control is the rendering target for any feature that can
 * produce a PDF {@link BinaryData}; an Office-document viewer, for example, can convert
 * {@code .docx}/{@code .xlsx}/{@code .pptx} to PDF server-side and feed the result here.
 * </p>
 */
public class ReactPdfViewerControl extends ReactControl implements DataProvider {

	/** State key indicating whether PDF data is available. */
	private static final String HAS_PDF = "hasPdf";

	/** State key whose value increments each time the PDF data is replaced. */
	private static final String DATA_REVISION = "dataRevision";

	private final BinaryDataValue _model;

	private final Listener<BinaryData> _modelListener = this::handleModelChanged;

	private int _dataRevision;

	/**
	 * Creates a new {@link ReactPdfViewerControl}.
	 *
	 * @param model
	 *        The shared data model holding the PDF to display.
	 */
	public ReactPdfViewerControl(ReactContext context, BinaryDataValue model) {
		super(context, null, "TLPdfViewer");
		_model = model;
		BinaryData data = model.getData();
		putState(HAS_PDF, data != null);
		putState(DATA_REVISION, _dataRevision);
		model.addListener(_modelListener);
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
		putState(HAS_PDF, data != null);
		putState(DATA_REVISION, _dataRevision);
	}

}
