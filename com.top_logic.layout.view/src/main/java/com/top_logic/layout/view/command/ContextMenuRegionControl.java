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
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.overlay.ContextMenuContribution;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener;
import com.top_logic.layout.react.control.overlay.ContextMenuOpener.Targeted;

/**
 * Chrome control produced by a {@code <context-menu>} view element.
 *
 * <p>
 * Wraps a single child content control and forwards {@code openContextMenu} requests from the
 * client to the {@link ContextMenuOpener} of the enclosing frame, reading the current target value
 * from the injected target supplier before opening the menu.
 * </p>
 */
public class ContextMenuRegionControl extends ReactControl {

	private static final String REACT_MODULE = "TLContextMenuRegion";

	private static final String CHILD_ID = "childId";

	private static final String CHILD = "child";

	private final ReactControl _child;

	private final ContextMenuContribution _contribution;

	private final Supplier<Object> _targetSupplier;

	private final ContextMenuOpener _opener;

	/**
	 * Creates a {@link ContextMenuRegionControl}.
	 *
	 * @param context
	 *        The React context.
	 * @param child
	 *        The child content control whose DOM region triggers the context menu.
	 * @param contribution
	 *        The contribution supplying commands.
	 * @param targetSupplier
	 *        Supplier for the current target value at click time.
	 * @param opener
	 *        The frame's context menu opener.
	 */
	public ContextMenuRegionControl(ReactContext context, ReactControl child,
			ContextMenuContribution contribution, Supplier<Object> targetSupplier, ContextMenuOpener opener) {
		super(context, null, REACT_MODULE);
		_child = child;
		_contribution = contribution;
		_targetSupplier = targetSupplier;
		_opener = opener;

		putState(CHILD, child);
		putState(CHILD_ID, child.getID());
	}

	@Override
	protected void cleanupChildren() {
		if (_child != null) {
			_child.cleanupTree();
		}
	}

	/**
	 * Opens the context menu at the given client coordinates, using the current target supplier
	 * value as the selection target.
	 */
	@ReactCommand("openContextMenu")
	void handleOpen(Map<String, Object> arguments) {
		int x = intArg(arguments, "x");
		int y = intArg(arguments, "y");
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
