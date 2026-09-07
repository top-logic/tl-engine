/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.Targeted;

/**
 * Chrome control produced by a menu-carrying view element.
 *
 * <p>
 * Wraps a single child content control and forwards {@link #CMD_OPEN_MENU} requests from the client
 * to the {@link ContextMenuOpener} of the enclosing frame, reading the current target value from
 * the injected target supplier before opening the menu. The {@link MenuTrigger} decides which
 * gesture the client listens for, and thus where the menu appears: at the pointer for
 * {@link MenuTrigger#CONTEXT_MENU}, below the region for {@link MenuTrigger#CLICK}. Either way the
 * client sends the viewport coordinates the menu is placed at, so the opener needs to know nothing
 * about the trigger.
 * </p>
 *
 * @see com.top_logic.layout.view.element.ContextMenuElement
 * @see com.top_logic.layout.view.element.MenuElement
 */
public class MenuRegionControl extends ReactControl {

	private static final String REACT_MODULE = "TLMenuRegion";

	/** State key for the wrapped content control. */
	private static final String CHILD = "child";

	/** State key for the wrapped content control's ID. */
	private static final String CHILD_ID = "childId";

	/** State key for the {@link MenuTrigger} the client listens for. */
	private static final String TRIGGER = "trigger";

	/** The {@link ReactCommandHandler} sent when the region's menu is to be opened. */
	public static final String CMD_OPEN_MENU = "openMenu";

	/** Argument key for the viewport x coordinate the menu is placed at. */
	private static final String ARG_X = "x";

	/** Argument key for the viewport y coordinate the menu is placed at. */
	private static final String ARG_Y = "y";

	private final ContextMenuContribution _contribution;

	private final Supplier<Object> _targetSupplier;

	private final ContextMenuOpener _opener;

	/**
	 * Creates a {@link MenuRegionControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param child
	 *        The child content control whose DOM region carries the trigger gesture.
	 * @param contribution
	 *        The contribution supplying commands.
	 * @param targetSupplier
	 *        Supplier for the current target value at trigger time.
	 * @param opener
	 *        The frame's context menu opener.
	 * @param trigger
	 *        The gesture that opens the menu.
	 */
	public MenuRegionControl(ReactContext context, ReactControl child,
			ContextMenuContribution contribution, Supplier<Object> targetSupplier, ContextMenuOpener opener,
			MenuTrigger trigger) {
		super(context, null, REACT_MODULE);
		_contribution = contribution;
		_targetSupplier = targetSupplier;
		_opener = opener;

		putState(CHILD, child);
		putState(CHILD_ID, child.getID());
		putState(TRIGGER, trigger.getExternalName());
	}

	/**
	 * Opens the menu at the given client coordinates, using the current target supplier value as the
	 * selection target.
	 */
	@ReactCommandHandler(CMD_OPEN_MENU)
	void handleOpen(Map<String, Object> arguments) {
		int x = intArg(arguments, ARG_X);
		int y = intArg(arguments, ARG_Y);
		Object target = _targetSupplier == null ? null : _targetSupplier.get();
		_opener.open(x, y, List.of(new Targeted(_contribution, target)));
	}

	private static int intArg(Map<String, Object> arguments, String key) {
		Object value = arguments.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value instanceof String) {
			return (int) Double.parseDouble((String) value);
		}
		return 0;
	}
}
