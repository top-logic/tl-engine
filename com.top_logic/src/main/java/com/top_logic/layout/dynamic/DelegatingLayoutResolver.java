/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dynamic;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * {@link LayoutResolver}, that delegates layout resolving to a list of configured inner
 * {@link LayoutResolver}s, so that the first {@link LayoutResolver} in order, that can handle the
 * given context model, will be used for layout resolving.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class DelegatingLayoutResolver extends LayoutResolver {

	/**
	 * Configuration of {@link DelegatingLayoutResolver}.
	 */
	public interface Config extends LayoutResolver.Config {

		/** Property name of {@link #getLayoutResolvers()} */
		public static final String LAYOUT_RESOLVERS_PROPERTY = "layoutResolvers";

		/**
		 * list of {@link LayoutResolver}s, that will be used to resolve layouts out of
		 *         given context model.
		 */
		@Name(LAYOUT_RESOLVERS_PROPERTY)
		@InstanceFormat
		@Mandatory
		List<LayoutResolver> getLayoutResolvers();
	}

	private List<LayoutResolver> _layoutResolvers;

	/**
	 * Creates a {@link DelegatingLayoutResolver} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DelegatingLayoutResolver(InstantiationContext context, Config config) {
		super(context, config);
		_layoutResolvers = config.getLayoutResolvers();
	}

	@Override
	public boolean canResolve(Object model) {
		for (LayoutResolver layoutResolver : _layoutResolvers) {
			if (layoutResolver.canResolve(model)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String getLayoutName(Object model) {
		for (LayoutResolver layoutResolver : _layoutResolvers) {
			if (layoutResolver.canResolve(model)) {
				return layoutResolver.getLayoutName(model);
			}
		}
		IllegalArgumentException exception = new IllegalArgumentException(createErrorMessage());
		Logger.error(
			"No appropriate layout resolver available to resolve layout! To prevent this error, check for appropriate layout resolvers before layout resolve attempt.",
			exception, DelegatingLayoutResolver.class);
		throw exception;
	}

	private String createErrorMessage() {
		StringBuilder errorMessage = new StringBuilder();
		errorMessage.append("None of the following configured layout resolvers is able to resolve the layout: [");
		for (int i = 0; i < _layoutResolvers.size(); i++) {
			LayoutResolver layoutResolver = _layoutResolvers.get(i);
			if (i > 0) {
				errorMessage.append(", ");
			}
			errorMessage.append(layoutResolver.getClass());
		}
		errorMessage.append("]");
		return errorMessage.toString();
	}

}
