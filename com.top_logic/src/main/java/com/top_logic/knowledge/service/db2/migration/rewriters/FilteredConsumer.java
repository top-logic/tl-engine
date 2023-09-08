/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.function.Consumer;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.knowledge.event.ItemChange;

/**
 * {@link Consumer} delegating to a configured {@link Consumer} when a configured {@link Filter}
 * matches.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FilteredConsumer<T extends ItemChange> extends AbstractConfiguredInstance<FilteredConsumer.Config<T>>
		implements Consumer<T> {

	/**
	 * Typed configuration interface definition for {@link FilteredConsumer}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<T extends ItemChange> extends PolymorphicConfiguration<FilteredConsumer<T>> {

		/**
		 * The algorithm that rewrites the {@link ItemChange}.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends Consumer<? super T>> getConsumer();

		/**
		 * Setter for {@link #getConsumer()}.
		 */
		void setConsumer(PolymorphicConfiguration<? extends Consumer<? super T>> consumer);

		/**
		 * The algorithm that rewrites the {@link ItemChange}.
		 */
		@Mandatory
		PolymorphicConfiguration<? extends Filter<? super T>> getFilter();

		/**
		 * Setter for {@link #getConsumer()}.
		 */
		void setFilter(PolymorphicConfiguration<? extends Filter<? super T>> filter);

	}

	private Consumer<? super T> _consumer;

	private Filter<? super T> _filter;

	/**
	 * Create a {@link FilteredConsumer}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public FilteredConsumer(InstantiationContext context, Config<T> config) {
		super(context, config);
		_filter = context.getInstance(config.getFilter());
		_consumer = context.getInstance(config.getConsumer());
	}

	@Override
	public void accept(T t) {
		if (_filter.accept(t)) {
			_consumer.accept(t);
		}
	}

}

