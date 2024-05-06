/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration;

import static com.top_logic.basic.db.sql.SQLFactory.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.top_logic.basic.Log;
import com.top_logic.basic.LongID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.db.schema.properties.DBProperties;
import com.top_logic.basic.db.sql.CompiledStatement;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.basic.sql.DBType;
import com.top_logic.basic.sql.PooledConnection;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.dob.meta.BasicTypes;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.element.config.ReferenceConfig;
import com.top_logic.element.config.annotation.TLStorage;
import com.top_logic.element.meta.kbbased.KBBasedMetaAttribute;
import com.top_logic.element.meta.kbbased.PersistentReference;
import com.top_logic.element.meta.kbbased.storage.LinkStorage;
import com.top_logic.element.meta.schema.ElementSchemaConstants;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.service.db2.DBKnowledgeAssociation;
import com.top_logic.knowledge.service.migration.MigrationContext;
import com.top_logic.knowledge.service.migration.MigrationProcessor;
import com.top_logic.layout.scripting.recorder.ref.ApplicationObjectUtil;
import com.top_logic.model.impl.util.TLStructuredTypeColumns;
import com.top_logic.model.migration.Util;
import com.top_logic.util.TLContext;

/**
 * {@link MigrationProcessor} moving links implementing composition references to the correct table.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MoveCompositionLinksToStructureChildTable implements MigrationProcessor {

	private Util _util;

	@Override
	public void doMigration(MigrationContext context, Log log, PooledConnection connection) {
		try {
			_util = context.getSQLUtils();
			HashSet<Long> compositionRefIds = migrateData(log, connection);
			migrateAnnotations(log, connection, compositionRefIds);
			migrateBaseModel(log, connection);
		} catch (SQLException ex) {
			log.error("Failed to migrate composition references.", ex);
		}
	}

	private void migrateBaseModel(Log log, PooledConnection connection) throws SQLException {
		String xml = DBProperties.getProperty(connection, DBProperties.GLOBAL_PROPERTY,
			DynamicModelService.APPLICATION_MODEL_PROPERTY);
		if (xml != null) {
			Document document = DOMUtil.parse(xml);

			boolean changed = removeCompositionStoragAnnotations(document);

			if (changed) {
				String newConfig = DOMUtil.toString(document);
				DBProperties.setProperty(connection, DBProperties.GLOBAL_PROPERTY,
					DynamicModelService.APPLICATION_MODEL_PROPERTY, newConfig);

				log.info("Upgraded stored model by removing storage annotations of compositions: " + newConfig);
			}
		}
	}

	private boolean removeCompositionStoragAnnotations(Document model) {
		boolean changed = false;
		NodeList references = model.getElementsByTagNameNS(null, ElementSchemaConstants.REFERENCE_ELEMENT);
		for (int n = 0, cnt = references.getLength(); n < cnt; n++) {
			Element reference = (Element) references.item(n);
			String composite = reference.getAttributeNS(null, ReferenceConfig.COMPOSITE_PROPERTY);
			if ("true".equals(composite)) {
				for (Element annotations : DOMUtil.elementsNS(reference, null, ReferenceConfig.ANNOTATIONS)) {
					for (Element storage : DOMUtil.elementsNS(annotations, null, TLStorage.TAG_NAME)) {
						storage.getParentNode().removeChild(storage);
						changed = true;
					}
				}
			}
		}
		return changed;
	}

	private HashSet<Long> migrateData(Log log, PooledConnection connection) throws SQLException {
		DBHelper sql = connection.getSQLDialect();

		CompiledStatement getCompositionEnds = query(
		parameters(),
		select(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(SQLH.mangleDBName(KBBasedMetaAttribute.NAME)),
				columnDef(SQLH.mangleDBName(PersistentReference.ANNOTATIONS_MO_ATTRIBUTE))),
			table(SQLH.mangleDBName(PersistentReference.OBJECT_NAME)),
			and(
				eqSQL(
					_util.branchColumnRef(),
					literal(DBType.LONG, TLContext.TRUNK_ID)),
				eqSQL(
					column(SQLH.mangleDBName(BasicTypes.REV_MAX_DB_NAME)),
					literal(DBType.LONG, Revision.CURRENT_REV)),
				eqSQL(
					column(SQLH.mangleDBName(KBBasedMetaAttribute.IMPLEMENTATION_NAME)),
					literal(DBType.STRING, TLStructuredTypeColumns.ASSOCIATION_END_IMPL)),
				eqSQL(
					column(SQLH.mangleDBName(PersistentReference.COMPOSITE_ATTR)),
					literal(DBType.BOOLEAN, true))))).toSql(sql);

		String endIdColumn = ReferencePart.name.getReferenceAspectColumnName(
			SQLH.mangleDBName(PersistentReference.END_ATTR));

		CompiledStatement getRef = query(
		parameters(
			parameterDef(DBType.ID, "endId")),
		select(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(SQLH.mangleDBName(KBBasedMetaAttribute.NAME)),
				columnDef(SQLH.mangleDBName(PersistentReference.ANNOTATIONS_MO_ATTRIBUTE))),
			table(SQLH.mangleDBName(PersistentReference.OBJECT_NAME)),
			and(
				eqSQL(
					_util.branchColumnRef(),
					literal(DBType.LONG, TLContext.TRUNK_ID)),
				eqSQL(
					column(SQLH.mangleDBName(BasicTypes.REV_MAX_DB_NAME)),
					literal(DBType.LONG, Revision.CURRENT_REV)),
				eqSQL(
					column(endIdColumn),
					parameter(DBType.ID, "endId"))))).toSql(sql);

		String definitionIdColumn = ReferencePart.name.getReferenceAspectColumnName(
			SQLH.mangleDBName(PersistentReference.DEFINITION_REF));

		CompiledStatement getOverrides = query(
		parameters(
			parameterDef(DBType.ID, "definitionId")),
		select(
			columns(
				columnDef(BasicTypes.IDENTIFIER_DB_NAME)),
			table(SQLH.mangleDBName(PersistentReference.OBJECT_NAME)),
			and(
				eqSQL(
					_util.branchColumnRef(),
					literal(DBType.LONG, TLContext.TRUNK_ID)),
				eqSQL(
					column(SQLH.mangleDBName(BasicTypes.REV_MAX_DB_NAME)),
					literal(DBType.LONG, Revision.CURRENT_REV)),
				eqSQL(
					column(definitionIdColumn),
					parameter(DBType.ID, "definitionId"))))).toSql(sql);

		HashSet<Long> compositionRefIds = new HashSet<>();
		try (ResultSet ends = getCompositionEnds.executeQuery(connection)) {
			while (ends.next()) {
				long endId = ends.getLong(1);

				try (ResultSet refs = getRef.executeQuery(connection, LongID.valueOf(endId))) {
					while (refs.next()) {
						long refId = refs.getLong(1);
						compositionRefIds.add(Long.valueOf(refId));

						try (ResultSet overrides = getOverrides.executeQuery(connection, LongID.valueOf(refId))) {
							while (overrides.next()) {
								compositionRefIds.add(Long.valueOf(overrides.getLong(1)));
							}
						}

						String name = ends.getString(2);
						String refAnnotationXml = sql.getClobValue(refs, 3);

						String table = ApplicationObjectUtil.WRAPPER_ATTRIBUTE_ASSOCIATION;
						if (refAnnotationXml != null) {
							Document document = DOMUtil.parse(refAnnotationXml);
							NodeList annotations = document.getElementsByTagName(TLStorage.TAG_NAME);
							for (int n = 0, cnt = annotations.getLength(); n < cnt; n++) {
								for (Element storage : DOMUtil.elements(annotations.item(n))) {
									if (storage.getTagName().equals("structure-storage")
										|| "com.top_logic.element.meta.kbbased.storage.StructuredElementStorage"
											.equals(storage.getAttributeNS(null,
											PolymorphicConfiguration.IMPLEMENTATION_CLASS_NAME))) {
										table = ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION;
										break;
									}

									String tableAnnotation = storage.getAttribute(LinkStorage.Config.TABLE);
									if (!StringServices.isEmpty(tableAnnotation)) {
										table = tableAnnotation;
										break;
									}
								}
							}
						}

						if (!ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION.equals(table)) {
							moveLinks(log, connection, table, name, refId);
						}
					}
				}
			}
		}
		return compositionRefIds;
	}

	private void moveLinks(Log log, PooledConnection connection, String table, String name, long refId)
			throws SQLException {
		DBHelper sql = connection.getSQLDialect();
	
		String srcIdColumn = ReferencePart.name.getReferenceAspectColumnName(
			SQLH.mangleDBName(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME));
		String srcTypeColumn = ReferencePart.type.getReferenceAspectColumnName(
			SQLH.mangleDBName(DBKnowledgeAssociation.REFERENCE_SOURCE_NAME));
		String destIdColumn = ReferencePart.name.getReferenceAspectColumnName(
			SQLH.mangleDBName(DBKnowledgeAssociation.REFERENCE_DEST_NAME));
		String destTypeColumn = ReferencePart.type.getReferenceAspectColumnName(
			SQLH.mangleDBName(DBKnowledgeAssociation.REFERENCE_DEST_NAME));
		String attrIdColumn = ReferencePart.name.getReferenceAspectColumnName(
			SQLH.mangleDBName(ApplicationObjectUtil.META_ATTRIBUTE_ATTR));
	
		CompiledStatement copyLinks = query(
		parameters(
			parameterDef(DBType.ID, "refId")),
		insert(
			table(SQLH.mangleDBName(ApplicationObjectUtil.STRUCTURE_CHILD_ASSOCIATION)),
			Util.listWithoutNull(
				_util.branchColumnOrNull(),
				BasicTypes.IDENTIFIER_DB_NAME,
				BasicTypes.REV_MIN_DB_NAME,
				BasicTypes.REV_MAX_DB_NAME,
				BasicTypes.REV_CREATE_DB_NAME,
				srcIdColumn,
				srcTypeColumn,
				destIdColumn,
				destTypeColumn,
				attrIdColumn,
				SQLH.mangleDBName(LinkStorage.SORT_ORDER)),
			select(
				Util.listWithoutNull(
					_util.branchColumnDefOrNull(),
					columnDef(BasicTypes.IDENTIFIER_DB_NAME),
					columnDef(BasicTypes.REV_MIN_DB_NAME),
					columnDef(BasicTypes.REV_MAX_DB_NAME),
					columnDef(BasicTypes.REV_CREATE_DB_NAME),
					columnDef(srcIdColumn),
					columnDef(srcTypeColumn),
					columnDef(destIdColumn),
					columnDef(destTypeColumn),
					columnDef(attrIdColumn),
					columnDef(SQLH.mangleDBName(LinkStorage.SORT_ORDER))),
				table(SQLH.mangleDBName(table)),
				eqSQL(
					column(attrIdColumn),
					parameter(DBType.ID, "refId"))))).toSql(sql);
	
		CompiledStatement deleteLinks = query(
		parameters(
			parameterDef(DBType.ID, "refId")),
		delete(
			table(SQLH.mangleDBName(table)),
			eqSQL(
				column(attrIdColumn),
				parameter(DBType.ID, "refId")))).toSql(sql);
	
		int count = copyLinks.executeUpdate(connection, LongID.valueOf(refId));
		deleteLinks.executeUpdate(connection, LongID.valueOf(refId));
	
		log.info("Migrated composition reference '" + name + "(" + refId + "), " + count + " links moved.");
	}

	private void migrateAnnotations(Log log, PooledConnection connection, HashSet<Long> compositionRefIds)
			throws SQLException {
		DBHelper sql = connection.getSQLDialect();

		CompiledStatement getAllRefs = query(
		parameters(),
		select(
				Util.listWithoutNull(
					_util.branchColumnDefOrNull(),
				columnDef(BasicTypes.IDENTIFIER_DB_NAME),
				columnDef(BasicTypes.REV_MAX_DB_NAME),
				columnDef(SQLH.mangleDBName(KBBasedMetaAttribute.NAME)),
				columnDef(SQLH.mangleDBName(PersistentReference.ANNOTATIONS_MO_ATTRIBUTE))),
			table(SQLH.mangleDBName(PersistentReference.OBJECT_NAME)),
			and(
				eqSQL(
					_util.branchColumnRef(),
					literal(DBType.LONG, TLContext.TRUNK_ID)),
				eqSQL(
					column(SQLH.mangleDBName(BasicTypes.REV_MAX_DB_NAME)),
					literal(DBType.LONG, Revision.CURRENT_REV)),
				eqSQL(
					column(SQLH.mangleDBName(KBBasedMetaAttribute.IMPLEMENTATION_NAME)),
					literal(DBType.STRING, TLStructuredTypeColumns.REFERENCE_IMPL))))).toSql(sql);
		getAllRefs.setResultSetConfiguration(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);

		int inc = _util.getBranchIndexInc();
		try (ResultSet refs = getAllRefs.executeQuery(connection)) {
			while (refs.next()) {
				long refId = refs.getLong(1 + inc);
				if (!compositionRefIds.contains(Long.valueOf(refId))) {
					continue;
				}

				String name = refs.getString(3 + inc);
				String refAnnotationXml = sql.getClobValue(refs, 4 + inc);
				if (refAnnotationXml != null) {
					boolean changed = false;
					Document document = DOMUtil.parse(refAnnotationXml);
					NodeList annotations = document.getElementsByTagName(TLStorage.TAG_NAME);
					for (int n = annotations.getLength() - 1; n >= 0; n--) {
						Node annotation = annotations.item(n);
						annotation.getParentNode().removeChild(annotation);

						changed = true;
					}
					if (changed) {
						String newXml = DOMUtil.toString(document);
						log.info("Removed storage annotation from reference '" + name + "(" + refId + ")" + "': "
							+ newXml);
						refs.updateString(4 + inc, newXml);
						refs.updateRow();
					}
				}
			}
		}
	}
}
