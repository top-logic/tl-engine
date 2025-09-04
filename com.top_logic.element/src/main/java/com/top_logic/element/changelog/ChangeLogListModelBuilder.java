/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.layout.meta.TLStructuredTypeFormBuilder;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLModel;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.util.TLContext;
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
		Config.ALL_USERS,
		Config.INCLUDE_TECHNICAL_CHANGES,
		Config.EXCLUDED_MODULES,
	})
	public interface Config<I extends ChangeLogListModelBuilder> extends PolymorphicConfiguration<I> {

		/** Configuration name for {@link #getAllUsers()}. */
		String ALL_USERS = "all-users";

		/** Configuration name for {@link #getIncludeTechnicalChanges()}. */
		String INCLUDE_TECHNICAL_CHANGES = "include-technical-changes";

		/** Configuration name for {@link #getMaxTime()}. */
		String MAX_TIME = "max-time";

		/** Configuration name for {@link #getExcludedModules()}. */
		String EXCLUDED_MODULES = "excluded-modules";

		/**
		 * Whether to build a change log for all users instead only for the currently logged-in
		 * user.
		 */
		@Name(ALL_USERS)
		boolean getAllUsers();

		/**
		 * Whether to include technical changes.
		 */
		@Name(INCLUDE_TECHNICAL_CHANGES)
		boolean getIncludeTechnicalChanges();

		/**
		 * How long to look into the past.
		 * 
		 * <p>
		 * A value of <code>0</code> means unlimited.
		 * </p>
		 */
		@FormattedDefault("7d")
		@Format(MillisFormat.class)
		@Name(MAX_TIME)
		long getMaxTime();

		/**
		 * Modules in this list are not observed. I.e. no changes are reported for elements whose
		 * type is part of one of this module.
		 */
		@Format(TLModelPartRef.CommaSeparatedTLModelPartRefs.class)
		@Options(fun = TLStructuredTypeFormBuilder.EditModel.AllModules.class, mapping = TLModelPartRef.PartMapping.class)
		@Name(EXCLUDED_MODULES)
		List<TLModelPartRef> getExcludedModules();
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
		return aModel == null;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		TLModel model = ModelService.getApplicationModel();
		KnowledgeBase kb = model.tKnowledgeBase();

		Config<?> config = getConfig();

		HistoryManager hm = kb.getHistoryManager();

		Revision startRev;
		long maxTime = config.getMaxTime();
		if (maxTime > 0) {
			long startTime = System.currentTimeMillis() - maxTime;
			startRev = hm.getRevisionAt(startTime);
			if (startRev.getCommitNumber() < 1) {
				startRev = hm.getRevision(1);
			}
		} else {
			startRev = hm.getRevision(1);
		}

		return new ChangeLogBuilder(kb, model)
			.setAuthor(config.getAllUsers() ? null : TLContext.currentUser())
			.setStartRev(startRev)
			.setExcludedModules(getConfig().getExcludedModules()
				.stream()
				.map(TLModelPartRef::qualifiedName)
				.collect(Collectors.toSet()))
			.build();
	}

	@Override
	public boolean supportsListElement(LayoutComponent component, Object candidate) {
		return candidate instanceof com.top_logic.element.changelog.model.ChangeSet;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent component, Object candidate) {
		return null;
	}
}
