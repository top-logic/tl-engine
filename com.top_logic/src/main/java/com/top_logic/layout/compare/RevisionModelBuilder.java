/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} returning a list of last {@link Revision}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionModelBuilder extends AbstractConfiguredInstance<RevisionModelBuilder.Config>
		implements ListModelBuilder {

	/**
	 * Configuration for {@link RevisionModelBuilder}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<RevisionModelBuilder> {

		/** Name of {@link RevisionModelBuilder.Config#getNumberRevisions()} */
		String NUMBER_REVISIONS = "number-revisions";

		/** Name of {@link RevisionModelBuilder.Config#isIncludeCurrent()} */
		String INCLUDE_CURRENT = "include-current";

		/**
		 * Number of revisions to fetch.
		 */
		@IntDefault(10000)
		@Name(NUMBER_REVISIONS)
		int getNumberRevisions();

		/**
		 * Setter for {@link #getNumberRevisions()}.
		 */
		void setNumberRevisions(int value);

		/**
		 * Whether {@link Revision#CURRENT} should be included.
		 * 
		 */
		@BooleanDefault(true)
		@Name(INCLUDE_CURRENT)
		boolean isIncludeCurrent();

		/**
		 * Setter for {@link #isIncludeCurrent()}.
		 */
		void setIncludeCurrent(boolean value);

	}

	/**
	 * Creates a new {@link RevisionModelBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link RevisionModelBuilder}.
	 */
	public RevisionModelBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return getAllRevisions(getConfig());
	}

	private List<Revision> getAllRevisions(Config config) {
		List<Revision> lastRevisions = HistoryUtils.getLastRevisions(config.getNumberRevisions());
		if (config.isIncludeCurrent()) {
			lastRevisions.add(0, Revision.CURRENT);
		}
		return lastRevisions;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof Revision;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}

