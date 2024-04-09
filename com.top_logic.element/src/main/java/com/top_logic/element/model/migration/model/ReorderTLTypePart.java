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
import com.top_logic.knowledge.wrap.list.FastList;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.MigrationException;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.QualifiedTypeName;
import com.top_logic.model.migration.data.Type;

/**
 * {@link MigrationProcessor} changing the order of an {@link TLTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ReorderTLTypePart extends AbstractConfiguredInstance<ReorderTLTypePart.Config>
		implements TLModelBaseLineMigrationProcessor {

	/**
	 * Configuration options of {@link ReorderTLTypePart}.
	 */
	@TagName("reorder-part")
	public interface Config extends PolymorphicConfiguration<ReorderTLTypePart> {

		/**
		 * Qualified name of the {@link TLTypePart} to reorder.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName value);

		/**
		 * The name of the {@link TLTypePart} which must be the direct successor of the reordered
		 * {@link TLTypePart}.
		 * 
		 * <p>
		 * May be <code>null</code>. In this case the part is moved to the end of the list.
		 * </p>
		 */
		@Nullable
		String getBefore();

		/**
		 * Setter for {@link #getBefore()}.
		 */
		void setBefore(String before);

	}

	private Util _util;

	/**
	 * Creates a {@link ReorderTLTypePart} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ReorderTLTypePart(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public boolean migrateTLModel(MigrationContext context, Log log, PooledConnection connection, Document tlModel) {
		try {
			_util = context.get(Util.PROPERTY);
			return internalDoMigration(log, connection, tlModel);
		} catch (Exception ex) {
			log.error("Reordering tl part migration failed at " + getConfig().location(), ex);
			return false;
		}
	}

	private boolean internalDoMigration(Log log, PooledConnection connection, Document tlModel)
			throws SQLException, MigrationException {
		QualifiedPartName partToReorder = getConfig().getName();
		QualifiedTypeName owner = partToReorder.getOwner();

		Type type;
		try {
			type = _util.getTLTypeOrFail(connection, owner);
		} catch (MigrationException ex) {
			log.info("No type with name '" + _util.qualifiedName(owner) + "' as owner of '"
					+ _util.qualifiedName(partToReorder) + "' available at" + getConfig().location(),
				Log.WARN);
			return false;
		}

		boolean updateModelBaseline;
		String before = getConfig().getBefore();
		if (FastList.OBJECT_NAME.equals(type.getTable())) {
			_util.reorderTLClassifier(connection, type, partToReorder.getName(), before);
			updateModelBaseline =
				tlModel == null ? false : MigrationUtils.reorderClassifier(log, tlModel, partToReorder, before);
		} else {
			_util.reorderTLStructuredTypePart(connection, type, partToReorder.getPartName(), before);
			updateModelBaseline =
				tlModel == null ? false : MigrationUtils.reorderStructuredTypePart(log, tlModel, partToReorder, before);
		}
		StringBuilder info = new StringBuilder("Part '");
		info.append(_util.qualifiedName(partToReorder)).append("' reordered ");
		if (before == null) {
			info.append("to the end.");
		} else {
			info.append("before '" + before + "'.");
		}
		log.info(info.toString());
		return updateModelBaseline;
	}

}
