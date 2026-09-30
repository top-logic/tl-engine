/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.navigation;

import java.util.List;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.navigation.ObjectNavigator;
import com.top_logic.layout.view.ViewMessages;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLType;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ObjectNavigator} leading to the places the application's {@link DisplayTargetService
 * display targets} declare.
 *
 * <p>
 * An object can be shown exactly when a target is declared for its type, so a control offers a
 * value as a link by the same rule that decides where following the link leads.
 * </p>
 */
public final class DisplayTargetNavigator implements ObjectNavigator {

	/** Singleton {@link DisplayTargetNavigator} instance. */
	public static final DisplayTargetNavigator INSTANCE = new DisplayTargetNavigator();

	/**
	 * The display, as the step of a chain: it hands over the {@link ViewAction}'s continuation, on
	 * which the display suspends while the user is asked about unsaved changes on the way.
	 */
	private static final ViewAction SHOW_OBJECT = new ShowObjectAction();

	private DisplayTargetNavigator() {
		// Singleton constructor.
	}

	@Override
	public boolean canShow(Object value) {
		if (!(value instanceof TLObject object)) {
			return false;
		}
		if (!DisplayTargetService.Module.INSTANCE.isActive()) {
			return false;
		}
		TLType type = object.tType();
		return type != null && DisplayTargetService.getInstance().hasTarget(type);
	}

	@Override
	public void show(ReactContext context, Object value) {
		try {
			ViewActionChain.run(context, List.of(SHOW_OBJECT), value, null);
		} catch (TopLogicException problem) {
			ViewMessages.error(context, I18NConstants.ERROR_CANNOT_SHOW_OBJECT, problem.getErrorKey());
		}
	}
}
