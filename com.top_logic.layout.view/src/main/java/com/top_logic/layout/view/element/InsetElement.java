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
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.ContainerElement;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that insets its content from the surrounding container border.
 *
 * <p>
 * In the spacing model, containers are flush by default and content owns its breathing room. An
 * {@code <inset>} is dropped into a view wherever content would otherwise glue to the border -
 * e.g. around a stack/grid of cards, or around explanatory text in a dialog.
 * </p>
 *
 * @implNote The tag is {@code <inset>} rather than {@code <padding>} because {@code padding} already
 *           names a content property (e.g. on {@code <card>}), which the tag resolver would treat as
 *           ambiguous.
 */
public class InsetElement extends ContainerElement {

	/**
	 * Configuration for {@link InsetElement}.
	 */
	@TagName("inset")
	public interface Config extends ContainerElement.Config {

		@Override
		@ClassDefault(InsetElement.class)
		Class<? extends UIElement> getImplementationClass();
	}

	/**
	 * Creates a new {@link InsetElement} from configuration.
	 */
	@CalledByReflection
	public InsetElement(InstantiationContext context, Config config) {
		super(context, config);
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

		return new ReactInsetControl(context, content);
	}
}
