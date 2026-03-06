/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactCardControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactCardControl}.
 *
 * <p>
 * Renders a lightweight content container with optional title and visual variant.
 * </p>
 */
public class CardElement extends ContainerElement {

	/**
	 * Configuration for {@link CardElement}.
	 */
	@TagName("card")
	public interface Config extends ContainerElement.Config {

		@Override
		@ClassDefault(CardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getTitle()}. */
		String TITLE = "title";

		/** Configuration name for {@link #getVariant()}. */
		String VARIANT = "variant";

		/** Configuration name for {@link #getPadding()}. */
		String PADDING = "padding";

		/**
		 * The card title, or empty for no header.
		 */
		@Name(TITLE)
		String getTitle();

		/**
		 * The visual variant: "outlined" or "elevated".
		 */
		@Name(VARIANT)
		@StringDefault("outlined")
		String getVariant();

		/**
		 * The content padding: "none", "compact", or "default".
		 */
		@Name(PADDING)
		@StringDefault("default")
		String getPadding();
	}

	private final String _title;

	private final String _variant;

	private final String _padding;

	/**
	 * Creates a new {@link CardElement} from configuration.
	 */
	@CalledByReflection
	public CardElement(InstantiationContext context, Config config) {
		super(context, config);
		_title = config.getTitle();
		_variant = config.getVariant();
		_padding = config.getPadding();
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		List<ViewControl> childControls = createChildControls(context);

		ReactControl content;
		if (childControls.size() == 1) {
			content = (ReactControl) childControls.get(0);
		} else {
			List<ReactControl> reactChildren = childControls.stream()
				.map(c -> (ReactControl) c)
				.collect(Collectors.toList());
			content = new ReactStackControl(reactChildren);
		}

		String title = _title != null && !_title.isEmpty() ? _title : null;
		return new ReactCardControl(title, _variant, _padding, List.of(), content);
	}
}
