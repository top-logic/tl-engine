/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import static java.util.Objects.*;

import java.util.function.BiFunction;

import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link PopupCommand} that takes a lambda expression as implementation for
 * {@link #showPopup(DisplayContext, PopupHandler)}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LambdaPopupCommand extends PopupCommand {

	private final BiFunction<? super DisplayContext, ? super PopupHandler, ? extends HandlerResult> _showPopupFunction;

	/**
	 * Creates a {@link LambdaPopupCommand}.
	 * 
	 * @param showPopupFunction
	 *        Is not allowed to be null. The implementation for
	 *        {@link #showPopup(DisplayContext, PopupHandler)}.
	 */
	public LambdaPopupCommand(
			BiFunction<? super DisplayContext, ? super PopupHandler, ? extends HandlerResult> showPopupFunction) {
		_showPopupFunction = requireNonNull(showPopupFunction);
	}

	@Override
	public HandlerResult showPopup(DisplayContext context, PopupHandler handler) {
		return _showPopupFunction.apply(context, handler);
	}

}
