/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a responsive dashboard grid of
 * {@link Tile tiles} with drag-to-reorder support.
 *
 * <p>
 * The client-side layout engine packs tiles left-to-right, stretches the last
 * tile in each row to remove horizontal gaps and extends neighbors vertically
 * to fill gaps left by row-spanning tiles.
 * </p>
 */
public class ReactDashboardControl extends ReactControl {

	private static final String REACT_MODULE = "TLDashboard";

	private static final String MIN_COL_WIDTH = "minColWidth";

	private static final String CHILDREN = "children";

	private static final String ORDER_ARG = "order";

	/**
	 * A single tile of the dashboard.
	 */
	public static final class Tile {

		private final String _id;

		private final TileWidth _width;

		private final int _rowSpan;

		private final ReactControl _control;

		/**
		 * Creates a new {@link Tile}.
		 *
		 * @param id
		 *        Stable tile id, used as persistence key for reordering.
		 * @param width
		 *        The tile's relative width.
		 * @param rowSpan
		 *        Number of rows the tile occupies (>= 1).
		 * @param control
		 *        The control rendered inside the tile.
		 */
		public Tile(String id, TileWidth width, int rowSpan, ReactControl control) {
			_id = id;
			_width = width;
			_rowSpan = Math.max(1, rowSpan);
			_control = control;
		}

		/** The stable id of this tile. */
		public String getId() {
			return _id;
		}

		/** The inner control. */
		public ReactControl getControl() {
			return _control;
		}
	}

	private final List<Tile> _tiles;

	private final Consumer<List<String>> _onReorder;

	/**
	 * Creates a new {@link ReactDashboardControl}.
	 *
	 * @param minColWidth
	 *        CSS length used to decide column count (e.g. {@code "16rem"}).
	 * @param tiles
	 *        Tiles in the order they should appear initially.
	 * @param onReorder
	 *        Callback invoked with the new tile-id order after the user
	 *        reordered tiles. May be {@code null}.
	 */
	public ReactDashboardControl(ReactContext context, String minColWidth, List<Tile> tiles,
			Consumer<List<String>> onReorder) {
		super(context, null, REACT_MODULE);
		_tiles = new ArrayList<>(tiles);
		_onReorder = onReorder;
		putState(MIN_COL_WIDTH, minColWidth);
		putState(CHILDREN, buildDescriptors());
	}

	private List<Map<String, Object>> buildDescriptors() {
		List<Map<String, Object>> list = new ArrayList<>(_tiles.size());
		for (Tile t : _tiles) {
			Map<String, Object> d = new LinkedHashMap<>();
			d.put("id", t._id);
			d.put("width", t._width.getExternalName());
			d.put("rowSpan", Integer.valueOf(t._rowSpan));
			d.put("control", t._control);
			list.add(d);
		}
		return list;
	}

	@Override
	protected void cleanupChildren() {
		for (Tile t : _tiles) {
			t._control.cleanupTree();
		}
	}

	/**
	 * Handles the {@code reorder} command sent by the React client after a
	 * drag-and-drop reorder.
	 */
	@SuppressWarnings("unchecked")
	@ReactCommand("reorder")
	@FrameworkInternal
	void handleReorder(Map<String, Object> arguments) {
		Object raw = arguments.get(ORDER_ARG);
		if (!(raw instanceof List)) {
			return;
		}
		List<String> newOrder = (List<String>) raw;
		reorderTiles(newOrder);
		putState(CHILDREN, buildDescriptors());
		if (_onReorder != null) {
			_onReorder.accept(Collections.unmodifiableList(new ArrayList<>(newOrder)));
		}
	}

	private void reorderTiles(List<String> newOrder) {
		Map<String, Tile> byId = new LinkedHashMap<>();
		for (Tile t : _tiles) {
			byId.put(t._id, t);
		}
		List<Tile> reordered = new ArrayList<>(_tiles.size());
		for (String id : newOrder) {
			Tile t = byId.remove(id);
			if (t != null) {
				reordered.add(t);
			}
		}
		// Append any tiles that weren't mentioned in the new order.
		reordered.addAll(byId.values());
		_tiles.clear();
		_tiles.addAll(reordered);
	}
}
