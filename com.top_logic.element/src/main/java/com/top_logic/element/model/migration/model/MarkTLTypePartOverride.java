/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.util.Arrays;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.migration.Util;
import com.top_logic.model.migration.data.QualifiedPartName;
import com.top_logic.model.migration.data.Type;
import com.top_logic.model.migration.data.TypePart;

/**
 * {@link MigrationProcessor} marks a {@link TLStructuredTypePart} as override of another part.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MarkTLTypePartOverride extends AbstractConfiguredInstance<MarkTLTypePartOverride.Config>
		implements MigrationProcessor {

	/**
	 * Configuration options of {@link MarkTLTypePartOverride}.
	 */
	@TagName("mark-override")
	public interface Config extends PolymorphicConfiguration<MarkTLTypePartOverride> {

		/**
		 * Qualified name of the overriding part.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Setter for {@link #getName()}.
		 */
		void setName(QualifiedPartName value);

		/**
		 * Qualified name of the definition of the overriding part, i.e. the top level part in the
		 * override chain.
		 */
		@Mandatory
		QualifiedPartName getDefinition();

		/**
		 * Setter for {@link #getDefinition()}.
		 */
		void setDefinition(QualifiedPartName value);

	}

	private Util _util;

	/**
	 * Creates a {@link MarkTLTypePartOverride} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MarkTLTypePartOverride(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.get(Util.PROPERTY);
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error(
				"Marking " + _util.qualifiedName(getConfig().getName()) + " as override of "
					+ _util.qualifiedName(getConfig().getDefinition()) + " failed at " + getConfig().location(),
				ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		QualifiedPartName definition = (getConfig().getDefinition());
		Type partOwner = _util.getTLTypeOrFail(connection, partName);

		TypePart definitionPart = _util.getTLTypePartOrFail(connection, definition);
		CompiledStatement sql = query(
		parameters(
			_util.branchParamDef(),
			parameterDef(DBType.ID, "owner"),
			parameterDef(DBType.STRING, "part"),
			parameterDef(DBType.ID, "definitionID")),
		update(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				_util.eqBranch(),
				eqSQL(
					column(_util.refID(KBBasedMetaAttribute.OWNER_REF)),
					parameter(DBType.ID, "owner")),
				eqSQL(
					column(SQLH.mangleDBName(TLNamed.NAME)),
					parameter(DBType.STRING, "part"))),
			Arrays.asList(_util.refID(KBBasedMetaAttribute.DEFINITION_REF)),
			Arrays.asList(parameter(DBType.ID, "definitionID")))).toSql(connection.getSQLDialect());

		sql.executeUpdate(connection, partOwner.getBranch(), partOwner.getID(), partName.getPartName(),
			definitionPart.getID());

		log.info("Mark " + _util.qualifiedName(partName) + " as override of " + _util.qualifiedName(definition));
	}

}
