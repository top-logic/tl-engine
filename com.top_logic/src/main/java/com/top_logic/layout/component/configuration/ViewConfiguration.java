/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.layout.View;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Instances of {@link ViewConfiguration} are used to create {@link View}s for
 * {@link LayoutComponent}s which are configured in the layout XML files.
 * 
 * <p>
 * For one {@link LayoutComponent}, the {@link #getName() configured view names}
 * must be unique.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ViewConfiguration {

    /**
     * Configuration interface for {@link ViewConfiguration}s.
     * 
     * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
     */
	public interface Config<I extends ViewConfiguration> extends PolymorphicConfiguration<I>, NamedConfigMandatory {
		// Pure sum interface
	}
	
	/**
	 * Creates the {@link View} for the given {@link LayoutComponent}.
	 * 
	 * <p>
	 * This method is called from its {@link LayoutComponent} after the component is resolved. The
	 * created {@link View} is added to {@link LayoutComponent#getConfiguredViews()}.
	 * </p>
	 * 
	 * @param component
	 *        The context {@link LayoutComponent} that owns the created {@link View}.
	 */
	public HTMLFragment createView(LayoutComponent component);

	/**
	 * Returns the name for this view configuration.
	 */
	public String getName();

}
