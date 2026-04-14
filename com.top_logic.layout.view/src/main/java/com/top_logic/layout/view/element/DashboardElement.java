/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.Tile;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.command.CommandScope;

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
 */
public class DashboardElement implements UIElement {

	private static final String PC_KEY_PREFIX = "dashboard.tileOrder.";

	/**
	 * Configuration for {@link DashboardElement}.
	 */
	@TagName("dashboard")
	public interface Config extends UIElement.Config {

		/** Config property name for {@link #getId()}. */
		String ID = "id";

		/** Config property name for {@link #getMinColWidth()}. */
		String MIN_COL_WIDTH = "min-col-width";

		/** Config property name for {@link #getTiles()}. */
		String TILES = "tiles";

		@Override
		@ClassDefault(DashboardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Stable id used as persistence key for personal tile ordering.
		 */
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
		 * The tiles. Represented as polymorphic configurations so that the
		 * default container (child elements) can hold {@code <tile>} entries.
		 */
		@Name(TILES)
		@com.top_logic.basic.config.annotation.DefaultContainer
		List<PolymorphicConfiguration<? extends TileElement>> getTiles();
	}

	private final String _id;

	private final String _minColWidth;

	private final List<TileElement> _tiles;

	/**
	 * Creates a new {@link DashboardElement} from configuration.
	 */
	@CalledByReflection
	public DashboardElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_minColWidth = config.getMinColWidth();
		_tiles = new ArrayList<>();
		for (PolymorphicConfiguration<? extends TileElement> tc : config.getTiles()) {
			TileElement tile = context.getInstance(tc);
			if (tile != null) {
				_tiles.add(tile);
			}
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<TileElement> ordered = applyPersonalOrder(_tiles);
		List<Tile> reactTiles = new ArrayList<>(ordered.size());
		for (TileElement t : ordered) {
			reactTiles.add(new Tile(t.getId(), t.getWidth(), t.getRowSpan(), t.createContentControl(context)));
		}
		ReactDashboardControl control =
			new ReactDashboardControl(context, _minColWidth, reactTiles, this::storePersonalOrder);

		contributeEditCommands(context, control);

		return control;
	}

	private void contributeEditCommands(ViewContext context, ReactDashboardControl control) {
		CommandScope scope = context.getCommandScope();
		if (scope == null) {
			return;
		}
		DashboardCommandModel edit = DashboardCommandModel.editCommand(control);
		DashboardCommandModel done = DashboardCommandModel.doneCommand(control);

		control.addAttachListener(() -> {
			edit.attach();
			done.attach();
			scope.addCommand(edit);
			scope.addCommand(done);
		});
		control.addDetachListener(() -> {
			scope.removeCommand(edit);
			scope.removeCommand(done);
			edit.detach();
			done.detach();
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
