/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.db2.migration.config.ReplayConfig;

/**
 * Configuration of the migration steps for the represented version.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@DisplayOrder({
	MigrationConfig.VERSION_PROPERTY,
	MigrationConfig.DEPENDENCIES_PROPERTY,
	MigrationConfig.PRE_PROCESSORS_PROPERTY,
	MigrationConfig.PROCESSORS_PROPERTY,
	MigrationConfig.MIGRATION_PROPERTY,
	MigrationConfig.POST_PROCESSORS,
})
public interface MigrationConfig extends ConfigurationItem {

	/** Configuration name for {@link #getDependencies()}. */
	String DEPENDENCIES_PROPERTY = "dependencies";

	/** Configuration name for {@link #getVersion()}. */
	String VERSION_PROPERTY = "version";

	/** Configuration name for {@link #getMigration()}. */
	String MIGRATION_PROPERTY = ReplayConfig.TAG_NAME;

	/** Configuration name for {@link #getPreProcessors()}. */
	String PRE_PROCESSORS_PROPERTY = "pre-processors";

	/** Configuration name for {@link #getProcessors()}. */
	String PROCESSORS_PROPERTY = "processors";

	/** Configuration name for {@link #getPostProcessors()}. */
	String POST_PROCESSORS = "post-processors";

	/**
	 * The versions which are needed to be able to apply this migration.
	 * 
	 * <p>
	 * The dependencies are complete in the sense that for all dependent modules, if there is a
	 * version, a version is contained.
	 * </p>
	 */
	@Key(Version.MODULE_ATTRIBUTE)
	@Name(DEPENDENCIES_PROPERTY)
	Map<String, Version> getDependencies();

	/**
	 * The {@link Version} for which this migration is made.
	 */
	@Mandatory
	@Name(VERSION_PROPERTY)
	Version getVersion();

	/**
	 * Setter for {@link #getVersion()}.
	 * 
	 * @param v
	 *        New value of {@link #getVersion()}.
	 */
	void setVersion(Version v);

	/**
	 * {@link MigrationProcessor} to be executed out of order before regular processors.
	 * 
	 * <p>
	 * Adjusting the baselines of model and schema due to code changes (e.g. class or property
	 * renames or structural changes) is recommended to do before regular processing to allow
	 * regular processors to read the baselines with typed configuration tooling.
	 * </p>
	 */
	@Name(PRE_PROCESSORS_PROPERTY)
	@EntryTag("processor") // Allow to easily move processors between phase blocks.
	List<PolymorphicConfiguration<? extends MigrationProcessor>> getPreProcessors();

	/**
	 * @see #getPreProcessors()
	 */
	void setPreProcessors(List<PolymorphicConfiguration<? extends MigrationProcessor>> value);

	/**
	 * {@link MigrationProcessor} to execute before the actual migration as described in
	 * {@link #getMigration()}.
	 */
	@Name(PROCESSORS_PROPERTY)
	List<PolymorphicConfiguration<? extends MigrationProcessor>> getProcessors();

	/**
	 * @see #getProcessors()
	 */
	void setProcessors(List<PolymorphicConfiguration<? extends MigrationProcessor>> value);

	/**
	 * Definition of the migration steps of a replay.
	 */
	@Name(MIGRATION_PROPERTY)
	ReplayConfig getMigration();

	/**
	 * Setter for {@link #getMigration()}.
	 */
	void setMigration(ReplayConfig replayMigration);

	/**
	 * {@link MigrationPostProcessor}s that are executed after a replay migration has potentially
	 * happened.
	 */
	@Name(POST_PROCESSORS)
	List<PolymorphicConfiguration<? extends MigrationPostProcessor>> getPostProcessors();

	/**
	 * @see #getPostProcessors()
	 */
	void setPostProcessors(List<PolymorphicConfiguration<? extends MigrationPostProcessor>> value);

}

