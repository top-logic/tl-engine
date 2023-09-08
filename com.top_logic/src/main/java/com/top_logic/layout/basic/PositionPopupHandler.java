/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.awt.Point;

import com.top_logic.layout.FrameScope;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;

/**
 * {@link PopupHandler} opening the popup control at a given {@link Point}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PositionPopupHandler extends AbstractPopupHandler {

	private final Point _position;

	/**
	 * Creates a new {@link PositionPopupHandler}.
	 */
	public PositionPopupHandler(FrameScope targetDocument, Point position) {
		super(targetDocument);
		_position = position;
	}

	/**
	 * @see com.top_logic.layout.basic.PopupHandler#createPopup(com.top_logic.layout.structure.PopupDialogModel)
	 */
	@Override
	public PopupDialogControl createPopup(PopupDialogModel dialogModel) {
		return new PopupDialogControl(_targetDocument, dialogModel, _position);
	}

}

