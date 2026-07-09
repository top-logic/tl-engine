/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.util.model.ModelService;

/**
 * {@link ListModelBuilder} for a table that displays the application's change log.
 */
public class ChangeLogListModelBuilder extends AbstractConfiguredInstance<ChangeLogListModelBuilder.Config<?>>
		implements ListModelBuilder {

	/**
	 * Configuration options for {@link ChangeLogListModelBuilder}.
	 */
	@DisplayOrder({
		Config.MAX_TIME,
		Config.MAX_ENTRIES,
		Config.ALL_USERS,
		Config.INCLUDE_TECHNICAL_CHANGES,
		Config.ROOT_REQUIRED,
		Config.EXCLUDED_MODULES,
	})
	public interface Config<I extends ChangeLogListModelBuilder>
			extends PolymorphicConfiguration<I>, ChangeLogOptions {

		/** Configuration name for {@link #getMaxEntries()}. */
		String MAX_ENTRIES = "max-entries";

		/** Configuration name for {@link #getRootRequired()}. */
		String ROOT_REQUIRED = "root-required";

		/**
		 * The value specifies the maximum number of entries to be displayed.
		 * 
		 * <p>
		 * A value <code>&lt;= 0</code> means all entries.
		 * </p>
		 */
		@IntDefault(30)
		@Name(MAX_ENTRIES)
		@Label("Number entries")
		int getMaxEntries();

		/**
		 * Whether the builder requires a non-null business model to produce any entries.
		 *
		 * <p>
		 * When {@code true} and no model is provided, the returned list is empty (rather than the
		 * un-scoped, application-wide change log). This is the useful default for a log panel
		 * whose model is driven by a selection channel: with no selection, the panel stays
		 * empty.
		 * </p>
		 */
		@Name(ROOT_REQUIRED)
		boolean getRootRequired();
	}

	/**
	 * Creates a {@link ChangeLogListModelBuilder} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ChangeLogListModelBuilder(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || aModel instanceof TLObject;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		Config<?> config = getConfig();
		if (businessModel == null && config.getRootRequired()) {
			return Collections.emptyList();
		}

		TLModel model = ModelService.getApplicationModel();
		KnowledgeBase kb = model.tKnowledgeBase();

		ChangeLogBuilder builder = new ChangeLogBuilder(kb, model)
			.applyOptions(config)
			.setNumberEntries(config.getMaxEntries());

		if (businessModel instanceof TLObject root) {
			builder.setFilter(new SubtreeFilter(root));
		}

		return builder.build();
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object candidate) {
		return null;
	}
}
