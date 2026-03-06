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

import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

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

	/** @see #updateItems(List) */
	private static final String ITEMS = "items";

	/** Entry identifier within the breadcrumb trail. */
	private static final String ENTRY_ID = "id";

	/** Entry display label. */
	private static final String ENTRY_LABEL = "label";

	/** Command argument: the navigated item identifier. */
	private static final String ITEM_ID_ARG = "itemId";

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
		super(null, REACT_MODULE);
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

	// -- Commands --

	/**
	 * Handles breadcrumb navigation from the client.
	 */
	@ReactCommand("navigate")
	void handleNavigate(Map<String, Object> arguments) {
		String itemId = (String) arguments.get(ITEM_ID_ARG);
		_navigateHandler.accept(itemId);
	}

}
