/*
 * Copyright (c) 2025 Business Operation Systems GmbH. All Rights Reserved
 */
package com.top_logic.model.search.providers;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragmentProvider;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.ColumnConfigurator;

/**
 * Adds additional headers to a column configuration while preserving existing ones.
 *
 * @author <a href="mailto:Jonathan.Hüsing@top-logic.com">Jonathan Hüsing</a>
 */
@InApp
public class ColumnAdditionalHeaders
		extends AbstractConfiguredInstance<ColumnAdditionalHeaders.Config<?>>
		implements ColumnConfigurator {

	/**
	 * Configuration options for {@link ColumnAdditionalHeaders}.
	 */
	@DisplayOrder({
		Config.ADDITIONAL_HEADERS
	})
	public interface Config<I extends ColumnAdditionalHeaders>
			extends PolymorphicConfiguration<I> {

		/**
		 * Configuration key for additional headers list.
		 * 
		 * @see #getAdditionalHeaders()
		 */
		String ADDITIONAL_HEADERS = "additionalHeaders";

		/**
		 * List of HTML fragment providers for additional column headers.
		 */
		@Name(ADDITIONAL_HEADERS)
		@Mandatory
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<HTMLFragmentProvider>> getAdditionalHeaders();
	}

	private List<HTMLFragmentProvider> _headers;


	/**
	 * Creates a {@link ColumnAdditionalHeaders} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ColumnAdditionalHeaders(InstantiationContext context, Config<?> config) {
		super(context, config);

		_headers = TypedConfiguration.getInstanceList(context, config.getAdditionalHeaders());
	}

	@Override
	public void adapt(ColumnConfiguration col) {
		// save current Additional Headers in a new List
		List<HTMLFragmentProvider> currentHeaders = new ArrayList<>(col.getAdditionalHeaders());
		// add new Additional Headers to them
		currentHeaders.addAll(_headers);
		// overwrite the Additional Headers of the column with the old and new ones
		col.setAdditionalHeaders(currentHeaders);
	}

}
