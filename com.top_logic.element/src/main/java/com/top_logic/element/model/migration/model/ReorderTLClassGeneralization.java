/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import java.sql.SQLException;

import org.w3c.dom.Document;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLType;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} changing the order of a {@link TLClass} generalization relation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReorderTLClassGeneralization extends AbstractConfiguredInstance<ReorderTLClassGeneralization.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link ReorderTLClassGeneralization}.
	 */
	@TagName("reorder-generalization")
	public interface Config extends PolymorphicConfiguration<ReorderTLClassGeneralization> {

		/**
		 * Qualified name of the specialization {@link TLType}.
		 */
		@Mandatory
		QualifiedTypeName getType();

		/**
		 * Setter for {@link #getType()}.
		 */
		void setType(QualifiedTypeName value);

		/**
		 * Qualified name of the generalization that is reordered.
		 */
		@Mandatory
		QualifiedTypeName getGeneralization();

		/**
		 * Setter for {@link #getGeneralization()}.
		 */
		void setGeneralization(QualifiedTypeName value);

		/**
		 * The name of the generalization of {@link #getType()} which must be the direct successor
		 * of the reordered generalization.
		 * 
		 * <p>
		 * May be <code>null</code>. In this case the generalization is moved to the end of the
		 * list.
		 * </p>
		 */
		@Nullable
		QualifiedTypeName getBefore();

		/**
		 * Setter for {@link #getBefore()}.
		 */
		void setBefore(QualifiedTypeName before);

	}

	private Util _util;

	/**
	 * Creates a {@link ReorderTLClassGeneralization} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReorderTLClassGeneralization(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Reordering generalizations failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel) throws SQLException, MigrationException {
		Type specialization;
		try {
			specialization = _util.getTLTypeOrFail(connection, getConfig().getType());
		} catch (MigrationException ex) {
			log.info("No type with name '" + _util.qualifiedName(getConfig().getType())
					+ "' as source of the generalization relation available at" + getConfig().location(),
				Log.WARN);
			return false;
		}

		Type generalization;
		try {
			generalization = _util.getTLTypeOrFail(connection, getConfig().getGeneralization());
		} catch (MigrationException ex) {
			log.info(
				"No type with name '" + _util.qualifiedName(getConfig().getGeneralization())
						+ "' as destination of the generalization relation available at" + getConfig().location(),
				Log.WARN);
			return false;
		}

		Type before;
		if (getConfig().getBefore() != null) {
			try {
				before = _util.getTLTypeOrFail(connection, getConfig().getBefore());
			} catch (MigrationException ex) {
				log.info(
					"No type with name '" + _util.qualifiedName(getConfig().getBefore())
							+ "' as successor of the generalization relation available at" + getConfig().location(),
					Log.WARN);
				return false;
			}
		} else {
			before = null;
		}
		_util.reorderTLTypeGeneralization(connection, specialization, generalization, before);
		boolean updateModelBaseline =
			MigrationUtils.reorderGeneralisation(log, tlModel, getConfig().getType(), getConfig().getGeneralization(),
				getConfig().getBefore());
		StringBuilder info = new StringBuilder("Generalization link '");
		info.append(_util.toString(specialization)).append(" <-> ").append(_util.toString(generalization));
		info.append("' has been moved ");
		if (before == null) {
			info.append("to the end.");
		} else {
			info.append("before '");
			info.append(_util.toString(specialization)).append(" <-> ").append(_util.toString(before));
			info.append("'.");
		}
		log.info(info.toString());
		return updateModelBaseline;
	}

}
