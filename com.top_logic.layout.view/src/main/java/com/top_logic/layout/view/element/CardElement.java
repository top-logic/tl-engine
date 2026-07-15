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
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactCardControl;
import com.top_logic.layout.react.control.layout.ReactCardControl.CardPadding;
import com.top_logic.layout.react.control.layout.ReactCardControl.CardVariant;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

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
		 * The card title, or {@code null} for no header.
		 */
		@Name(TITLE)
		@Nullable
		ResKey getTitle();

		/**
		 * The visual variant.
		 */
		@Name(VARIANT)
		CardVariant getVariant();

		/**
		 * The content padding.
		 */
		@Name(PADDING)
		CardPadding getPadding();
	}

	private final ResKey _title;

	private final CardVariant _variant;

	private final CardPadding _padding;

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
	public IReactControl createControl(ViewContext context) {
		List<IReactControl> childControls = createChildControls(context);

		ReactControl content;
		if (childControls.size() == 1) {
			content = (ReactControl) childControls.get(0);
		} else {
			List<ReactControl> reactChildren = childControls.stream()
				.map(c -> (ReactControl) c)
				.collect(Collectors.toList());
			content = new ReactStackControl(context, reactChildren);
		}

		String title = _title != null ? Resources.getInstance().getString(_title) : null;
		return new ReactCardControl(context, title, _variant, _padding, List.of(), content);
	}
}
