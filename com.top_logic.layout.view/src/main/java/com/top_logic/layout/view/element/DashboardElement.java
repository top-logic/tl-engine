/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.TypeIcon;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.Tile;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.TileContent;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.command.ViewCommandModel;
import com.top_logic.util.TLContext;

/**
 * A UI element that renders a responsive dashboard grid of {@link TileElement
 * tiles}.
 *
 * <p>
 * Tile order is personalized per user via {@link PersonalConfiguration}, keyed
 * by {@link Config#getId() this element's id}. Unknown ids in the persisted
 * order are ignored; tiles not mentioned are appended in their configured
 * order.
 * </p>
 *
 * <p>
 * Which tiles are shown is personalized as well. A user hides a tile while editing the layout and
 * adds it again through a menu of the surrounding toolbar that offers one command per hidden tile,
 * named after the tile. Only the tiles a user shows or hides deviating from their default are stored: a
 * tile is shown by default unless it is {@link TileElement.Config#isOptional() optional}. A tile
 * added to the configuration therefore appears for everyone who has not hidden it. A tile the user
 * may not access is neither shown nor offered.
 * </p>
 *
 * <p>
 * An anonymous session is offered neither the layout edit commands nor tile
 * reordering nor hiding: all anonymous visitors share one account, so there is no personal
 * layout to store for any of them.
 * </p>
 *
 * @implNote The commands adding a tile again form the clique
 *           {@link com.top_logic.layout.view.command.CommandCliques#DASHBOARD_TILES}, which the
 *           {@link com.top_logic.layout.view.command.CommandCliqueService} displays as menu.
 */
@InApp
public class DashboardElement implements UIElement {

	/**
	 * Prefix of the {@link PersonalConfiguration} key, followed by the dashboard id, holding the
	 * ids of all tiles in the order the user arranged them.
	 */
	private static final String PC_KEY_PREFIX = "dashboard.tileOrder.";

	/**
	 * Prefix of the {@link PersonalConfiguration} key, followed by the dashboard id, holding a JSON
	 * object that maps the id of a tile to whether it is shown.
	 *
	 * <p>
	 * Only tiles shown or hidden deviating from their default are mentioned.
	 * </p>
	 */
	public static final String PC_VISIBILITY_KEY_PREFIX = "dashboard.tileVisibility.";

	/**
	 * Configuration for {@link DashboardElement}.
	 */
	@TagName("dashboard")
	@TypeIcon("css:bi bi-grid-1x2")
	public interface Config extends UIElement.Config {

		/** Config property name for {@link #getMinColWidth()}. */
		String MIN_COL_WIDTH = "min-col-width";

		/** Config property name for {@link #getRowHeight()}. */
		String ROW_HEIGHT = "row-height";

		/** Config property name for {@link #getTiles()}. */
		String TILES = "tiles";

		@Override
		@ClassDefault(DashboardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Stable id used as persistence key for personal tile ordering.
		 */
		@Override
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * CSS length hint used by the client to decide the column count.
		 * Defaults to {@code 16rem}.
		 */
		@Name(MIN_COL_WIDTH)
		@StringDefault("16rem")
		String getMinColWidth();

		/**
		 * The height of one grid row, as a CSS length.
		 *
		 * <p>
		 * A tile is as many rows tall as the row span of its
		 * {@link TileElement}, plus the gaps between those rows. Content that
		 * fills the available height, a panel configured to fill or a table,
		 * resolves its height against the tile and scrolls inside it; content
		 * that does not fill and is taller than the tile scrolls inside the
		 * tile as well.
		 * </p>
		 */
		@Name(ROW_HEIGHT)
		@StringDefault("16rem")
		String getRowHeight();

		/**
		 * The tiles.
		 *
		 * @implNote Written directly as {@code <tile>} children of the {@code <dashboard>}, the
		 *           only entries accepted, since the entry type only binds the instance type to
		 *           {@link UIElement}. Keyed by {@link TileElement.Config#ID} so that a same-path
		 *           overlay can address a tile.
		 */
		@Name(TILES)
		@Subtypes({ @Subtype(tag = TileElement.Config.TAG_NAME, type = TileElement.Config.class) })
		@com.top_logic.basic.config.annotation.Key(TileElement.Config.ID)
		@com.top_logic.basic.config.annotation.DefaultContainer
		@TreeProperty
		List<TileElement.Config> getTiles();
	}

	private final String _id;

	private final String _minColWidth;

	private final String _rowHeight;

	private final List<TileElement> _tiles;

	private final String _cssClass;

	/**
	 * Creates a new {@link DashboardElement} from configuration.
	 */
	@CalledByReflection
	public DashboardElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_minColWidth = config.getMinColWidth();
		_rowHeight = config.getRowHeight();
		_tiles = new ArrayList<>();
		for (TileElement.Config tc : config.getTiles()) {
			TileElement tile = (TileElement) context.getInstance(tc);
			if (tile != null) {
				_tiles.add(tile);
			}
		}
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(List.<UIElement> copyOf(_tiles)));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// The shared anonymous account has no personal layout of its own, so an anonymous session
		// gets a dashboard that offers no rearranging and no hiding at all.
		boolean personalizable = !TLContext.isAnonymous();
		Map<String, Boolean> visibility = personalizable ? readPersonalVisibility() : Map.of();

		List<TileElement> ordered = applyPersonalOrder(_tiles);
		List<TileElement> accessible = new ArrayList<>(ordered.size());
		List<Tile> reactTiles = new ArrayList<>(ordered.size());
		for (TileElement t : ordered) {
			if (!t.isAccessible()) {
				// Access denied for the current user: omit the tile entirely.
				continue;
			}
			accessible.add(t);
			Boolean stored = visibility.get(t.getId());
			boolean visible = stored != null ? stored.booleanValue() : defaultVisibility(t);
			reactTiles.add(new Tile(t.getId(), t.getWidth(), t.getRowSpan(), visible,
				() -> createTileContent(context, t)));
		}
		ReactDashboardControl control = new ReactDashboardControl(context, _minColWidth, _rowHeight, reactTiles,
			personalizable ? this::storePersonalOrder : null,
			personalizable ? (tileId, visible) -> storePersonalVisibility(tileId, visible.booleanValue()) : null);

		control.setCssClass(_cssClass);
		if (personalizable) {
			contributeEditCommands(context, control, accessible);
		}

		return control;
	}

	/**
	 * Whether the given tile is shown for a user who has neither hidden nor added it.
	 */
	private static boolean defaultVisibility(TileElement tile) {
		return !tile.isOptional();
	}

	/**
	 * Creates what the given tile displays and runs.
	 *
	 * <p>
	 * The model of the tile's action follows its input exactly while the tile's content is
	 * displayed, so that a tile is offered, refused or hidden according to what its command says
	 * about the current input.
	 * </p>
	 */
	private static TileContent createTileContent(ViewContext context, TileElement tile) {
		ReactControl content = tile.createContentControl(context);
		ViewCommandModel action = tile.createActionModel(context);
		if (action != null) {
			content.addAttachListener(() -> action.attach(context.getModelScope()));
			content.addDetachListener(action::detach);
		}
		return new TileContent(content, tile.toAction(action));
	}

	private void contributeEditCommands(ViewContext context, ReactDashboardControl control,
			List<TileElement> tiles) {
		CommandScope scope = context.getScope(CommandScope.class);
		if (scope == null) {
			return;
		}
		List<DashboardCommandModel> commands = new ArrayList<>(2 + tiles.size());
		commands.add(DashboardCommandModel.editCommand(control));
		commands.add(DashboardCommandModel.doneCommand(control));
		for (TileElement tile : tiles) {
			commands.add(DashboardCommandModel.showTileCommand(control, tile.getId(), tile.getTitle()));
		}

		control.addAttachListener(() -> {
			for (DashboardCommandModel command : commands) {
				command.attach();
				scope.addCommand(command);
			}
		});
		control.addDetachListener(() -> {
			for (DashboardCommandModel command : commands) {
				scope.removeCommand(command);
				command.detach();
			}
		});
	}

	private List<TileElement> applyPersonalOrder(List<TileElement> tiles) {
		List<String> personal = readPersonalOrder();
		if (personal == null || personal.isEmpty()) {
			return tiles;
		}
		Map<String, TileElement> byId = new LinkedHashMap<>();
		for (TileElement t : tiles) {
			byId.put(t.getId(), t);
		}
		List<TileElement> result = new ArrayList<>(tiles.size());
		for (String id : personal) {
			TileElement t = byId.remove(id);
			if (t != null) {
				result.add(t);
			}
		}
		result.addAll(byId.values());
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<String> readPersonalOrder() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		Object value = pc.getJSONValue(PC_KEY_PREFIX + _id);
		if (value instanceof List) {
			List<?> raw = (List<?>) value;
			List<String> result = new ArrayList<>(raw.size());
			for (Object o : raw) {
				if (o instanceof String) {
					result.add((String) o);
				}
			}
			return result;
		}
		return null;
	}

	/**
	 * The tile visibilities the current user stored, tile id to whether the tile is shown.
	 */
	private Map<String, Boolean> readPersonalVisibility() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return Map.of();
		}
		Map<String, Boolean> result = new HashMap<>();
		if (pc.getJSONValue(PC_VISIBILITY_KEY_PREFIX + _id) instanceof Map<?, ?> raw) {
			for (Map.Entry<?, ?> entry : raw.entrySet()) {
				if (entry.getKey() instanceof String tileId && entry.getValue() instanceof Boolean visible) {
					result.put(tileId, visible);
				}
			}
		}
		return result;
	}

	/**
	 * Stores that the given tile is shown or hidden, as a deviation from its default, or drops the
	 * stored entry where the tile is back to its default.
	 */
	private void storePersonalVisibility(String tileId, boolean visible) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			Map<String, Boolean> visibility = new LinkedHashMap<>(readPersonalVisibility());
			TileElement tile = tile(tileId);
			if (tile != null && visible == defaultVisibility(tile)) {
				visibility.remove(tileId);
			} else {
				visibility.put(tileId, Boolean.valueOf(visible));
			}
			pc.setJSONValue(PC_VISIBILITY_KEY_PREFIX + _id, visibility.isEmpty() ? null : visibility);
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Failed to persist dashboard tile visibility for '" + _id + "'.", ex,
				DashboardElement.class);
		}
	}

	private TileElement tile(String tileId) {
		for (TileElement tile : _tiles) {
			if (tile.getId().equals(tileId)) {
				return tile;
			}
		}
		return null;
	}

	private void storePersonalOrder(List<String> order) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			pc.setJSONValue(PC_KEY_PREFIX + _id, order);
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Failed to persist dashboard tile order for '" + _id + "'.", ex, DashboardElement.class);
		}
	}
}
