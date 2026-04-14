/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.TileWidth;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * A single tile in a {@link DashboardElement dashboard}.
 *
 * <p>
 * Exactly one inner {@link UIElement} is rendered inside the tile. The tile
 * carries layout metadata: a stable {@link Config#getId() id} (used as
 * persistence key for reordering), a relative {@link Config#getWidth() width}
 * fraction and an optional {@link Config#getRowSpan() row span}.
 * </p>
 */
public class TileElement implements UIElement {

	/**
	 * Configuration for {@link TileElement}.
	 */
	@TagName("tile")
	public interface Config extends UIElement.Config {

		/** Config property name for {@link #getId()}. */
		String ID = "id";

		/** Config property name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Config property name for {@link #getRowSpan()}. */
		String ROW_SPAN = "row-span";

		/** Config property name for {@link #getContent()}. */
		String CONTENT = "content";

		@Override
		@ClassDefault(TileElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Stable id identifying this tile. Used as the persistence key when
		 * users reorder tiles via drag-and-drop.
		 */
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * The tile's relative width as a fraction of available columns.
		 */
		@Name(WIDTH)
		@Mandatory
		TileWidth getWidth();

		/**
		 * The number of grid rows this tile spans. Defaults to 1.
		 */
		@Name(ROW_SPAN)
		@IntDefault(1)
		int getRowSpan();

		/**
		 * The {@link UIElement} rendered inside this tile.
		 */
		@Name(CONTENT)
		@DefaultContainer
		PolymorphicConfiguration<? extends UIElement> getContent();
	}

	private final String _id;

	private final TileWidth _width;

	private final int _rowSpan;

	private final UIElement _content;

	/**
	 * Creates a new {@link TileElement} from configuration.
	 */
	@CalledByReflection
	public TileElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_width = config.getWidth();
		_rowSpan = Math.max(1, config.getRowSpan());
		_content = context.getInstance(config.getContent());
	}

	/** The stable tile id. */
	public String getId() {
		return _id;
	}

	/** The tile's relative width. */
	public TileWidth getWidth() {
		return _width;
	}

	/** The tile's row span. */
	public int getRowSpan() {
		return _rowSpan;
	}

	/**
	 * Creates the inner control of this tile.
	 */
	public ReactControl createContentControl(ViewContext context) {
		IReactControl inner = _content.createControl(context);
		return (ReactControl) inner;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// Tiles are rendered by DashboardElement; direct rendering is unsupported.
		throw new UnsupportedOperationException("<tile> must be a child of <dashboard>.");
	}
}
