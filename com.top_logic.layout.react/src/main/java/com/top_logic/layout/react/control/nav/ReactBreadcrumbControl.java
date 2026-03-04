/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.nav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Navigation trail showing the current location in a hierarchy.
 *
 * <p>
 * Renders a breadcrumb bar with clickable ancestor items. The last item is displayed as plain text
 * (current page).
 * </p>
 */
public class ReactBreadcrumbControl extends ReactControl {

	private static final String REACT_MODULE = "TLBreadcrumb";

	private static final String ITEMS = "items";

	private static final String ENTRY_ID = "id";

	private static final String ENTRY_LABEL = "label";

	private static final String ITEM_ID_ARG = "itemId";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new NavigateCommand());

	private final Consumer<String> _navigateHandler;

	/**
	 * Creates a breadcrumb navigation trail.
	 *
	 * @param items
	 *        The breadcrumb entries (last = current page).
	 * @param navigateHandler
	 *        Called with the item ID when an ancestor is clicked.
	 */
	public ReactBreadcrumbControl(List<BreadcrumbEntry> items, Consumer<String> navigateHandler) {
		super(null, REACT_MODULE, COMMANDS);
		_navigateHandler = navigateHandler;
		updateItems(items);
	}

	/**
	 * Updates the breadcrumb items.
	 *
	 * @param items
	 *        The new breadcrumb entries.
	 */
	public void updateItems(List<BreadcrumbEntry> items) {
		List<Map<String, String>> itemList = new ArrayList<>();
		for (BreadcrumbEntry entry : items) {
			Map<String, String> map = new HashMap<>();
			map.put(ENTRY_ID, entry.id());
			map.put(ENTRY_LABEL, entry.label());
			itemList.add(map);
		}
		putState(ITEMS, itemList);
	}

	/**
	 * A single entry in a breadcrumb trail.
	 *
	 * @param id
	 *        The entry identifier.
	 * @param label
	 *        The display label.
	 */
	public record BreadcrumbEntry(String id, String label) {
		// Record
	}

	/**
	 * Command sent when a breadcrumb item is clicked.
	 */
	public static class NavigateCommand extends ControlCommand {

		/** Creates a {@link NavigateCommand}. */
		public NavigateCommand() {
			super("navigate");
		}

		@Override
		public ResKey getI18NKey() {
			return I18NConstants.REACT_BREADCRUMB_NAVIGATE;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactBreadcrumbControl bc = (ReactBreadcrumbControl) control;
			String itemId = (String) arguments.get(ITEM_ID_ARG);
			bc._navigateHandler.accept(itemId);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
