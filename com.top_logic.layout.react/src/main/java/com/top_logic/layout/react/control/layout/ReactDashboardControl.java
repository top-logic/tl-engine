/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ReactParam;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ReactControl} that renders a responsive dashboard grid of
 * {@link Tile tiles} with drag-to-reorder support.
 *
 * <p>
 * The client-side layout engine packs tiles left-to-right, stretches the last
 * tile in each row to remove horizontal gaps and extends neighbors vertically
 * to fill gaps left by row-spanning tiles.
 * </p>
 *
 * <p>
 * A tile is either shown or hidden. Only shown tiles are published to the client. A hidden tile
 * keeps its position in the order of all tiles, so that it reappears where it was when it is
 * {@link #showTile(String) shown} again. The content of a tile is created when the tile is shown
 * for the first time; a tile that is never shown creates none.
 * </p>
 */
public class ReactDashboardControl extends ReactControl {

	private static final String REACT_MODULE = "TLDashboard";

	private static final String MIN_COL_WIDTH = "minColWidth";

	private static final String ROW_HEIGHT = "rowHeight";

	private static final String CHILDREN = "children";

	private static final String EDIT_MODE = "editMode";

	/** The {@link ReactCommandHandler} that applies a drag-and-drop tile reordering. */
	public static final String REORDER_COMMAND = "reorder";

	private static final String ORDER_ARG = "order";

	/**
	 * The {@link ReactCommandHandler} that runs the action of the tile the user activated.
	 *
	 * @see ActivateTileArguments
	 */
	public static final String ACTIVATE_COMMAND = "activate";

	/**
	 * The {@link ReactCommandHandler} that hides the tile the user removed from the dashboard.
	 *
	 * @see HideTileArguments
	 */
	public static final String HIDE_COMMAND = "hide";

	/** Descriptor key holding a tile's {@link Tile#getId() id}. */
	private static final String TILE_ID = "id";

	/** Descriptor key holding a tile's relative {@link TileWidth width}. */
	private static final String TILE_WIDTH = "width";

	/** Descriptor key holding the number of grid rows a tile occupies. */
	private static final String TILE_ROW_SPAN = "rowSpan";

	/** Descriptor key holding the control rendered inside a tile. */
	private static final String TILE_CONTROL = "control";

	/** Descriptor key telling that a tile is withheld, see {@link #setTileWithheld(String, boolean)}. */
	private static final String TILE_HIDDEN = "hidden";

	/** Descriptor key holding a tile's {@link TileAction action}, {@code null} without one. */
	private static final String TILE_ACTION = "action";

	/** Action descriptor key holding the name the action is offered under. */
	private static final String ACTION_LABEL = "label";

	/** Action descriptor key telling whether the action is currently refused. */
	private static final String ACTION_DISABLED = "disabled";

	/** Action descriptor key holding the text explaining the action, e.g. why it is refused. */
	private static final String ACTION_TOOLTIP = "tooltip";

	/**
	 * What a tile runs when the user activates it.
	 *
	 * @param model
	 *        The command the activation runs. Its state decides whether the tile is offered as an
	 *        entry point at all ({@link CommandModel#isVisible()}) and whether the activation is
	 *        currently accepted ({@link CommandModel#isExecutable()}).
	 * @param label
	 *        The name the action is offered under - what the tile is announced as. {@code null} to
	 *        use the {@link CommandModel#getLabel() model's own label}.
	 */
	public record TileAction(CommandModel model, String label) {
		// Pure data.
	}

	/**
	 * What a tile displays and runs, created when the tile is shown for the first time.
	 *
	 * @param control
	 *        The control rendered inside the tile. It is attached exactly while the tile is
	 *        displayed, so that it can follow what it shows only while it is on screen.
	 * @param action
	 *        What activating the tile runs, {@code null} for a tile that only displays its content.
	 */
	public record TileContent(ReactControl control, TileAction action) {
		// Pure data.
	}

	/**
	 * A single tile of the dashboard.
	 */
	public static final class Tile {

		private final String _id;

		private final TileWidth _width;

		private final int _rowSpan;

		private final Supplier<TileContent> _contentFactory;

		private TileContent _content;

		private boolean _visible;

		private boolean _withheld;

		/**
		 * Creates a {@link Tile}.
		 *
		 * @param id
		 *        Stable tile id, used as persistence key for reordering and hiding.
		 * @param width
		 *        The tile's relative width.
		 * @param rowSpan
		 *        Number of rows the tile occupies (>= 1).
		 * @param visible
		 *        Whether the tile is shown initially.
		 * @param content
		 *        Creates the {@link TileContent content} of the tile. Called at most once, when the
		 *        tile is shown for the first time.
		 */
		public Tile(String id, TileWidth width, int rowSpan, boolean visible, Supplier<TileContent> content) {
			_id = id;
			_width = width;
			_rowSpan = Math.max(1, rowSpan);
			_visible = visible;
			_contentFactory = content;
		}

		/** The stable id of this tile. */
		public String getId() {
			return _id;
		}

		/** Whether this tile is currently shown. */
		public boolean isVisible() {
			return _visible;
		}

		/**
		 * Whether this tile is currently withheld from the dashboard.
		 *
		 * @see ReactDashboardControl#setTileWithheld(String, boolean)
		 */
		public boolean isWithheld() {
			return _withheld;
		}

		/**
		 * Sets whether this tile is withheld from the dashboard when the dashboard is built.
		 *
		 * @return This instance for fluent chaining.
		 */
		public Tile withWithheld(boolean withheld) {
			_withheld = withheld;
			return this;
		}
	}

	private final List<Tile> _tiles;

	private final Consumer<List<String>> _onReorder;

	private final BiConsumer<String, Boolean> _onVisibilityChange;

	private boolean _editMode;

	private final List<Runnable> _stateChangeListeners = new CopyOnWriteArrayList<>();

	private final Runnable _actionChangeHandler = this::handleActionChange;

	/**
	 * Creates a new {@link ReactDashboardControl}.
	 *
	 * @param minColWidth
	 *        CSS length used to decide column count (e.g. {@code "16rem"}).
	 * @param rowHeight
	 *        CSS length of a single grid row (e.g. {@code "16rem"}). A tile is
	 *        as tall as the rows it spans, plus the gaps between them.
	 * @param tiles
	 *        All tiles, shown or hidden, in the order they should appear initially.
	 * @param onReorder
	 *        Callback invoked with the ids of all tiles, shown or hidden, in their new order after
	 *        the user reordered tiles. May be {@code null}.
	 * @param onVisibilityChange
	 *        Callback invoked with a tile id and whether the tile is now shown, after the user
	 *        hid a tile or a tile was {@link #showTile(String) shown} again. {@code null} for a
	 *        dashboard whose tiles cannot be hidden: its {@link #HIDE_COMMAND} does nothing.
	 */
	public ReactDashboardControl(ReactContext context, String minColWidth, String rowHeight, List<Tile> tiles,
			Consumer<List<String>> onReorder, BiConsumer<String, Boolean> onVisibilityChange) {
		super(context, null, REACT_MODULE);
		_tiles = new ArrayList<>(tiles);
		_onReorder = onReorder;
		_onVisibilityChange = onVisibilityChange;
		putState(MIN_COL_WIDTH, minColWidth);
		putState(ROW_HEIGHT, rowHeight);
		putState(CHILDREN, buildDescriptors());
		putState(EDIT_MODE, Boolean.FALSE);
	}

	/**
	 * The descriptors of the shown tiles. Creates the content of a shown tile that has none yet.
	 */
	private List<Map<String, Object>> buildDescriptors() {
		List<Map<String, Object>> list = new ArrayList<>(_tiles.size());
		for (Tile t : _tiles) {
			if (!t._visible) {
				continue;
			}
			TileContent content = contentOf(t);
			Map<String, Object> d = new LinkedHashMap<>();
			d.put(TILE_ID, t._id);
			d.put(TILE_WIDTH, t._width.getExternalName());
			d.put(TILE_ROW_SPAN, Integer.valueOf(t._rowSpan));
			if (!t._withheld) {
				// A withheld tile is not drawn, so its content is not part of the displayed tree.
				d.put(TILE_CONTROL, content.control());
			}
			d.put(TILE_ACTION, actionDescriptor(content.action()));
			if (t._withheld) {
				d.put(TILE_HIDDEN, Boolean.TRUE);
			}
			list.add(d);
		}
		return list;
	}

	/**
	 * The content of the given tile, created on first access.
	 */
	private TileContent contentOf(Tile tile) {
		TileContent content = tile._content;
		if (content == null) {
			content = tile._contentFactory.get();
			tile._content = content;
			TileAction action = content.action();
			if (action != null) {
				action.model().addStateChangeListener(_actionChangeHandler);
			}
		}
		return content;
	}

	/**
	 * What the client needs to offer the tile as an entry point, {@code null} for a tile that only
	 * displays its content.
	 *
	 * <p>
	 * A command that is currently hidden yields {@code null} as well: the tile then shows what it
	 * always shows, with nothing to activate.
	 * </p>
	 */
	private static Map<String, Object> actionDescriptor(TileAction action) {
		if (action == null) {
			return null;
		}
		CommandModel model = action.model();
		if (!model.isVisible()) {
			return null;
		}
		Map<String, Object> d = new LinkedHashMap<>();
		String label = action.label();
		d.put(ACTION_LABEL, label == null || label.isEmpty() ? model.getLabel() : label);
		d.put(ACTION_DISABLED, Boolean.valueOf(!model.isExecutable()));
		d.put(ACTION_TOOLTIP, model.getTooltip());
		return d;
	}

	/**
	 * Republishes the tile descriptors, so that a tile whose action has become available, refused
	 * or hidden is offered accordingly.
	 */
	private void handleActionChange() {
		putState(CHILDREN, buildDescriptors());
	}

	/**
	 * Disposes the content of the hidden tiles as well, which the state of this control does not
	 * show.
	 */
	@Override
	protected void cleanupChildren() {
		super.cleanupChildren();
		for (Tile tile : _tiles) {
			if (tile._content != null) {
				tile._content.control().cleanupTree();
			}
		}
	}

	@Override
	protected void onCleanup() {
		for (Tile tile : _tiles) {
			TileContent content = tile._content;
			if (content != null && content.action() != null) {
				content.action().model().removeStateChangeListener(_actionChangeHandler);
			}
		}
	}

	/**
	 * Whether the dashboard is currently in edit mode (drag-to-reorder active).
	 */
	public boolean isEditMode() {
		return _editMode;
	}

	/**
	 * Switches edit mode on. Notifies listeners registered via
	 * {@link #addStateChangeListener(Runnable)}.
	 */
	public void enterEditMode() {
		setEditMode(true);
	}

	/**
	 * Switches edit mode off. Notifies listeners.
	 */
	public void exitEditMode() {
		setEditMode(false);
	}

	private void setEditMode(boolean value) {
		if (_editMode == value) {
			return;
		}
		_editMode = value;
		putState(EDIT_MODE, Boolean.valueOf(value));
		fireStateChange();
	}

	/**
	 * Whether the tile with the given id is currently shown.
	 *
	 * @return {@code false} for an unknown id.
	 */
	public boolean isTileVisible(String tileId) {
		Tile tile = tile(tileId);
		return tile != null && tile._visible;
	}

	/**
	 * Shows the tile with the given id again, at its position in the order of all tiles.
	 *
	 * <p>
	 * Notifies the visibility callback and the listeners registered via
	 * {@link #addStateChangeListener(Runnable)}. Does nothing for a tile that is shown already or an
	 * unknown id.
	 * </p>
	 */
	public void showTile(String tileId) {
		setTileVisible(tileId, true);
	}

	private void setTileVisible(String tileId, boolean visible) {
		Tile tile = tile(tileId);
		if (tile == null || tile._visible == visible) {
			return;
		}
		tile._visible = visible;
		if (!visible && tile._content != null) {
			// The content is kept for a later show, but is no longer displayed.
			tile._content.control().detach();
		}
		putState(CHILDREN, buildDescriptors());
		if (visible && isAttached()) {
			tile._content.control().attach();
		}
		if (_onVisibilityChange != null) {
			_onVisibilityChange.accept(tileId, Boolean.valueOf(visible));
		}
		fireStateChange();
	}

	/**
	 * Withholds the tile with the given ID from the dashboard, or draws it again.
	 *
	 * <p>
	 * Unlike a tile the user {@link #HIDE_COMMAND hid}, a withheld tile is not offered to be added
	 * again, and nothing is remembered: the tile is withheld e.g. while the current user may not see
	 * it for the object displayed. The grid closes the gap. The content of the tile is kept, but
	 * detached while the tile is withheld.
	 * </p>
	 *
	 * @param tileId
	 *        The ID of the tile.
	 * @param withheld
	 *        Whether the tile is left out.
	 */
	public void setTileWithheld(String tileId, boolean withheld) {
		Tile tile = tile(tileId);
		if (tile == null || tile._withheld == withheld) {
			return;
		}
		tile._withheld = withheld;
		if (tile._content != null && tile._visible) {
			if (withheld) {
				tile._content.control().detach();
			} else if (isAttached()) {
				tile._content.control().attach();
			}
		}
		putState(CHILDREN, buildDescriptors());
	}

	private Tile tile(String tileId) {
		for (Tile tile : _tiles) {
			if (tile._id.equals(tileId)) {
				return tile;
			}
		}
		return null;
	}

	private void fireStateChange() {
		for (Runnable l : _stateChangeListeners) {
			l.run();
		}
	}

	/**
	 * Registers a listener that fires when {@link #isEditMode()} or
	 * {@link #isTileVisible(String)} changes.
	 */
	public void addStateChangeListener(Runnable listener) {
		_stateChangeListeners.add(listener);
	}

	/**
	 * Unregisters a previously added state change listener.
	 */
	public void removeStateChangeListener(Runnable listener) {
		_stateChangeListeners.remove(listener);
	}

	/**
	 * Handles the {@link #REORDER_COMMAND} sent by the React client after a drag-and-drop reorder.
	 *
	 * <p>
	 * The client sends the order of the shown tiles. The hidden tiles keep their positions in the
	 * order of all tiles.
	 * </p>
	 */
	// Argument is a string array (ordered tile ids). The config-JSON binding does not support a List
	// of primitives, so this command keeps a raw Map with a lightweight @ReactParam schema.
	@ReactCommandHandler(value = REORDER_COMMAND, params = @ReactParam(name = ORDER_ARG, type = "string[]",
		required = true, description = "The ids of the shown tiles in their new order."))
	@FrameworkInternal
	void handleReorder(Map<String, Object> arguments) {
		@SuppressWarnings("unchecked")
		List<String> newOrder = (List<String>) arguments.get(ORDER_ARG);
		reorderTiles(newOrder);
		putState(CHILDREN, buildDescriptors());
		if (_onReorder != null) {
			List<String> allIds = new ArrayList<>(_tiles.size());
			for (Tile tile : _tiles) {
				allIds.add(tile._id);
			}
			_onReorder.accept(Collections.unmodifiableList(allIds));
		}
	}

	/**
	 * Handles the {@link #ACTIVATE_COMMAND} the React client sends when the user opens a tile.
	 *
	 * <p>
	 * The tile's command runs through {@link CommandModel#executeCommand(ReactContext)}: a command
	 * that currently refuses to run is not executed, and its refusal is the result of the
	 * activation. An activation of a tile without an action, of a hidden or withheld tile, or of an
	 * unknown tile does nothing.
	 * </p>
	 *
	 * @param args
	 *        Names the tile the user activated, see {@link ActivateTileArguments#getTileId()}.
	 */
	@ReactCommandHandler(ACTIVATE_COMMAND)
	@FrameworkInternal
	HandlerResult handleActivate(ActivateTileArguments args) {
		Tile tile = tile(args.getTileId());
		if (tile == null || !tile._visible || tile._withheld) {
			return HandlerResult.DEFAULT_RESULT;
		}
		TileAction action = contentOf(tile).action();
		if (action == null) {
			return HandlerResult.DEFAULT_RESULT;
		}
		return action.model().executeCommand(getReactContext());
	}

	/**
	 * Handles the {@link #HIDE_COMMAND} the React client sends when the user removes a tile from the
	 * dashboard.
	 *
	 * <p>
	 * The tile is no longer published and its content is no longer displayed. A dashboard created
	 * without a visibility callback, a hidden tile and an unknown tile ignore the command.
	 * </p>
	 *
	 * @param args
	 *        Names the tile the user hid, see {@link HideTileArguments#getTileId()}.
	 */
	@ReactCommandHandler(HIDE_COMMAND)
	@FrameworkInternal
	void handleHide(HideTileArguments args) {
		if (_onVisibilityChange == null) {
			return;
		}
		setTileVisible(args.getTileId(), false);
	}

	/**
	 * Applies the given order of tiles: the positions of the mentioned tiles are filled with them in
	 * the given order, all other tiles keep their positions.
	 */
	private void reorderTiles(List<String> newOrder) {
		Map<String, Tile> byId = new LinkedHashMap<>();
		for (Tile t : _tiles) {
			byId.put(t._id, t);
		}
		List<Tile> mentioned = new ArrayList<>(newOrder.size());
		Set<String> mentionedIds = new HashSet<>();
		for (String id : newOrder) {
			Tile t = byId.get(id);
			if (t != null && mentionedIds.add(id)) {
				mentioned.add(t);
			}
		}
		int next = 0;
		for (int n = 0, cnt = _tiles.size(); n < cnt; n++) {
			if (mentionedIds.contains(_tiles.get(n)._id)) {
				_tiles.set(n, mentioned.get(next++));
			}
		}
	}
}
