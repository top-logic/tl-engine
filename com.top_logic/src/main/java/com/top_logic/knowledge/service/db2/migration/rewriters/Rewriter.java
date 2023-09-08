/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import java.util.List;
import java.util.Set;

import com.google.inject.Inject;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.filter.TrueFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableAndFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.event.convert.ConfiguredRewritingEventVisitor;
import com.top_logic.knowledge.event.convert.RewritingEventVisitor;
import com.top_logic.knowledge.event.convert.TypesConfig;
import com.top_logic.knowledge.service.db2.migration.Migration;
import com.top_logic.knowledge.service.db2.migration.config.FilterConfig;

/**
 * {@link RewritingEventVisitor} which rewrites only events of certain types which match a
 * configured filter.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class Rewriter extends ConfiguredRewritingEventVisitor<Rewriter.Config> {

	/**
	 * Configuration of a {@link Rewriter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends ConfiguredRewritingEventVisitor.Config<Rewriter>, FilterConfig, TypesConfig {

		// sum interface

	}

	private final Set<String> _typeNames;

	private final TypedFilter _filter;

	private Migration _migration;

	/**
	 * Creates a {@link Rewriter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Rewriter(InstantiationContext context, Config config) {
		super(context, config);
		_typeNames = config.getTypeNames();
		_filter = toAndFilter(context, config.getFilters());
	}

	/**
	 * Conjuncts all given {@link TypedFilter}.
	 */
	public static TypedFilter toAndFilter(InstantiationContext context,
			List<PolymorphicConfiguration<? extends TypedFilter>> filters) {
		if (filters.isEmpty()) {
			return TrueFilter.INSTANCE;
		}
		if (filters.size() == 1) {
			return context.getInstance(filters.get(0));
		}
		ConfigurableAndFilter.Config andConfig = TypedConfiguration.newConfigItem(ConfigurableAndFilter.Config.class);
		andConfig.getFilters().addAll(filters);
		return context.getInstance(andConfig);
	}

	/**
	 * Initialises {@link #migration()} with the {@link Migration} which uses this {@link Rewriter}.
	 */
	@Inject
	public void init(Migration migration) {
		_migration = migration;
	}

	/**
	 * The {@link Migration} which uses this {@link Rewriter}.
	 */
	public Migration migration() {
		return _migration;
	}

	@Override
	public final Object visitDelete(ItemDeletion event, Void arg) {
		if (isIgnored(event)) {
			return APPLY_EVENT;
		}
		return processDelete(event);
	}

	/**
	 * Rewrites the given {@link ItemDeletion}.
	 */
	protected Object processDelete(ItemDeletion event) {
		return super.visitDelete(event, none);
	}

	@Override
	public final Object visitCreateObject(ObjectCreation event, Void arg) {
		if (isIgnored(event)) {
			return APPLY_EVENT;
		}
		return processCreateObject(event);
	}

	/**
	 * Rewrites the given {@link ObjectCreation}.
	 */
	protected Object processCreateObject(ObjectCreation event) {
		return super.visitCreateObject(event, none);
	}

	@Override
	public final Object visitUpdate(ItemUpdate event, Void arg) {
		if (isIgnored(event)) {
			return APPLY_EVENT;
		}
		return processUpdate(event);
	}

	/**
	 * Rewrites the given {@link ItemUpdate}.
	 */
	protected Object processUpdate(ItemUpdate event) {
		return super.visitUpdate(event, none);
	}

	private boolean isIgnored(ItemEvent event) {
		return ignoredByType(event) || _filter.matches(event) != FilterResult.TRUE;
	}

	private boolean ignoredByType(ItemEvent event) {
		if (_typeNames.isEmpty()) {
			return false;
		}
		return !_typeNames.contains(event.getObjectType().getName());
	}

}
