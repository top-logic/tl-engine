/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ToolbarControl;
import com.top_logic.layout.react.control.layout.ReactPanelControl;
import com.top_logic.layout.react.control.layout.ReactToolbarControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * UIElement that wraps {@link ReactPanelControl}.
 *
 * <p>
 * Renders a titled panel with a toolbar header. Commands configured in the {@code <commands>}
 * section are available to child elements via the command scope and toolbar-placed commands are
 * automatically rendered as clique-grouped toolbar buttons.
 * </p>
 */
public class PanelElement extends CommandScopeElement {

	/**
	 * Configuration for {@link PanelElement}.
	 */
	@TagName("panel")
	public interface Config extends CommandScopeElement.Config {

		@Override
		@ClassDefault(PanelElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getFill()}. */
		String FILL = "fill";

		/**
		 * The panel title displayed in the toolbar header.
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();

		/**
		 * Whether the panel fills the bounded height of its container instead of growing with its
		 * content.
		 *
		 * <p>
		 * Enable this for a panel whose single child scrolls internally - in particular a table
		 * ({@link com.top_logic.layout.react.control.table.TableViewControl}), which only bounds its
		 * scroll viewport (and virtualizes) when its container has a definite height. A plain
		 * (non-filling) panel grows with its content, so a large table would overflow and scroll the
		 * surrounding tab rather than itself.
		 * </p>
		 */
		@Name(FILL)
		boolean getFill();
	}

	private final ResKey _title;

	private final boolean _fill;

	/**
	 * Creates a new {@link PanelElement} from configuration.
	 */
	@CalledByReflection
	public PanelElement(InstantiationContext context, Config config) {
		super(context, config);
		_title = config.getTitle();
		_fill = config.getFill();
	}

	@Override
	protected ToolbarControl createChromeControl(ViewContext context, ReactControl content,
			ReactToolbarControl toolbar, ReactToolbarControl buttonBar) {
		String title = _title != null ? Resources.getInstance().getString(_title) : "";
		ReactPanelControl panel = new ReactPanelControl(context, title, content, toolbar, buttonBar, false, false, false);
		panel.setFill(_fill);
		return panel;
	}
}
