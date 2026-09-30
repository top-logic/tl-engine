/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.view.component.table;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.element.layout.grid.GridBuilder;
import com.top_logic.element.layout.grid.GridComponent;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.tool.boundsec.SecurityObjectProvider;

/**
 * {@link GridComponent} implementing {@link FilterAwareComponent}.
 * 
 * @author     <a href="mailto:kbu@top-logic.com">kbu</a>
 */
public class FilterAwareGridComponent extends GridComponent implements FilterAwareComponent {
	
	public interface Config extends GridComponent.Config {
		@Override
		@FormattedDefault("path:master.currentObject")
		PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();
	}

	/**
	 * Creates a {@link FilterAwareGridComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FilterAwareGridComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
	}

	@Override
	public FilterAwareModelBuilder getModelBuilder() {
		ModelBuilder builder = ((GridBuilder<?>) getBuilder()).unwrap();
		if (builder instanceof FilterAwareModelBuilder) {
			return (FilterAwareModelBuilder) builder;
		}
		return null;
	}
	
}
