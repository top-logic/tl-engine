/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.contribution.ViewContributionService;

/**
 * UIElement placeholder whose content is supplied by the {@link ViewContributionService} for a named
 * extension point.
 *
 * <p>
 * Instead of embedding a fixed view, a container embeds an {@code <extension-point id="..."/>}. The
 * single {@link UIElement} registered for that id at the {@link ViewContributionService} provides the
 * content. Because that registration lives in the mergeable application configuration, any module
 * refines it - adding, positioning or overriding parts - through the standard typed-configuration
 * merge, without the embedding view depending on the contributor.
 * </p>
 *
 * @implNote The content configuration is resolved once from
 *           {@link ViewContributionService#getExtensionContent(String)} and instantiated in the
 *           embedding view's context.
 */
public class ExtensionPointElement implements UIElement {

	/**
	 * Configuration for {@link ExtensionPointElement}.
	 */
	@TagName("extension-point")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ExtensionPointElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getId()}. */
		String ID = "id";

		/**
		 * The extension-point id whose registered content is rendered at this position.
		 */
		@Name(ID)
		@Mandatory
		String getId();
	}

	private final UIElement _content;

	/**
	 * Creates a new {@link ExtensionPointElement} from configuration.
	 */
	@CalledByReflection
	public ExtensionPointElement(InstantiationContext context, Config config) {
		PolymorphicConfiguration<? extends UIElement> contentConfig =
			ViewContributionService.getInstance().getExtensionContent(config.getId());
		_content = contentConfig == null ? null : context.getInstance(contentConfig);
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		if (_content == null) {
			// No module registered content for this extension point.
			return new ReactStackControl(context, List.of());
		}
		return _content.createControl(context);
	}

}
