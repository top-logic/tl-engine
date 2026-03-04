/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.Control;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;

/**
 * The mandatory root element of every {@code .view.xml} file.
 *
 * <p>
 * Establishes the scope boundary for a view. In the future, this is where channel declarations
 * and view-level configuration will be defined.
 * </p>
 */
public class ViewElement implements UIElement {

	/**
	 * Configuration for {@link ViewElement}.
	 */
	@TagName("view")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ViewElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getContent()}. */
		String CONTENT = "content";

		/**
		 * The root content element of this view.
		 *
		 * <p>
		 * Exactly one element is expected. The list type enables {@code @TagName} resolution so that
		 * short element names (e.g. {@code <app-shell>}) can be used directly inside {@code <view>}.
		 * </p>
		 */
		@Name(CONTENT)
		@DefaultContainer
		List<PolymorphicConfiguration<? extends UIElement>> getContent();
	}

	private final List<UIElement> _content;

	/**
	 * Creates a new {@link ViewElement} from configuration.
	 */
	@CalledByReflection
	public ViewElement(InstantiationContext context, Config config) {
		List<PolymorphicConfiguration<? extends UIElement>> contentList = config.getContent();
		if (contentList.isEmpty()) {
			context.error("View element must have at least one content element.");
			_content = List.of();
		} else {
			_content = contentList.stream()
				.map(context::getInstance)
				.collect(Collectors.toList());
		}
	}

	@Override
	public Control createControl(ViewContext context) {
		if (_content.size() == 1) {
			return _content.get(0).createControl(context);
		}
		List<ReactControl> children = _content.stream()
			.map(e -> (ReactControl) e.createControl(context))
			.collect(Collectors.toList());
		return new ReactStackControl(children);
	}
}
