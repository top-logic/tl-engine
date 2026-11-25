/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.BranchIdType;

/**
 * {@link MigrationProcessor} making the container end of each composition in the stored model
 * single and an {@link TLAssociationEnd#isAggregate() aggregate}.
 *
 * <p>
 * The container end of a composition is the own end of its association, the end opposite to the
 * {@link TLAssociationEnd#isComposite() composite} end. It refers to the single container of a
 * part, so that a backwards reference of a composition answers the container of a part. Each
 * container end that is multiple or no aggregate is repaired and reported with its qualified name.
 * A model without such ends is left unchanged.
 * </p>
 *
 * @see Util#getInvalidCompositionContainerEnds(PooledConnection)
 */
public class RepairCompositionContainerEndsProcessor
		extends AbstractConfiguredInstance<RepairCompositionContainerEndsProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link RepairCompositionContainerEndsProcessor}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends RepairCompositionContainerEndsProcessor> extends PolymorphicConfiguration<I> {

		/**
		 * Tag name of {@link Config} in a migration script.
		 */
		String TAG_NAME = "repair-composition-container-ends";

	}

	/**
	 * Creates a {@link RepairCompositionContainerEndsProcessor} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public RepairCompositionContainerEndsProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();
		try {
			Map<BranchIdType, String> ends = util.getInvalidCompositionContainerEnds(connection);
			for (Entry<BranchIdType, String> end : ends.entrySet()) {
				util.updateTLStructuredTypePart(connection, end.getKey(), null, null, null, null, null, null,
					Boolean.TRUE, Boolean.FALSE, Boolean.FALSE, Boolean.FALSE, null, null, null, null, null);
				log.info("Made the container end '" + end.getValue() + "' of a composition single and aggregate.");
			}
			log.info("Repaired " + ends.size() + " container ends of compositions.");
		} catch (SQLException ex) {
			log.error("Failed to repair the container ends of compositions: " + ex.getMessage(), ex);
		}
	}

}
