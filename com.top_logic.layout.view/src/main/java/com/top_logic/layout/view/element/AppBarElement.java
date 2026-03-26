/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.nav.ReactAppBarControl;
import com.top_logic.layout.react.control.nav.ReactAppBarControl.AppBarVariant;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * UIElement that wraps {@link ReactAppBarControl}.
 *
 * <p>
 * Renders a top-level application bar with a title and optional variant/color configuration.
 * </p>
 */
public class AppBarElement implements UIElement {

	/**
	 * Configuration for {@link AppBarElement}.
	 */
	@TagName("app-bar")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(AppBarElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getVariant()}. */
		String VARIANT = "variant";

		/**
		 * The bar title.
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();

		/**
		 * The visual variant.
		 */
		@Name(VARIANT)
		AppBarVariant getVariant();
	}

	private final ResKey _title;

	private final AppBarVariant _variant;

	/**
	 * Creates a new {@link AppBarElement} from configuration.
	 */
	@CalledByReflection
	public AppBarElement(InstantiationContext context, Config config) {
		_title = config.getTitle();
		_variant = config.getVariant();
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		String title = _title != null ? Resources.getInstance().getString(_title) : "";
		return new ReactAppBarControl(context, title, _variant, null, List.of());
	}
}
