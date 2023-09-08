/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.rewriters;

import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.annotation.TableName;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;

/**
 * Mapping rewriting the {@link TableName} annotation of a {@link AnnotationConfigs}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangeTablesAnnotation extends AbstractConfiguredInstance<ChangeTablesAnnotation.Config>
		implements Mapping<ConfigurationItem, ConfigurationItem> {

	/**
	 * Typed configuration interface definition for {@link ChangeTablesAnnotation}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<ChangeTablesAnnotation> {

		/**
		 * The source table. Identifies the {@link TableName} to rewrite.
		 */
		String getSourceTable();

		/**
		 * The new table to write to.
		 */
		String getTargetTable();

	}

	/**
	 * Create a {@link ChangeTablesAnnotation}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ChangeTablesAnnotation(InstantiationContext context, Config config) {
		super(context, config);
		if (config.getSourceTable().equals(config.getTargetTable())) {
			context.error("Source table must not match target table: " + config.getSourceTable());
		}
	}

	@Override
	public ConfigurationItem map(ConfigurationItem input) {
		if (!(input instanceof AnnotationConfigs)) {
			return input;
		}
		AnnotationConfigs annotations = (AnnotationConfigs) input;
		TableName tableName = annotations.getAnnotation(TableName.class);
		String sourceTableName = getConfig().getSourceTable();
		if (tableName == null) {
			if (!sourceTableName.isEmpty()) {
				return input;
			}
		} else {
			if (!sourceTableName.equals(tableName.getName())) {
				return input;
			}
		}
		annotations = ConfigCopier.copy(annotations);

		String targetTable = getConfig().getTargetTable();
		if (targetTable.isEmpty()) {
			annotations.getAnnotations().removeIf(element -> element instanceof TableName);
		} else {
			TableName existingAnnotation = annotations.getAnnotation(TableName.class);
			if (existingAnnotation == null) {
				annotations.getAnnotations().add(TLAnnotations.newTableAnnotation(targetTable));
			} else {
				existingAnnotation.update(existingAnnotation.descriptor().getProperty(TableName.NAME), targetTable);
			}
		}
		return annotations;
	}

}

