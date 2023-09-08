/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.FrameScope;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;

/**
 * Default {@link PopupHandler} encapsulating all information necessary to open a popup dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class DefaultPopupHandler extends AbstractPopupHandler {

	private final String _placementID;

	/**
	 * Creates a {@link DefaultPopupHandler}.
	 * 
	 * @param targetDocument
	 *        The {@link FrameScope} in which the element with the given placement ID lives.
	 * @param placementID
	 *        The client-side identifier of the element relative to which the popup dialog should be
	 *        opened.
	 */
	public DefaultPopupHandler(FrameScope targetDocument, String placementID) {
		super(targetDocument);
		_placementID = placementID;
	}

	@Override
	public PopupDialogControl createPopup(PopupDialogModel dialogModel) {
		return new PopupDialogControl(_targetDocument, dialogModel, _placementID);
	}

}