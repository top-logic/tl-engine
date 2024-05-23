/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Log;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.dob.meta.MORepository;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.model.migration.Util;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} fills an order attribute of a link table.
 */
public class SynthesizeLinkOrderProcessor extends AbstractConfiguredInstance<SynthesizeLinkOrderProcessor.Config<?>>
		implements MigrationProcessor {

	/**
	 * Configuration options for {@link SynthesizeLinkOrderProcessor}.
	 */
	@TagName("synthesize-link-order")
	public interface Config<I extends SynthesizeLinkOrderProcessor> extends PolymorphicConfiguration<I> {
		/**
		 * The name of the link table that defines the order attribute to be filled.
		 */
		@Mandatory
		@Name("link-table")
		String getLinkTable();

		/**
		 * The object table from which the order information is extracted.
		 */
		@Mandatory
		@Name("source-table")
		String getSourceTable();

		/**
		 * The order column to fill.
		 */
		@StringDefault(KBBasedMetaAttribute.OWNER_REF_ORDER_ATTR)
		@Name("order-column")
		String getOrderColumn();

		/**
		 * The reference to the object from which the order information is taken.
		 */
		@StringDefault(DBKnowledgeAssociation.REFERENCE_DEST_NAME)
		@Name("source-ref")
		String getSourceRef();

		/**
		 * The attribute in the source table to take the order information from.
		 */
		@Mandatory
		@Name("source-order-attribute")
		String getSourceOrderAttribute();

		/**
		 * Factor to multiply the extracted order information with to get the internal ordering
		 * value.
		 */
		@IntDefault(1)
		int getFactor();
	}

	/**
	 * Creates a {@link SynthesizeLinkOrderProcessor} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SynthesizeLinkOrderProcessor(InstantiationContext context, Config<?> config) {
		super(context, config);
	}


	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		Util util = context.getSQLUtils();
		Config<?> config = getConfig();
		try {
			MORepository repository = context.getPersistentRepository();

			MOClass sourceSpec = (MOClass) repository.getType(config.getSourceTable());
			MOAttribute sourceOrderAttr = sourceSpec.getAttribute(config.getSourceOrderAttribute());
			
			MOClass linkTable = (MOClass) repository.getType(config.getLinkTable());
			MOReference sourceRef = (MOReference) linkTable.getAttribute(config.getSourceRef());
			MOAttribute orderAttr = linkTable.getAttribute(config.getOrderColumn());
			
			for (MetaObject type : repository.getMetaObjects()) {
				if (!(type instanceof MOClass)) {
					continue;
				}
				
				MOClass sourceTable = (MOClass) type;
				if (sourceTable.isAbstract()) {
					continue;
				}
				
				if (!sourceTable.isSubtypeOf(sourceSpec)) {
					continue;
				}
				
				boolean hasBranches = util.hasBranches();
				CompiledStatement select = query(
					select(
						Util.listWithoutNull(
							util.branchColumnDefOrNull("l"),
							columnDef(column("l", BasicTypes.IDENTIFIER_DB_NAME)),
							columnDef(column("l", BasicTypes.REV_MAX_DB_NAME)),
							columnDef(column("s", sourceOrderAttr.getDbMapping()[0]
								.getDBName()))
						),
						leftJoin(
							table(linkTable.getDBMapping().getDBName(), "l"), 
							table(sourceTable.getDBMapping().getDBName(), "s"), 
							and(
								hasBranches ?
									eqSQL(util.branchColumnRef("l"), util.branchColumnRef("s"))
									: literalTrueLogical(),
								eqSQL(column("l", sourceRef.getColumn(ReferencePart.name).getDBName()), column("s", BasicTypes.IDENTIFIER_DB_NAME)),
								le(column("l", BasicTypes.REV_MAX_DB_NAME), column("s", BasicTypes.REV_MAX_DB_NAME)),
								ge(column("l", BasicTypes.REV_MAX_DB_NAME), column("s", BasicTypes.REV_MIN_DB_NAME))
							)
						),
						eqSQL(
							literal(DBType.STRING, sourceTable.getName()), 
							column("l", sourceRef.getColumn(ReferencePart.type).getDBName())),
						Util.listWithoutNull(
							util.branchOrderOrNull("l"),
							order(false, column("l", BasicTypes.IDENTIFIER_DB_NAME)),
							order(false, column("l", BasicTypes.REV_MAX_DB_NAME)))
					)
				).toSql(connection.getSQLDialect());
								
				CompiledStatement update = query(
					select(
						Util.listWithoutNull(
							util.branchColumnDefOrNull(),
							columnDef(column(BasicTypes.IDENTIFIER_DB_NAME)),
							columnDef(column(BasicTypes.REV_MAX_DB_NAME)),
							columnDef(column(orderAttr.getDbMapping()[0].getDBName()))
						),
						table(linkTable.getDBMapping().getDBName()),
						eqSQL(
							literal(DBType.STRING, sourceTable.getName()), 
							column(sourceRef.getColumn(ReferencePart.type).getDBName())),
						Util.listWithoutNull(
							util.branchOrderOrNull(),
							order(false, column(BasicTypes.IDENTIFIER_DB_NAME)),
							order(false, column(BasicTypes.REV_MAX_DB_NAME)))
					)
				).toSql(connection.getSQLDialect());

				update.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

				int cnt = 0;
				int factor = getConfig().getFactor();
				int idIndex = util.getBranchIndexInc() + 1;
				int revIndex = util.getBranchIndexInc() + 2;
				int orderIndex = util.getBranchIndexInc() + 3;
				try (ResultSet cursor = update.executeQuery(connection)) {
					try (ResultSet result = select.executeQuery(connection)) {
						while (cursor.next()) {
							if (!result.next()) {
								log.info("Inconsistent line count.", Log.WARN);
								break;
							}

							long branch1 = hasBranches ? cursor.getLong(1) : TLContext.TRUNK_ID;
							long id1 = cursor.getLong(idIndex);
							long rev1 = cursor.getLong(revIndex);

							long branch2 = hasBranches ? result.getLong(1) : TLContext.TRUNK_ID;
							long id2 = result.getLong(idIndex);
							long rev2 = result.getLong(revIndex);
							int order = result.getInt(orderIndex);

							if (branch1 != branch2 || id1 != id2 || rev1 != rev2) {
								log.info("Inconsistent lines: " + branch1 + ", " + id1 + ", " + rev1 + " vs. " + branch2
									+ ", " + id2 + ", " + rev2, Log.WARN);
								break;
							}

							long newOrder = order * factor;
							if (newOrder > Integer.MAX_VALUE) {
								log.info("Overflow: " + branch1 + ", " + id1 + ", " + rev1 + ": " + newOrder, Log.WARN);

								newOrder = Integer.MAX_VALUE;
							}

							cursor.updateInt(orderIndex, (int) newOrder);
							cursor.updateRow();

							cnt++;
						}
					}
				}

				log.info("Synthesized " + cnt + " order values in table '" + sourceTable.getName() + "'.");
			}
		} catch (SQLException ex) {
			log.error("Failed to synthesize order of table '" + config.getLinkTable() + "': " + ex.getMessage(), ex);
		}
	}

}
