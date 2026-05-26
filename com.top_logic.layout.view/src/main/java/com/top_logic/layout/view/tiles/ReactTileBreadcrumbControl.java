/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.tiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.channel.ViewChannel.ChannelListener;
import com.top_logic.util.Resources;

/**
 * Server-side control of {@link TileBreadcrumbElement}.
 *
 * <p>
 * Reuses the generic {@code TLBreadcrumb} React component which expects a state
 * {@code items: [{id, label}]} and emits a {@code navigate} command with the clicked
 * {@code itemId}. The control maps the {@code List<TileFrame>} path to that representation: the
 * home crumb gets id {@code "0"}; frame at path index {@code i} gets id {@code "<i+1>"}. The
 * click handler interprets the id as the target stack depth and writes the corresponding path
 * prefix back to the channel.
 * </p>
 */
public class ReactTileBreadcrumbControl extends ReactControl {

	private static final String REACT_MODULE = "TLBreadcrumb";

	private static final String ITEMS = "items";

	private static final String NAVIGATE_COMMAND = "navigate";

	private static final String ITEM_ID = "itemId";

	private final ViewChannel _pathChannel;

	private final ResKey _homeLabel;

	private final ChannelListener _pathListener;

	/**
	 * Creates a new {@link ReactTileBreadcrumbControl}.
	 *
	 * @param context
	 *        The view context for ID allocation and SSE registration.
	 * @param pathChannel
	 *        The channel holding the {@code List<TileFrame>} path.
	 * @param homeLabel
	 *        Label for the home crumb, or {@code null} to fall back to a generic default.
	 */
	public ReactTileBreadcrumbControl(ReactContext context, ViewChannel pathChannel, ResKey homeLabel) {
		super(context, null, REACT_MODULE);
		_pathChannel = pathChannel;
		_homeLabel = homeLabel;

		_pathListener = (sender, oldValue, newValue) -> putState(ITEMS, buildItems());
		_pathChannel.addListener(_pathListener);
		addCleanupAction(() -> _pathChannel.removeListener(_pathListener));

		putStateSilent(ITEMS, buildItems());
	}

	@SuppressWarnings("unchecked")
	private List<Map<String, Object>> buildItems() {
		Object value = _pathChannel.get();
		List<TileFrame> path = value instanceof List<?> list ? (List<TileFrame>) list : List.of();

		Resources resources = Resources.getInstance();
		List<Map<String, Object>> items = new ArrayList<>(path.size() + 1);
		items.add(crumb("0", homeText(resources)));
		for (int i = 0; i < path.size(); i++) {
			TileFrame frame = path.get(i);
			items.add(crumb(String.valueOf(i + 1), frameText(resources, frame)));
		}
		return items;
	}

	private Map<String, Object> crumb(String id, String label) {
		Map<String, Object> entry = new HashMap<>(2);
		entry.put("id", id);
		entry.put("label", label);
		return entry;
	}

	private String homeText(Resources resources) {
		if (_homeLabel != null) {
			return resources.getString(_homeLabel);
		}
		return "Home";
	}

	private String frameText(Resources resources, TileFrame frame) {
		ResKey label = frame.getLabel();
		if (label != null) {
			return resources.getString(label);
		}
		return frame.getViewRef();
	}

	/**
	 * Handles a crumb click from the React component.
	 *
	 * <p>
	 * The {@code itemId} is the target stack depth as a string. Truncates the path channel to that
	 * length; the tile-stack listening on the same channel rebuilds its top frame accordingly.
	 * </p>
	 */
	@ReactCommand(NAVIGATE_COMMAND)
	void handleNavigate(Map<String, Object> arguments) {
		Object idValue = arguments.get(ITEM_ID);
		if (!(idValue instanceof String idString)) {
			Logger.warn("Missing or non-string '" + ITEM_ID + "' in navigate arguments: " + arguments,
				ReactTileBreadcrumbControl.class);
			return;
		}
		int depth;
		try {
			depth = Integer.parseInt(idString);
		} catch (NumberFormatException ex) {
			Logger.warn("Non-numeric breadcrumb itemId: " + idString, ReactTileBreadcrumbControl.class);
			return;
		}

		@SuppressWarnings("unchecked")
		List<TileFrame> current = _pathChannel.get() instanceof List<?> list
			? (List<TileFrame>) list
			: List.of();
		int target = Math.max(0, Math.min(depth, current.size()));
		if (target == current.size()) {
			return;
		}
		_pathChannel.set(Collections.unmodifiableList(new ArrayList<>(current.subList(0, target))));
	}
}
