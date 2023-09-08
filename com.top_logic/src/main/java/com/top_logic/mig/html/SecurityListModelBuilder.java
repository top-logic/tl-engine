/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.col.FilterUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.security.SecurityFilter;
import com.top_logic.layout.table.component.BuilderComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilderProxy} that filters the result list of an inner {@link ModelBuilder} by
 * applying security.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityListModelBuilder extends ListModelBuilderProxy
		implements ConfiguredInstance<SecurityListModelBuilder.Config> {

	/**
	 * Configuration of a {@link SecurityListModelBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<SecurityListModelBuilder> {

		/**
		 * {@link ListModelBuilder} to retrieve un-filtered list.
		 */
		@Mandatory
		@Name(BuilderComponent.MODEL_BUILDER_ELEMENT)
		PolymorphicConfiguration<ListModelBuilder> getModelBuilder();

		/**
		 * Configuration of the {@link SecurityFilter} used to filter the list model.
		 */
		@Mandatory
		SecurityFilter.Config<? extends SecurityFilter> getFilter();

	}

	private final Config _config;

	private final SecurityFilter _filter;

	private final Protocol _logProtocol = new LogProtocol(SecurityListModelBuilder.class);

	/**
	 * Creates a new {@link SecurityListModelBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link SecurityListModelBuilder}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public SecurityListModelBuilder(InstantiationContext context, Config config) throws ConfigurationException {
		super(context.getInstance(config.getModelBuilder()));
		_config = config;
		_filter = context.getInstance(config.getFilter());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		List<?> list = (List<?>) super.getModel(businessModel, aComponent);
		return FilterUtil.filterList(getInitializedFilter(aComponent), list);
	}

	private SecurityFilter getInitializedFilter(LayoutComponent component) {
		_filter.ensureDelegationDestination(_logProtocol, component.getMainLayout());
		return _filter;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		boolean supportsListElement = super.supportsListElement(contextComponent, listElement);
		if (!supportsListElement) {
			return false;
		}
		return getInitializedFilter(contextComponent).accept(listElement);
	}

}

