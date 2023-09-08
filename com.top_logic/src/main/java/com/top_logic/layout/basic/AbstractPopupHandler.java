/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.FrameScope;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Abstract superclass for {@link PopupHandler}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractPopupHandler implements PopupHandler {

	/** The {@link FrameScope} where the popup is opened. */
	protected final FrameScope _targetDocument;

	/**
	 * Creates a {@link AbstractPopupHandler}.
	 * 
	 * @param targetDocument
	 *        The {@link FrameScope} where the popup is opened.
	 */
	public AbstractPopupHandler(FrameScope targetDocument) {
		_targetDocument = targetDocument;
	}

	@Override
	public HandlerResult openPopup(PopupDialogControl popup) {
		_targetDocument.getWindowScope().openPopupDialog(popup);
		return HandlerResult.DEFAULT_RESULT;
	}
}

