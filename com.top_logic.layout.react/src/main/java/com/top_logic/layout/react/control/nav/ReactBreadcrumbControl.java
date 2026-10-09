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

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.state.BreadcrumbState;

/**
 * Navigation trail showing the current location in a hierarchy.
 *
 * <p>
 * Renders a breadcrumb bar with clickable ancestor items. The last item is displayed as plain text
 * (current page).
 * </p>
 *
 * <p>
 * The state is described by {@link BreadcrumbState}.
 * </p>
 */
public class ReactBreadcrumbControl extends ReactControl {

	private static final String REACT_MODULE = "TLBreadcrumb";

	/** The {@link ReactCommandHandler} that navigates to a breadcrumb ancestor. */
	public static final String NAVIGATE_COMMAND = "navigate";

	private final Consumer<String> _navigateHandler;

	/**
	 * Creates a breadcrumb navigation trail.
	 *
	 * @param items
	 *        The breadcrumb entries (last = current page).
	 * @param navigateHandler
	 *        Called with the item ID when an ancestor is clicked.
	 */
	public ReactBreadcrumbControl(ReactContext context, List<BreadcrumbEntry> items, Consumer<String> navigateHandler) {
		super(context, null, REACT_MODULE);
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
			map.put(BreadcrumbState.Item.ID__PROP, entry.id());
			map.put(BreadcrumbState.Item.LABEL__PROP, entry.label());
			itemList.add(map);
		}
		putState(BreadcrumbState.ITEMS__PROP, itemList);
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
	@ReactCommandHandler(NAVIGATE_COMMAND)
	void handleNavigate(NavigateArguments args) {
		_navigateHandler.accept(args.getItemId());
	}

}
