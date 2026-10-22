/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.setup.config;

import java.util.List;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.NamedInstanceConfig;
import com.top_logic.basic.config.NamedPolymorphicConfiguration;
import com.top_logic.basic.config.NamedResource;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.ResourceDeclaration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Indexed;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.model.DBSchema;
import com.top_logic.basic.db.schema.setup.SchemaSetup;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.schema.config.MetaObjectsConfig;

/**
 * A named schema in {@link ApplicationTypes}.
 * 
 * @see SchemaSetup
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface SchemaConfiguration extends PolymorphicConfiguration<SchemaSetup>, NamedConfiguration {

	/**
	 * Default of {@link #hasMultipleBranches()}
	 */
	boolean DEFAULT_MULTIPLE_BRANCHES_ATTRIBUTE = true;

	/**
	 * Default of {@link #isBranchLast()}
	 */
	boolean DEFAULT_BRANCH_LAST_ATTRIBUTE = false;

	/**
	 * Name of the {@link SchemaConfiguration} used by default.
	 */
	String DEFAULT_NAME = "Default";

	/** Attribute name of the "declarations" property. */
	String DECLARATIONS_ATTRIBUTE = "declarations";

	/** @see #getSchemas() */
	String SCHEMAS_ATTRIBUTE = "schemas";

	/** @see #hasMultipleBranches() */
	String MULTIPLE_BRANCHES_ATTRIBUTE = "multiple-branches";

	/** @see #isBranchLast() */
	String BRANCH_LAST_ATTRIBUTE = "branch-last";

	/** @see #getMetaObjects() */
	String META_OBJECTS_ATTRIBUTE = "meta-objects";

	/** @see #getAdditionalTables() */
	String ADDITIONAL_TABLES_ATTRIBUTE = "additional-tables";

	@Override
	@StringDefault(DEFAULT_NAME)
	String getName();

	/**
	 * List of {@link ResourceDeclaration resources} containing table declarations.
	 * 
	 * <p>
	 * These resources are internally used to resolve the {@link MetaObjectsConfig} which is stored
	 * in {@link #getMetaObjects()}.
	 * </p>
	 * 
	 * @see #getMetaObjects()
	 */
	@Name(DECLARATIONS_ATTRIBUTE)
	@Key(ResourceDeclaration.RESOURCE_ATTRIBUTE)
	List<ResourceDeclaration> getDeclarations();

	/**
	 * Declaration of additional low-level tables.
	 */
	@Name(SCHEMAS_ATTRIBUTE)
	@Key(NamedResource.NAME_ATTRIBUTE)
	List<NamedResource> getSchemas();

	/**
	 * Indexed access to #getSchemas()
	 */
	@Indexed(collection = SCHEMAS_ATTRIBUTE)
	NamedResource getSchema(String string);

	/**
	 * List of configurations of {@link TypeProvider} to create types programatically.
	 */
	@Key(NamedPolymorphicConfiguration.NAME_ATTRIBUTE)
	List<NamedInstanceConfig<? extends TypeProvider>> getProviders();

	/**
	 * Whether the types support more branches than trunk.
	 */
	@BooleanDefault(DEFAULT_MULTIPLE_BRANCHES_ATTRIBUTE)
	@Name(MULTIPLE_BRANCHES_ATTRIBUTE)
	boolean hasMultipleBranches();

	/**
	 * Whether the branch column is placed behind the identifying columns in the primary keys and
	 * reference indexes of the tables.
	 * 
	 * <p>
	 * By default, the branch column leads each index. This allows databases with index prefix
	 * compression (Oracle) to store the branch only once per index block. With this option, the
	 * branch column is placed directly before the trailing revision column instead (or at the end
	 * of an index without revision column), e.g. the primary key of an object table becomes
	 * <code>(IDENTIFIER, BRANCH, REV_MAX)</code> and the index of a reference
	 * <code>(&lt;REF&gt;_ID, BRANCH, REV_MAX)</code>.
	 * </p>
	 * 
	 * <p>
	 * This is meant for database systems that use an index for a condition with an
	 * <code>IN</code> list only if the list values are given for the first column of the index
	 * (H2). With the branch column in front, loading objects by a list of identifiers and searching
	 * the referrers of a list of objects scan the whole table on such a system.
	 * </p>
	 * 
	 * <p>
	 * Indexes declared with an explicit column list (custom indexes) keep their declared column
	 * order. In the other declared indexes, the branch column follows the declared columns.
	 * </p>
	 * 
	 * <p>
	 * Changing the value for an existing database converts the primary keys and indexes of its
	 * tables to the configured layout when the application starts.
	 * </p>
	 */
	@BooleanDefault(DEFAULT_BRANCH_LAST_ATTRIBUTE)
	@Name(BRANCH_LAST_ATTRIBUTE)
	boolean isBranchLast();

	/**
	 * No longer used: All {@link KeyAttributes} indexed by type for this
	 * {@link SchemaConfiguration}.
	 * 
	 * @deprecated Instead of configuring the key attributes globally at the
	 *             {@link SchemaConfiguration}, the {@link KeyAttributes} is now an annotation at
	 *             the concrete {@link MOClass}. This code can currently not be removed to ensure
	 *             the stored {@link SchemaConfiguration} in the KnowledgeBase can still be read.
	 *             This code (and referenced) can be removed as soon as all application have
	 *             migrated #23387. See also "migration_23387.migration.xml".
	 */
	@Hidden
	@Deprecated
	@Key("type")
	List<KeyAttributes> getKeyAttributes();

	/**
	 * The {@link MetaObject} declarations for this {@link SchemaSetup}.
	 * 
	 * <p>
	 * This value is filled from {@link #getDeclarations()} during a resolve process.
	 * </p>
	 * 
	 * @see SchemaSetup#resolve(com.top_logic.basic.config.InstantiationContext)
	 */
	@Name(META_OBJECTS_ATTRIBUTE)
	MetaObjectsConfig getMetaObjects();

	/**
	 * @see #getMetaObjects()
	 */
	void setMetaObjects(MetaObjectsConfig value);

	/**
	 * The additional table declarations for this {@link SchemaSetup}.
	 * 
	 * <p>
	 * This value is filled from {@link #getSchemas()} during a resolve process.
	 * </p>
	 * 
	 * @see SchemaSetup#resolve(com.top_logic.basic.config.InstantiationContext)
	 */
	@Name(ADDITIONAL_TABLES_ATTRIBUTE)
	DBSchema getAdditionalTables();

	/**
	 * @see #getAdditionalTables()
	 */
	void setAdditionalTables(DBSchema value);

}