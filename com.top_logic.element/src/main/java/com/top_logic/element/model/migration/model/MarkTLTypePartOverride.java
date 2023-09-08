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
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.TLNamed;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotatedConfig;
import com.top_logic.model.annotate.TLAttributeAnnotation;

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
	public interface Config
			extends PolymorphicConfiguration<MarkTLTypePartOverride>, AnnotatedConfig<TLAttributeAnnotation> {

		/**
		 * Qualified name of the overriding part.
		 */
		@Mandatory
		QualifiedPartName getName();

		/**
		 * Qualified name of the definition of the overriding part, i.e. the top level part in the
		 * override chain.
		 */
		@Mandatory
		QualifiedPartName getDefinition();

	}

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
	public void doMigration(Log log, PooledConnection connection) {
		try {
			internalDoMigration(log, connection);
		} catch (Exception ex) {
			log.error(
				"Marking " + Util.qualifiedName(getConfig().getName()) + " as override of "
					+ Util.qualifiedName(getConfig().getDefinition()) + " failed at " + getConfig().location(),
				ex);
		}
	}

	private void internalDoMigration(Log log, PooledConnection connection) throws Exception {
		QualifiedPartName partName = getConfig().getName();
		QualifiedPartName definition = (getConfig().getDefinition());
		Type partOwner = Util.getTLTypeOrFail(connection, partName);

		TypePart definitionPart = Util.getTLTypePartOrFail(connection, definition);
		CompiledStatement sql = query(
		parameters(
			parameterDef(DBType.LONG, "branch"),
			parameterDef(DBType.ID, "owner"),
			parameterDef(DBType.STRING, "part"),
			parameterDef(DBType.ID, "definitionID")),
		update(
			table(SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_OBJECT_TYPE)),
			and(
				eqSQL(
					column(BasicTypes.BRANCH_DB_NAME),
					parameter(DBType.LONG, "branch")),
				eqSQL(
					column(Util.refID(KBBasedMetaAttribute.OWNER_REF)),
					parameter(DBType.ID, "owner")),
				eqSQL(
					column(SQLH.mangleDBName(TLNamed.NAME)),
					parameter(DBType.STRING, "part"))),
			Arrays.asList(Util.refID(KBBasedMetaAttribute.DEFINITION_REF)),
			Arrays.asList(parameter(DBType.ID, "definitionID")))).toSql(connection.getSQLDialect());

		sql.executeUpdate(connection, partOwner.getBranch(), partOwner.getID(), partName.getPartName(),
			definitionPart.getID());

		log.info("Mark " + Util.qualifiedName(partName) + " as override of " + Util.qualifiedName(definition));
	}

}
