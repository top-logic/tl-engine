/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.view.channel.ChannelBindingConfig;
import com.top_logic.layout.view.channel.ViewChannel;

/**
 * A {@link UIElement} that embeds another view as an isolated component.
 *
 * <p>
 * Each {@code <view-ref>} creates an independent instance of the referenced view with its own
 * {@link ViewContext} and channel namespace. Communication across the boundary is established via
 * explicit {@code <bind>} elements that share the parent's channel instance with the child.
 * </p>
 *
 * <p>
 * The referenced view is loaded lazily at {@link #createControl(ViewContext)} time, not at config
 * parse time. This naturally supports multiple instances and recursive usage.
 * </p>
 *
 * <p>
 * Example:
 * </p>
 *
 * <pre>
 * &lt;view-ref view="customers/detail.view.xml"&gt;
 *   &lt;bind channel="item" to="selectedCustomer"/&gt;
 * &lt;/view-ref&gt;
 * </pre>
 */
public class ReferenceElement implements UIElement {

	/**
	 * Configuration for {@link ReferenceElement}.
	 */
	@TagName("view-ref")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(ReferenceElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #getBindings()}. */
		String BINDINGS = "bindings";

		/**
		 * Path to the referenced {@code .view.xml} file, relative to {@code /WEB-INF/views/}.
		 */
		@Name(VIEW)
		@Mandatory
		String getView();

		/**
		 * Channel bindings from the parent scope to the referenced view's channels.
		 *
		 * <p>
		 * Each binding maps a channel declared in the referenced view to a channel in the parent's
		 * scope. At runtime, the parent's channel instance is registered directly in the child's
		 * context - both sides share the same object.
		 * </p>
		 */
		@Name(BINDINGS)
		@DefaultContainer
		List<ChannelBindingConfig> getBindings();
	}

	private final String _viewPath;

	private final List<ChannelBindingConfig> _bindings;

	/**
	 * Creates a new {@link ReferenceElement} from configuration.
	 */
	@CalledByReflection
	public ReferenceElement(InstantiationContext context, Config config) {
		_viewPath = config.getView();
		_bindings = config.getBindings();
	}

	@Override
	public IReactControl createControl(ViewContext parentContext) {
		String fullPath = ViewLoader.VIEW_BASE_PATH + _viewPath;

		ViewElement referencedView;
		try {
			referencedView = ViewLoader.getOrLoadView(fullPath);
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to load referenced view: " + fullPath, ex);
		}

		// Create isolated child context.
		ViewContext childContext = new DefaultViewContext(parentContext.getReactContext());

		// Pre-bind parent channels into the child context.
		for (ChannelBindingConfig binding : _bindings) {
			ViewChannel parentChannel = parentContext.resolveChannel(binding.getTo());
			childContext.registerChannel(binding.getChannel(), parentChannel);
		}

		return referencedView.createControl(childContext);
	}
}
