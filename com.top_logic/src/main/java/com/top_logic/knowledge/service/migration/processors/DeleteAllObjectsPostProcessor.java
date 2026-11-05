/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.migration.processors;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.migration.MigrationPostProcessor;

/**
 * {@link MigrationPostProcessor} deleting every object stored in a table.
 *
 * <p>
 * The objects are deleted through the {@link KnowledgeBase}, not by SQL: their generic (flex)
 * attribute values and the links they are an end of go with them, as they do with every other
 * deletion. Use it for objects that are to be dropped as a whole, whose attributes do not all live
 * in their own table.
 * </p>
 */
public class DeleteAllObjectsPostProcessor extends AbstractConfiguredInstance<DeleteAllObjectsPostProcessor.Config<?>>
		implements MigrationPostProcessor {

	/**
	 * Configuration options for {@link DeleteAllObjectsPostProcessor}.
	 */
	public interface Config<I extends DeleteAllObjectsPostProcessor> extends PolymorphicConfiguration<I> {

		/** Configuration name of {@link #getTable()}. */
		String TABLE = "table";

		/**
		 * Name of the table whose objects are deleted.
		 */
		@Name(TABLE)
		@Mandatory
		String getTable();
	}

	/**
	 * Creates a {@link DeleteAllObjectsPostProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteAllObjectsPostProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void afterMigration(Log log, KnowledgeBase kb) {
		String table = getConfig().getTable();
		List<KnowledgeObject> objects = new ArrayList<>(kb.getAllKnowledgeObjects(table));
		KBUtils.deleteAllKI(objects.iterator());
		log.info("Deleted " + objects.size() + " object(s) from table '" + table + "'.");
	}

}
