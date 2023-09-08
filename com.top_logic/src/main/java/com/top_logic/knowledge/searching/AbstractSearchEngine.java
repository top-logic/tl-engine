/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.util.ResKey;
import com.top_logic.util.Resources;

/**
 * Abstract {@link SearchEngine} implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSearchEngine<C extends AbstractSearchEngine.Config<?>>
		extends AbstractConfiguredInstance<C> implements SearchEngine {

	/**
	 * Typed configuration interface definition for {@link AbstractSearchEngine}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config<I extends AbstractSearchEngine<?>> extends PolymorphicConfiguration<I> {

		/**
		 * {@link ResKey} to for the {@link SearchEngine#getDisplayName() display name} of the
		 * {@link SearchEngine}.
		 */
		@Mandatory
		ResKey getDisplayName();

		/**
		 * Setter for {@link #getDisplayName()}.
		 */
		void setDisplayName(ResKey key);

	}

	/**
	 * Create a {@link AbstractSearchEngine}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public AbstractSearchEngine(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public void cancel(SearchResultSet aSet) {
		// search is executed in same thread. No cancel possible.
	}

	@Override
	public String getDisplayName() {
		return Resources.getInstance().getString(getConfig().getDisplayName());
	}

}

