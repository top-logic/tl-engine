/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.config;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.InstanceDefault;
import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.migration.FuzzyTableNameMapping;
import com.top_logic.knowledge.service.db2.migration.TypeMapping;
import com.top_logic.knowledge.service.db2.migration.db.RowTransformer;

/**
 * Configuration options for a replay migration.
 * 
 * <p>
 * In a replay migration, application history is extracted to events. These events are passed to
 * {@link EventRewriter}s and the transformed events are applied to an empty database building a new
 * history.
 * </p>
 * 
 * @see #getSteps()
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ReplayConfig extends ConfigurationItem {

	/**
	 * Name of the root tag of a {@link ReplayConfig} XML file.
	 */
	String TAG_NAME = "migration";

	/**
	 * @see #getForceReplay()
	 */
	String FORCE_REPLAY = "force-replay";

	/**
	 * Whether a standard replay should be performed, even if no {@link #getSteps()} or
	 * {@link #getTransformerSteps()} are given.
	 * 
	 * <p>
	 * This can be useful, if a schema migration is performed that is automatically done by just
	 * performing a dump-load operation, e.g. moving an attribute from flex storage to row storage
	 * or vice versa.
	 * </p>
	 */
	@Name(FORCE_REPLAY)
	boolean getForceReplay();

	/**
	 * Mapping of type names from the dump to the target system.
	 * 
	 * <p>
	 * Since the dump is read in the context of the target system's type system, all type names must
	 * be resolvable in the target type system. If types were renamed, the renaming must happen in
	 * the dump reader and cannot be realized through a custom rewriter.
	 * </p>
	 */
	@InstanceFormat
	@ImplementationClassDefault(FuzzyTableNameMapping.class)
	@InstanceDefault(FuzzyTableNameMapping.class)
	TypeMapping getTypeMapping();

	/**
	 * Setter for {@link #getTypeMapping()}.
	 */
	void setTypeMapping(TypeMapping mapping);

	/**
	 * The replay steps containing {@link EventRewriter}s.
	 */
	@Key(Step.NAME_ATTRIBUTE)
	Map<String, Step> getSteps();

	/**
	 * A migration step is a named sequence of {@link EventRewriter rewrites}.
	 */
	interface Step extends NamedConfigMandatory {

		/**
		 * The {@link EventRewriter rewrites} that transform events in this step.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends EventRewriter>> getRewriters();

	}

	/**
	 * Migration steps for tables that are not known by the {@link KnowledgeBase}, e.g. workflow
	 * tables.
	 */
	List<TableTransformerStep> getTransformerSteps();

	/**
	 * A table transformer step is a named sequence of {@link RowTransformer transformers}.
	 */
	interface TableTransformerStep extends NamedConfigMandatory {

		/**
		 * The {@link RowTransformer}s that rewrite table rows.
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends RowTransformer>> getTransformers();

	}

}
