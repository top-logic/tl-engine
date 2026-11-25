/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.Tile;
import com.top_logic.layout.view.ChildGroup;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandScope;
import com.top_logic.layout.view.security.UnitHiding;
import com.top_logic.util.TLContext;

/**
 * A UI element that renders a responsive dashboard grid of tiles.
 *
 * <p>
 * The tiles are contributed by the {@link Config#getEntries() entries} of the dashboard: a
 * {@link TileElement &lt;tile&gt;} contributes one fixed tile, a {@link TileListElement
 * &lt;tiles&gt;} one tile per object of a computed list. All tiles share one order, and the tiles of
 * a {@code <tiles>} entry follow its objects while the dashboard is displayed.
 * </p>
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

		/** Config property name for {@link #getEntries()}. */
		String ENTRIES = "entries";

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
		 * The entries contributing the tiles: fixed {@code <tile>}s and {@code <tiles>} computed
		 * from a list of objects.
		 *
		 * <p>
		 * Without an arrangement of the user, the tiles appear in the order of their entries, the
		 * tiles of a {@code <tiles>} entry in the order of its objects.
		 * </p>
		 *
		 * @implNote Written directly as children of the {@code <dashboard>}. Keyed by
		 *           {@link TileDeclaration.Config#ID} so that a same-path overlay can address an
		 *           entry.
		 */
		@Name(ENTRIES)
		@Subtypes({
			@Subtype(tag = TileElement.Config.TAG_NAME, type = TileElement.Config.class),
			@Subtype(tag = TileListElement.Config.TAG_NAME, type = TileListElement.Config.class) })
		@com.top_logic.basic.config.annotation.Key(TileDeclaration.Config.ID)
		@com.top_logic.basic.config.annotation.DefaultContainer
		@TreeProperty
		List<TileDeclaration.Config> getEntries();
	}

	private final String _id;

	private final String _minColWidth;

	private final String _rowHeight;

	private final List<TileDeclaration> _declarations;

	private final String _cssClass;

	/**
	 * Creates a new {@link DashboardElement} from configuration.
	 */
	@CalledByReflection
	public DashboardElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_minColWidth = config.getMinColWidth();
		_rowHeight = config.getRowHeight();
		_declarations = new ArrayList<>();
		for (TileDeclaration.Config entry : config.getEntries()) {
			TileDeclaration declaration = (TileDeclaration) context.getInstance(entry);
			if (declaration != null) {
				_declarations.add(declaration);
			}
		}
		_cssClass = config.getCssClass();
	}

	@Override
	public List<ChildGroup> getChildGroups() {
		return List.of(ChildGroup.elements(List.<UIElement> copyOf(_declarations)));
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		return new Display(context).control();
	}

	/**
	 * One display of the dashboard: its control, the tiles of its entries and the arrangement of
	 * the user.
	 */
	private final class Display implements DashboardTiles.Host {

		/**
		 * Whether the user may rearrange the dashboard. The shared anonymous account has no personal
		 * layout of its own, so an anonymous session gets a dashboard that offers no rearranging and
		 * no hiding at all.
		 */
		private final boolean _personalizable;

		private final List<DashboardTiles> _sources = new ArrayList<>();

		/** The stored order of the tile ids, empty while the user has not rearranged the dashboard. */
		private List<String> _order;

		/** The stored tile visibilities deviating from the default, tile id to whether it is shown. */
		private final Map<String, Boolean> _visibility;

		/** The current tiles by id, in the order of their declaration. */
		private Map<String, DashboardTiles.Entry> _entries;

		private final ReactDashboardControl _control;

		/** The command scope the edit commands are offered in, {@code null} for none. */
		private final CommandScope _scope;

		/** The commands adding a hidden tile again, by tile id. */
		private final Map<String, DashboardCommandModel> _tileCommands = new LinkedHashMap<>();

		Display(ViewContext context) {
			_personalizable = !TLContext.isAnonymous();
			_order = _personalizable ? readPersonalOrder() : List.of();
			_visibility = _personalizable ? readPersonalVisibility() : new HashMap<>();
			for (TileDeclaration declaration : _declarations) {
				_sources.add(declaration.createTiles(context, this));
			}
			_entries = collectEntries();

			_control = new ReactDashboardControl(context, _minColWidth, _rowHeight, arrangeTiles(),
				_personalizable ? this::handleReorder : null,
				_personalizable ? this::handleVisibilityChange : null);
			_control.setCssClass(_cssClass);

			for (DashboardTiles.Entry entry : _entries.values()) {
				UnitHiding hiding = entry.hiding();
				if (hiding != null) {
					String id = entry.id();
					hiding.bind(_control, hidden -> _control.setTileWithheld(id, hidden));
				}
			}

			_control.addAttachListener(() -> {
				for (DashboardTiles source : _sources) {
					source.attach(context.getModelScope());
				}
			});
			_control.addDetachListener(() -> {
				for (DashboardTiles source : _sources) {
					source.detach();
				}
			});
			_control.addCleanupAction(() -> {
				for (DashboardTiles source : _sources) {
					source.dispose();
				}
			});

			_scope = _personalizable ? context.getScope(CommandScope.class) : null;
			if (_scope != null) {
				contributeEditCommands();
			}
		}

		ReactDashboardControl control() {
			return _control;
		}

		@Override
		public void tilesChanged() {
			_entries = collectEntries();
			_control.setTiles(arrangeTiles());
			if (_scope != null) {
				updateTileCommands();
			}
		}

		@Override
		public void activationChanged() {
			_control.updateActivation();
		}

		/**
		 * The tiles of all entries by id, in the order of their declaration.
		 */
		private Map<String, DashboardTiles.Entry> collectEntries() {
			Map<String, DashboardTiles.Entry> result = new LinkedHashMap<>();
			for (DashboardTiles source : _sources) {
				for (DashboardTiles.Entry entry : source.tiles()) {
					result.putIfAbsent(entry.id(), entry);
				}
			}
			return result;
		}

		/**
		 * The tiles of the control in the arrangement of the user: the tiles of the stored order
		 * first, skipping stored ids without a tile, then the others in the order of their
		 * declaration.
		 */
		private List<Tile> arrangeTiles() {
			Map<String, DashboardTiles.Entry> remaining = new LinkedHashMap<>(_entries);
			List<DashboardTiles.Entry> ordered = new ArrayList<>(remaining.size());
			for (String id : _order) {
				DashboardTiles.Entry entry = remaining.remove(id);
				if (entry != null) {
					ordered.add(entry);
				}
			}
			ordered.addAll(remaining.values());

			List<Tile> result = new ArrayList<>(ordered.size());
			for (DashboardTiles.Entry entry : ordered) {
				Boolean stored = _visibility.get(entry.id());
				boolean visible = stored != null ? stored.booleanValue() : !entry.optional();
				Tile tile = new Tile(entry.id(), entry.width(), entry.rowSpan(), visible, entry.content())
					.withLabel(entry.label());
				UnitHiding hiding = entry.hiding();
				if (hiding != null) {
					tile.withWithheld(hiding.isHidden());
				}
				if (entry.activation() != null) {
					tile.withActivation(entry.activation(), entry.item());
				}
				result.add(tile);
			}
			return result;
		}

		private void handleReorder(List<String> order) {
			_order = new ArrayList<>(order);
			storePersonalOrder(_order);
		}

		/**
		 * Remembers that the given tile is shown or hidden, as a deviation from its default, or
		 * drops the remembered entry where the tile is back to its default.
		 */
		private void handleVisibilityChange(String tileId, Boolean visible) {
			DashboardTiles.Entry entry = _entries.get(tileId);
			if (entry != null && visible.booleanValue() == !entry.optional()) {
				_visibility.remove(tileId);
			} else {
				_visibility.put(tileId, visible);
			}
			storePersonalVisibility(_visibility);
		}

		private void contributeEditCommands() {
			List<DashboardCommandModel> commands = List.of(
				DashboardCommandModel.editCommand(_control),
				DashboardCommandModel.doneCommand(_control));
			updateTileCommands();

			_control.addAttachListener(() -> {
				for (DashboardCommandModel command : commands) {
					offer(command);
				}
				for (DashboardCommandModel command : _tileCommands.values()) {
					offer(command);
				}
			});
			_control.addDetachListener(() -> {
				for (DashboardCommandModel command : commands) {
					withdraw(command);
				}
				for (DashboardCommandModel command : _tileCommands.values()) {
					withdraw(command);
				}
			});
		}

		/**
		 * Brings the commands adding a hidden tile in line with the current tiles.
		 */
		private void updateTileCommands() {
			boolean offered = _control.isAttached();
			for (Iterator<Map.Entry<String, DashboardCommandModel>> it = _tileCommands.entrySet().iterator(); it
				.hasNext();) {
				Map.Entry<String, DashboardCommandModel> command = it.next();
				if (!_entries.containsKey(command.getKey())) {
					it.remove();
					if (offered) {
						withdraw(command.getValue());
					}
				}
			}
			for (DashboardTiles.Entry entry : _entries.values()) {
				if (!_tileCommands.containsKey(entry.id())) {
					DashboardCommandModel command =
						DashboardCommandModel.showTileCommand(_control, entry.id(), entry.title());
					_tileCommands.put(entry.id(), command);
					if (offered) {
						offer(command);
					}
				}
			}
		}

		private void offer(DashboardCommandModel command) {
			command.attach();
			_scope.addCommand(command);
		}

		private void withdraw(DashboardCommandModel command) {
			_scope.removeCommand(command);
			command.detach();
		}
	}

	@SuppressWarnings("unchecked")
	private List<String> readPersonalOrder() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return List.of();
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
		return List.of();
	}

	/**
	 * The tile visibilities the current user stored, tile id to whether the tile is shown.
	 */
	private Map<String, Boolean> readPersonalVisibility() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return new HashMap<>();
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
	 * Stores the given tile visibilities deviating from their defaults.
	 */
	private void storePersonalVisibility(Map<String, Boolean> visibility) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			pc.setJSONValue(PC_VISIBILITY_KEY_PREFIX + _id, visibility.isEmpty() ? null : new LinkedHashMap<>(visibility));
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Failed to persist dashboard tile visibility for '" + _id + "'.", ex,
				DashboardElement.class);
		}
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
