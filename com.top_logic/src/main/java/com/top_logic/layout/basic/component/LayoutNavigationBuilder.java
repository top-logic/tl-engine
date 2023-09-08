/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.component;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.component.model.NoSingleSelectionModel;
import com.top_logic.layout.tree.breadcrumb.BreadcrumbDataOwner;
import com.top_logic.layout.tree.breadcrumb.ImmutableBreadcrumbData;
import com.top_logic.layout.tree.model.ComponentTreeModel;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} for a {@link BreadcrumbComponent} displaying the current navigtion state in
 * a layout.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class LayoutNavigationBuilder<C extends LayoutNavigationBuilder.Config<?>> extends AbstractConfiguredInstance<C>
		implements ModelBuilder {

	/**
	 * Configuration options for {@link LayoutNavigationBuilder}.
	 */
	public interface Config<I extends LayoutNavigationBuilder<?>> extends PolymorphicConfiguration<I> {

		/** @see #getRootComponent() */
		String ROOT_COMPONENT = "rootComponent";

		/**
		 * {@link ComponentName} where to start displaying navigation paths.
		 */
		@Name(ROOT_COMPONENT)
		ComponentName getRootComponent();
	}

	/**
	 * Creates a {@link LayoutNavigationBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public LayoutNavigationBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		ComponentName rootComponentName = getConfig().getRootComponent();
		LayoutComponent rootComponent = rootComponentName == null ? aComponent.getMainLayout()
			: aComponent.getComponentByName(rootComponentName);
		final ComponentTreeModel model = new ComponentTreeModel(rootComponent);

		return new ImmutableBreadcrumbData(model.getTree(), NoSingleSelectionModel.SINGLE_SELECTION_INSTANCE,
			model.getSelectionModel(), (BreadcrumbDataOwner) aComponent);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

}