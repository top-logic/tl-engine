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
	 * Name of the {@link SchemaConfiguration} used by default.
	 */
	String DEFAULT_NAME = "Default";

	/** Attribute name of the "declarations" property. */
	String DECLARATIONS_ATTRIBUTE = "declarations";

	/** @see #getSchemas() */
	String SCHEMAS_ATTRIBUTE = "schemas";

	/** @see #hasMultipleBranches() */
	String MULTIPLE_BRANCHES_ATTRIBUTE = "multiple-branches";

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
	@BooleanDefault(true)
	@Name(MULTIPLE_BRANCHES_ATTRIBUTE)
	boolean hasMultipleBranches();

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