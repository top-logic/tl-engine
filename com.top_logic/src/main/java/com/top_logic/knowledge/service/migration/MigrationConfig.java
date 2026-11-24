/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.NamedConfiguration;
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
	MigrationConfig.DROPPED_DEPENDENCIES_PROPERTY,
	MigrationConfig.ADDED_DEPENDENCIES_PROPERTY,
	MigrationConfig.PRE_PROCESSORS_PROPERTY,
	MigrationConfig.PROCESSORS_PROPERTY,
	MigrationConfig.MIGRATION_PROPERTY,
	MigrationConfig.POST_PROCESSORS,
	MigrationConfig.STARTUP_ACTIONS,
})
public interface MigrationConfig extends ConfigurationItem {

	/** Configuration name for {@link #getDependencies()}. */
	String DEPENDENCIES_PROPERTY = "dependencies";

	/** Configuration name for {@link #getDroppedDependencies()}. */
	String DROPPED_DEPENDENCIES_PROPERTY = "dropped-dependencies";

	/** Configuration name for {@link #getAddedDependencies()}. */
	String ADDED_DEPENDENCIES_PROPERTY = "added-dependencies";

	/** Tag name of an entry in {@link #getDroppedDependencies()} and {@link #getAddedDependencies()}. */
	String MODULE_ENTRY = "module";

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

	/** Configuration name for {@link #getStartupActions()}. */
	String STARTUP_ACTIONS = "startup-actions";

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
	 * Modules the predecessor migration of the same module depends on, that this migration
	 * intentionally no longer depends on, because the module itself no longer depends on them.
	 * 
	 * <p>
	 * The {@link #getDependencies() dependencies} of a migration normally contain all modules the
	 * dependencies of its predecessor contain. A module that is listed here may be missing from
	 * the {@link #getDependencies() dependencies} without the migration scripts being reported as
	 * inconsistent. A module that is not missing compared to the predecessor must not be listed.
	 * </p>
	 */
	@Key(NamedConfiguration.NAME_ATTRIBUTE)
	@EntryTag(MODULE_ENTRY)
	@Name(DROPPED_DEPENDENCIES_PROPERTY)
	Map<String, ModuleRef> getDroppedDependencies();

	/**
	 * Modules the predecessor migration of the same module does not depend on, that become a
	 * dependency of the module with this migration, because the application adopts them.
	 * 
	 * <p>
	 * A module normally depends on all modules any of its migrations depends on, so each migration
	 * not naming such a module gets an implicit dependency on the initial version of that module.
	 * The migrations before this one in the same module get neither this implicit dependency nor a
	 * transitive dependency to a module listed here: they never run in the same upgrade as the
	 * migrations of the adopted module, since a database without a stored version of a module
	 * skips the migrations of that module. A module listed here must be contained in the
	 * {@link #getDependencies() dependencies} of this migration and must not be contained in the
	 * dependencies of its predecessor.
	 * </p>
	 */
	@Key(NamedConfiguration.NAME_ATTRIBUTE)
	@EntryTag(MODULE_ENTRY)
	@Name(ADDED_DEPENDENCIES_PROPERTY)
	Map<String, ModuleRef> getAddedDependencies();

	/**
	 * Entry of {@link MigrationConfig#getDroppedDependencies()} and
	 * {@link MigrationConfig#getAddedDependencies()}, its {@link #getName() name} is the name of the
	 * module.
	 */
	interface ModuleRef extends NamedConfigMandatory {
		// The name is the only property.
	}

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

	/**
	 * Actions to apply after the system has started all modules.
	 */
	@Name(STARTUP_ACTIONS)
	List<PolymorphicConfiguration<? extends StartupAction>> getStartupActions();

}

